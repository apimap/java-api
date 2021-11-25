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

package io.apimap.api.service.response;

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ApiVersionCollection;
import io.apimap.api.rest.ApiCollectionDataRestEntity;
import io.apimap.api.rest.ApiCollectionRootRestEntity;
import io.apimap.api.rest.ApiDataApiMetadataEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.utils.URIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApiResponseBuilder extends ResponseBuilder<ApiResponseBuilder> {
    public ApiResponseBuilder(long startTime, ApimapConfiguration apimapConfiguration) {
        super(startTime, apimapConfiguration);
    }

    public static ApiResponseBuilder builder(ApimapConfiguration apimapConfiguration) {
        return new ApiResponseBuilder(System.currentTimeMillis(), apimapConfiguration);
    }

    public ApiResponseBuilder withApiVersionCollectionBody(ApiVersionCollection value) {
        List<ApiVersionDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> {
                    String link = "";

                    if (e.getVersion() != null) {
                        link = URIUtil.fromURI(resourceURI).append(e.getVersion()).stringValue();
                    }

                    return new ApiVersionDataRestEntity(
                            e.getVersion(),
                            e.getCreated(),
                            link);

                })
                .collect(Collectors.toList());

        this.body = new JsonApiRestResponseWrapper<>(items);
        return this;
    }

    public ApiResponseBuilder withApiCollectionBody(ApiCollection value) {
        ArrayList<ApiCollectionDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> {
                    JsonApiRelationships relationships = new JsonApiRelationships();
                    relationships.addRelationshipRef(
                            JsonApiRestResponseWrapper.VERSION_COLLECTION,
                            URIUtil.fromURI(resourceURI).append(e.getApi().getName()).append("version").uriValue());

                    return new ApiCollectionDataRestEntity(
                            e.getApi().getName(),
                            e.getApi().getCodeRepositoryUrl(),
                            e.getMetadata().isPresent() ? e.getMetadata().get().getDescription() : null,
                            e.getMetadata().isPresent() ? e.getMetadata().get().getReleaseStatus() : null,
                            e.getVersion().isPresent() ? e.getVersion().get().getVersion() : null,
                            e.getMetadata().isPresent() ? e.getMetadata().get().getDocumentation() : null,
                            URIUtil.fromURI(resourceURI).append(e.getApi().getName()).stringValue(),
                            relationships);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        ApiCollectionRootRestEntity apiCollectionRootRestEntity = new ApiCollectionRootRestEntity(items);
        this.body = new JsonApiRestResponseWrapper<>(apiCollectionRootRestEntity);
        return this;
    }

    public ApiResponseBuilder withApiBody(Api value) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        relationships.addRelationshipRef(
                JsonApiRestResponseWrapper.VERSION_COLLECTION,
                URIUtil.rootLevelFromURI(resourceURI).append("api").append(value.getName()).append("version").uriValue());

        ApiDataRestEntity apiRootRestEntity = new ApiDataRestEntity(
                value.getName(),
                value.getCodeRepositoryUrl(),
                resourceURI.toString(),
                relationships
        );

        if (value.getToken() != null) {
            apiRootRestEntity.setMeta(new ApiDataApiMetadataEntity(value.getToken()));
        }

        this.body = new JsonApiRestResponseWrapper<ApiDataRestEntity>(apiRootRestEntity);
        return this;
    }

    public ApiResponseBuilder withApiVersionBody(ApiVersion value) {
        ApiVersionDataRestEntity apiVersionDataRestEntity = new ApiVersionDataRestEntity(
                value.getVersion(),
                value.getCreated(),
                URIUtil.fromURI(resourceURI).append(value.getVersion()).stringValue()
        );

        this.body = new JsonApiRestResponseWrapper<>(apiVersionDataRestEntity);
        return this;
    }
}
