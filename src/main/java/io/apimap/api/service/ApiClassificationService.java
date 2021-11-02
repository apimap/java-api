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

import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.IClassificationRepository;
import io.apimap.api.repository.nitrite.entity.db.ApiClassification;
import io.apimap.api.repository.nitrite.entity.support.ClassificationCollection;
import io.apimap.api.service.request.ClassificationRequestParser;
import io.apimap.api.service.response.ApiResponseBuilder;
import io.apimap.api.service.response.ClassificationResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ApiClassificationService {

    protected IClassificationRepository classificationRepository;
    protected IApiRepository apiRepository;

    public ApiClassificationService(IClassificationRepository classificationRepository,
                                    IApiRepository apiRepository) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteClassification(ServerRequest request) {
        final ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        classificationRepository.delete(apiId, apiVersion);

        return responseBuilder.noContent();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateClassification(ServerRequest request) {
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return ApiResponseBuilder.builder().notFound();
        }

        classificationRepository.delete(apiId, apiVersion);

        return createClassification(request);
    }

    @NotNull
    public Mono<ServerResponse> getClassification(ServerRequest request) {
        final ClassificationResponseBuilder responseBuilder = ClassificationResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withClassificationCollectionBody(classificationRepository.all(apiId, apiVersion))
                .okResource();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createClassification(ServerRequest request) {
        final ClassificationResponseBuilder responseBuilder = ClassificationResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        final Optional<List<ApiClassification>> entities = ClassificationRequestParser
                .parser()
                .withRequest(request)
                .withApiId(apiId)
                .classificationArray();

        if (entities.isEmpty()) {
            return responseBuilder.badRequest();
        }

        if(entities.get().isEmpty()) {
            return responseBuilder
                    .withResourceURI(request.uri())
                    .withClassificationCollectionBody(new ClassificationCollection(Collections.emptyList(), "1.0"))
                    .created(false);
        }

        final ClassificationCollection insertedEntities = classificationRepository.add(apiId, entities.get());

        return responseBuilder
                .withResourceURI(request.uri())
                .withClassificationCollectionBody(insertedEntities)
                .created(false);
    }
}