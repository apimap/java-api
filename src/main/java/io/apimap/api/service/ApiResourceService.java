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

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.IClassificationRepository;
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.ITaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ApiVersionCollection;
import io.apimap.api.rest.ApiDataApiMetadataEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.request.ApiRequestParser;
import io.apimap.api.service.response.ApiResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.ServerResponse.notFound;

@Service
public class ApiResourceService extends FilteredResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiResourceService.class);

    protected IApiRepository apiRepository;
    protected IMetadataRepository metadataRepository;
    protected ITaxonomyRepository taxonomyRepository;
    protected IClassificationRepository classificationRepository;

    protected ApimapConfiguration apimapConfiguration;

    public ApiResourceService(IApiRepository apiRepository,
                              IMetadataRepository metadataRepository,
                              ITaxonomyRepository taxonomyRepository,
                              IClassificationRepository classificationRepository,
                              ApimapConfiguration apimapConfiguration) {
        this.apiRepository = apiRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.metadataRepository = metadataRepository;
        this.classificationRepository = classificationRepository;
        this.apimapConfiguration = apimapConfiguration;
    }

    /*
    API
     */

    @NotNull
    public Mono<ServerResponse> allApisZipArchive(ServerRequest request) {
        return ServerResponse.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .header("Content-Disposition", "attachment; filename=\"apis.zip\"")
                .contentType(MediaType.valueOf("application/zip"))
                .bodyValue("hello");/*
        String s = "hello world";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos)) {

  /* File is not on the disk, test.txt indicates
     only the file name to be put into the zip
            ZipEntry entry = new ZipEntry("test.txt");

            zos.putNextEntry(entry);
            zos.write(s.getBytes());
            zos.closeEntry();

  /* use more Entries to add more files
     and use closeEntry() to close each file entry




        } catch (IOException e) {
            e.printStackTrace();
        }
*/
      //  return null;
    }

    @NotNull
    public Mono<ServerResponse> allApis(ServerRequest request) {
        if (!requestQueryFilters(request).isEmpty()) {
            return allFilteredApis(request);
        }

        return allUnfilteredApis(request);
    }

    protected Mono<ServerResponse> allUnfilteredApis(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        ApiCollection collection = apiRepository.all();

        return responseBuilder
                .withResourceURI(request.uri())
                .withApiCollectionBody(collection)
                .okCollection();
    }

    protected Mono<ServerResponse> allFilteredApis(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        ApiCollection collection = apiRepository.all(requestQueryFilters(request));

        return responseBuilder
                .withResourceURI(request.uri())
                .withApiCollectionBody(collection)
                .okCollection();
    }

    @NotNull
    public Mono<ServerResponse> createApi(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);
        ApiRequestParser requestParser = ApiRequestParser
                .parser()
                .withRequest(request)
                .parse(ApiDataRestEntity.class);

        final Optional<Api> entity = requestParser
                .apiDbEntity();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        if (apiRepository.get(entity.get().getName()).isPresent()) {
            return responseBuilder.conflict();
        }

        final Optional<Api> insertedEntity = apiRepository.add(entity.get());

        if (insertedEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<ApiDataApiMetadataEntity> apiMetadata = requestParser.apiMetadata();

        Mono<ServerResponse> response = responseBuilder
                .withResourceURI(URIUtil.apiCollectionFromURI(request.uri()).append(insertedEntity.get().getName()).uriValue())
                .withApiBody(insertedEntity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.classificationCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.TAXONOMY_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).uriValue())
                .created(true);

        return response;
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateApi(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);

        final Optional<Api> entity = ApiRequestParser
                .parser()
                .withRequest(request)
                .parse(ApiDataRestEntity.class)
                .apiDbEntity();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<Api> updatedEntity = apiRepository.update(entity.get(), apiName);

        if (updatedEntity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(URIUtil.apiCollectionFromURI(request.uri()).append(updatedEntity.get().getName()).uriValue())
                .withApiBody(updatedEntity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.classificationCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.TAXONOMY_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).uriValue())
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getApi(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final Optional<Api> entity = apiRepository.get(RequestUtil.apiNameFromRequest(request));

        if (entity.isEmpty()) {
            return notFound().build();
        }

        return responseBuilder
                .withResourceURI(URIUtil.apiCollectionFromURI(request.uri()).append(entity.get().getName()).uriValue())
                .withApiBody(entity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).append(entity.get().getName()).append("version").uriValue())
                .okResource();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteApi(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);

        //Clear all connected information
        apiRepository.delete(apiName);
        classificationRepository.delete(apiName);
        metadataRepository.delete(apiName);

        return responseBuilder.noContent();
    }

    /*
    API Version
     */

    @NotNull
    public Mono<ServerResponse> getApiVersion(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        final Optional<ApiVersion> versionContent = apiRepository.getApiVersion(apiId, apiVersion);

        if (versionContent.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withApiVersionBody(versionContent.get())
                .addRelatedRef(JsonApiRestResponseWrapper.METADATA_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).append(apiName).append("version").append(apiVersion).append("metadata").uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).append(apiName).append("version").append(apiVersion).append("classification").uriValue())
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> allApiVersions(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiId = apiRepository.apiId(apiName);

        final ApiVersionCollection collection = apiRepository.allApiVersions(apiId);

        return responseBuilder
                .withResourceURI(request.uri())
                .withApiVersionCollectionBody(collection)
                .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.apiCollectionFromURI(request.uri()).append(apiName).uriValue())
                .okCollection();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createApiVersion(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);

        if(apiRepository.get(apiName).isEmpty()){
            return responseBuilder.notFound();
        }

        final Optional<ApiVersion> entity = ApiRequestParser
                .parser()
                .withRequest(request)
                .withApiRepository(apiRepository)
                .apiVersionDbEntity();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<ApiVersion> insertedEntity = apiRepository.addApiVersion(entity.get());

        if (insertedEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withApiVersionBody(insertedEntity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.API_ELEMENT, URIUtil.apiCollectionFromURI(request.uri()).append(apiName).uriValue())
                .created(false);
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteApiVersion(ServerRequest request) {
        ApiResponseBuilder responseBuilder = ApiResponseBuilder.builder(apimapConfiguration);

        final String apiName = RequestUtil.apiNameFromRequest(request);
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);

        //Clear all connected information
        apiRepository.deleteApiVersion(apiName, apiVersion);
        classificationRepository.delete(apiName, apiVersion);
        metadataRepository.delete(apiName, apiVersion);

        return responseBuilder.noContent();
    }
}