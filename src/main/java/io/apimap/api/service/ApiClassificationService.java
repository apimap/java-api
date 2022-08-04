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
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IApiClassification;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
public class ApiClassificationService {

    final protected IClassificationRepository classificationRepository;
    final protected IApiRepository apiRepository;
    final protected IRESTConverter entityMapper;
    final protected ApimapConfiguration apimapConfiguration;

    public ApiClassificationService(final IRESTConverter entityMapper,
                                    final IClassificationRepository classificationRepository,
                                    final IApiRepository apiRepository,
                                    final ApimapConfiguration apimapConfiguration) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteClassification(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> classificationRepository.delete(((IApi) api).getId(), context.getApiVersion()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateClassification(final ServerRequest request) {
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> classificationRepository.delete(((IApi) api).getId(), context.getApiVersion()))
                .flatMap(success -> createClassification(request))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @NotNull
    public Mono<ServerResponse> getClassification(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final URI uri = request.uri();

        return apiRepository
                .get(context.getApiName())
                .flatMapMany(api -> classificationRepository.all(((IApi) api).getId(), context.getApiVersion())
                )
                .collectList()
                .flatMap(collection -> entityMapper.encodeApiClassifications(uri, (List<IApiClassification>) collection))
                .flatMap(classification -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) classification)
                        .okCollection()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createClassification(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ClassificationRootRestEntity.class);
        final URI uri = request.uri();

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .zipWith(apiRepository.get(context.getApiName()), (classifications, api) -> Flux.fromStream(((JsonApiRestRequestWrapper<ClassificationRootRestEntity>) classifications)
                                .getData()
                                .getData()
                                .stream())
                        .flatMap(classification -> entityMapper.decodeClassification(context.withApiId(((IApi) api).getId()), classification))
                        .flatMap(classification -> classificationRepository.add(classification))
                        .collectList()
                )
                .flatMap(collection -> collection)
                .flatMap(collection -> entityMapper.encodeApiClassifications(uri, (List<IApiClassification>) collection))
                .flatMap(classification -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) classification)
                        .created(false)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }
}