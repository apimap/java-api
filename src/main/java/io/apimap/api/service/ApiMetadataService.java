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
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.request.MetadataRequestParser;
import io.apimap.api.service.response.MetadataResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class ApiMetadataService {

    protected IMetadataRepository metadataRepository;
    protected IApiRepository apiRepository;

    public ApiMetadataService(IMetadataRepository metadataRepository,
                              IApiRepository apiRepository) {
        this.metadataRepository = metadataRepository;
        this.apiRepository = apiRepository;
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteMetadata(ServerRequest request) {
        final MetadataResponseBuilder responseBuilder = MetadataResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        metadataRepository.delete(apiId, apiVersion);

        return responseBuilder.noContent();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateMetadata(ServerRequest request) {
        final MetadataResponseBuilder responseBuilder = MetadataResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        final Optional<Metadata> dbEntity = MetadataRequestParser
                .parser()
                .withApiRepository(apiRepository)
                .withRequest(request)
                .metadatDbEntity();

        if (dbEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<Metadata> updatedEntity = metadataRepository.update(dbEntity.get());

        if (updatedEntity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withMetadataBody(updatedEntity.get())
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getMetadata(ServerRequest request) {
        final MetadataResponseBuilder responseBuilder = MetadataResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        final Optional<Metadata> entity = metadataRepository.get(apiId, apiVersion);

        if (entity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withMetadataBody(entity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.rootLevelFromURI(request.uri()).append("api").append(apiName).append("version").uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.rootLevelFromURI(request.uri()).append("api").append(apiName).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.rootLevelFromURI(request.uri()).append("api").uriValue())
                .okResource();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createMetadata(ServerRequest request) {
        final MetadataResponseBuilder responseBuilder = MetadataResponseBuilder.builder();
        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        if(apiRepository.getApi(apiName).isEmpty() || apiRepository.getApiVersion(apiId, apiVersion).isEmpty()){
            return responseBuilder.notFound();
        }

        final Optional<Metadata> dbEntity = MetadataRequestParser
                .parser()
                .withApiRepository(apiRepository)
                .withRequest(request)
                .metadatDbEntity();

        if (dbEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<Metadata> insertedEntity = metadataRepository.add(dbEntity.get());

        if (insertedEntity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withMetadataBody(insertedEntity.get())
                .created(false);
    }
}
