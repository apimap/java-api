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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IMetadata;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class ApiMetadataService {

    final protected IRESTConverter entityMapper;
    final protected IMetadataRepository metadataRepository;
    final protected IApiRepository apiRepository;
    final protected ApimapConfiguration apimapConfiguration;

    @SuppressFBWarnings
    public ApiMetadataService(final IRESTConverter entityMapper,
                              final IMetadataRepository metadataRepository,
                              final IApiRepository apiRepository,
                              final ApimapConfiguration apimapConfiguration) {
        this.metadataRepository = metadataRepository;
        this.apiRepository = apiRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteMetadata(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.delete(((IApi) api).getId(), context.getApiVersion()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateMetadata(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, MetadataDataRestEntity.class);
        final URI uri = request.uri();

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .flatMap(metadata -> entityMapper.decodeMetadata(context, (JsonApiRestRequestWrapper<MetadataDataRestEntity>) metadata))
                .flatMap(metadata -> apiRepository
                        .get(context.getApiName())
                        .flatMap(api -> {
                            metadata.setApiId(((IApi) api).getId());
                            return Mono.justOrEmpty(metadata);
                        })
                )
                .flatMap(metadata -> metadataRepository.update((IMetadata) metadata)
                        .switchIfEmpty(metadataRepository.add((IMetadata) metadata)))
                .flatMap(metadata -> entityMapper.encodeMetadata(uri, (IMetadata) metadata))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<MetadataDataRestEntity>) version)
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getMetadata(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.get(((IApi) api).getId(), context.getApiVersion()))
                .flatMap(metadata -> entityMapper.encodeMetadata(uri, (IMetadata) metadata))
                .flatMap(metadata -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) metadata)
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.rootLevelFromURI(uri).append("api").append(context.getApiName()).append("version").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.rootLevelFromURI(uri).append("api").append(context.getApiName()).uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.rootLevelFromURI(uri).append("api").uriValue())
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createMetadata(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, MetadataDataRestEntity.class);
        final URI uri = request.uri();

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .flatMap(metadata -> entityMapper.decodeMetadata(context, (JsonApiRestRequestWrapper<MetadataDataRestEntity>) metadata))
                .flatMap(metadata -> apiRepository
                        .get(context.getApiName())
                        .flatMap(api -> {
                            metadata.setApiId(((IApi) api).getId());
                            return Mono.justOrEmpty(metadata);
                        })
                )
                .flatMap(metadata -> metadataRepository.add((IMetadata) metadata))
                .flatMap(metadata -> entityMapper.encodeMetadata(uri, (IMetadata) metadata))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<MetadataDataRestEntity>) version)
                        .created(true)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }
}
