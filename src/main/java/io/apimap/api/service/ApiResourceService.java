/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package io.apimap.api.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IApiVersion;
import io.apimap.api.repository.repository.*;
import io.apimap.api.rest.ApiCollectionRootRestEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionCollectionRootRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ApiResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiResourceService.class);

    final protected IRESTConverter entityMapper;
    final protected IApiRepository apiRepository;
    final protected IMetadataRepository metadataRepository;
    final protected ITaxonomyRepository taxonomyRepository;
    final protected IClassificationRepository classificationRepository;
    final protected IVoteRepository voteRepository;

    final protected ApimapConfiguration apimapConfiguration;

    public ApiResourceService(final IApiRepository apiRepository,
                              final IMetadataRepository metadataRepository,
                              final ITaxonomyRepository taxonomyRepository,
                              final IClassificationRepository classificationRepository,
                              final ApimapConfiguration apimapConfiguration,
                              final IRESTConverter entityMapper,
                              final IVoteRepository voteRepository) {
        this.apiRepository = apiRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.metadataRepository = metadataRepository;
        this.classificationRepository = classificationRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
        this.voteRepository = voteRepository;
    }

    @NotNull
    public Mono<ServerResponse> allApis(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final URI uri = request.uri();

        return apiRepository
                .allByFilters(apiRepository. queryFilters(context.getFilters()))
                .flatMap(api -> apiRepository.getLatestApiVersion(((IApi) api).getId())
                                .flatMap(apiVersion -> Mono.just(Tuples.of(api, apiVersion)))
                                .flatMap(tuple -> metadataRepository.get(((IApi) ((Tuple2) tuple).getT1()).getId(), ((IApiVersion) ((Tuple2) tuple).getT2()).getVersion())
                                    .flatMap(metadata -> Mono.just(Tuples.of(api, metadata, ((Tuple2<?, ?>) tuple).getT2())))))
                .collectList()
                .flatMap(result -> entityMapper.encodeApis(uri, (List) result))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>) collection)
                        .okCollection()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.noContent().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidAccessToken(#request)")
    public Mono<ServerResponse> allApisZip(final ServerRequest request) {
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
        DataBuffer dataBuffer = nettyDataBufferFactory.allocateBuffer();
        ZipOutputStream zipOutputStream = new ZipOutputStream(dataBuffer.asOutputStream());
        ReentrantLock lock = new ReentrantLock();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return Mono.when(
                        apiRepository
                                .all()
                                .collectList()
                                .publishOn(Schedulers.boundedElastic())
                                .doOnNext(collection -> {
                                    try {
                                        lock.lock();
                                        zipOutputStream.putNextEntry(new ZipEntry("apis.json"));
                                        zipOutputStream.write(mapper.writeValueAsBytes(collection));
                                        zipOutputStream.closeEntry();
                                        lock.unlock();
                                    } catch (IOException ignored) {
                                    }
                                }),
                        classificationRepository
                                .all()
                                .collectList()
                                .publishOn(Schedulers.boundedElastic())
                                .doOnNext(collection -> {
                                    try {
                                        lock.lock();
                                        zipOutputStream.putNextEntry(new ZipEntry("classifications.json"));
                                        zipOutputStream.write(mapper.writeValueAsBytes(collection));
                                        zipOutputStream.closeEntry();
                                        lock.unlock();
                                    } catch (IOException ignored) {
                                    }
                                }),
                        metadataRepository
                                .all()
                                .collectList()
                                .publishOn(Schedulers.boundedElastic())
                                .doOnNext(collection -> {
                                    try {
                                        lock.lock();
                                        zipOutputStream.putNextEntry(new ZipEntry("metadata.json"));
                                        zipOutputStream.write(mapper.writeValueAsBytes(collection));
                                        zipOutputStream.closeEntry();
                                        lock.unlock();
                                    } catch (IOException ignored) {
                                    }
                                })
                )
                .thenReturn(Boolean.TRUE)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(status -> {
                    lock.lock();
                    try {
                        zipOutputStream.close();
                    } catch (IOException ignored) {
                    }
                    lock.unlock();

                    return ServerResponse.status(HttpStatus.OK)
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Request-Method", "GET")
                            .contentType(new MediaType("application", "zip"))
                            .body(Mono.just(dataBuffer), DataBuffer.class);
                });
    }

    @NotNull
    public Mono<ServerResponse> createApi(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ApiDataRestEntity.class);

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .filter(Objects::nonNull)
                .flatMap(api -> entityMapper.decodeApi((JsonApiRestRequestWrapper<ApiDataRestEntity>) api))
                .flatMap(api -> apiRepository.add(api))
                .flatMap(api -> entityMapper.encodeApi(uri, (IApi) api))
                .flatMap(api -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(URIUtil.apiCollectionFromURI(uri).append(((JsonApiRestResponseWrapper<ApiDataRestEntity>) api).getData().getName()).uriValue())
                        .withBody((JsonApiRestResponseWrapper<ApiDataRestEntity>) api)
                        .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.apiCollectionFromURI(uri).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.classificationCollectionFromURI(uri).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.TAXONOMY_COLLECTION, URIUtil.taxonomyCollectionFromURI(uri).uriValue())
                        .created(true)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateApi(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ApiDataRestEntity.class);
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .filter(Objects::nonNull)
                .flatMap(api -> entityMapper.decodeApi((JsonApiRestRequestWrapper<ApiDataRestEntity>) api))
                .flatMap(api -> apiRepository.update(api, context.getApiName()))
                .flatMap(api -> entityMapper.encodeApi(uri, (IApi) api))
                .flatMap(api -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(URIUtil.apiCollectionFromURI(request.uri()).append(((JsonApiRestResponseWrapper<ApiDataRestEntity>) api).getData().getName()).uriValue())
                        .withBody((JsonApiRestResponseWrapper<ApiDataRestEntity>) api)
                        .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.classificationCollectionFromURI(request.uri()).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.TAXONOMY_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).uriValue())
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    @NotNull
    public Mono<ServerResponse> getApi(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> entityMapper.encodeApi(uri, (IApi) api))
                .flatMap(api -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(URIUtil.apiCollectionFromURI(uri).append(((JsonApiRestResponseWrapper<ApiDataRestEntity>) api).getData().getName()).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.apiCollectionFromURI(uri).append(((JsonApiRestResponseWrapper<ApiDataRestEntity>) api).getData().getName()).append("version").uriValue())
                        .withBody((JsonApiRestResponseWrapper<?>) api)
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteApi(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> classificationRepository.delete(((IApi) api).getId())
                        .zipWith(metadataRepository.delete(((IApi) api).getId()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue())
                        .zipWith(apiRepository.delete(((IApi) api).getName()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    /*
    API Version
     */

    @NotNull
    public Mono<ServerResponse> getApiVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> apiRepository.getApiVersion(((IApi) api).getId(), context.getApiVersion())
                        .flatMap(version -> voteRepository.rating(((IApi) api).getId(), context.getApiVersion())
                            .flatMap(rating -> Mono.just(Tuples.of((IApiVersion) version, rating)))))
                .flatMap(tuple -> entityMapper.encodeApiVersion(uri, (IApiVersion)((Tuple2) tuple).getT1(), (Integer)((Tuple2) tuple).getT2()))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) version)
                        .addRelatedRef(JsonApiRestResponseWrapper.METADATA_COLLECTION, URIUtil.apiCollectionFromURI(uri).append(context.getApiName()).append("version").append(context.getApiVersion()).append("metadata").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.apiCollectionFromURI(uri).append(context.getApiName()).append("version").append(context.getApiVersion()).append("classification").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.README_ELEMENT, URIUtil.apiCollectionFromURI(uri).append(context.getApiName()).append("version").append(context.getApiVersion()).append("readme").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.CHANGELOG_ELEMENT, URIUtil.apiCollectionFromURI(uri).append(context.getApiName()).append("version").append(context.getApiVersion()).append("changelog").uriValue())
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> allApiVersions(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final URI uri = request.uri();

        return apiRepository
                .get(context.getApiName())
                .flatMapMany(api -> apiRepository.allApiVersions(((IApi) api).getId())
                        .flatMap(version -> voteRepository.rating(((IApi) api).getId(), ((IApiVersion) version).getVersion())
                                .flatMap(rating -> Mono.justOrEmpty(Tuples.of((IApiVersion) version, rating)))))
                .collectList()
                .flatMap(collection -> entityMapper.encodeApiVersions(uri, (List<Tuple2<IApiVersion, Integer>>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<ApiVersionCollectionRootRestEntity>) collection)
                        .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.apiCollectionFromURI(uri).append(context.getApiName()).uriValue())
                        .okCollection()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.noContent().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createApiVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ApiVersionDataRestEntity.class);
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .filter(Objects::nonNull)
                .zipWith(apiRepository.get(context.getApiName()), (version, api) -> entityMapper.decodeApiVersion((IApi) api, (JsonApiRestRequestWrapper<ApiVersionDataRestEntity>) version))
                .flatMap(tuple -> tuple)
                .flatMap(version -> apiRepository.addApiVersion((IApiVersion) version))
                .flatMap(version -> entityMapper.encodeApiVersion(uri, (IApiVersion) version, Integer.valueOf(0)))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<ApiVersionDataRestEntity>) version)
                        .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.apiCollectionFromURI(request.uri()).append(context.getApiName()).uriValue())
                        .created(true)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteApiVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> classificationRepository.delete(((IApi) api).getId(), context.getApiVersion())
                        .zipWith(metadataRepository.delete(((IApi) api).getId(), context.getApiVersion()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue())
                        .zipWith(apiRepository.deleteApiVersion(((IApi) api).getId(), context.getApiVersion()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }
}