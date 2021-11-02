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
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.response.EmptyResponseBuilder;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class RootResourceService {

    protected ApimapConfiguration apimapConfiguration;

    public RootResourceService(ApimapConfiguration apimapConfiguration) {
        this.apimapConfiguration = apimapConfiguration;
    }

    @NotNull
    public Mono<ServerResponse> rootResource(ServerRequest request) {
        return EmptyResponseBuilder
                .builder(apimapConfiguration)
                .withResourceURI(request.uri())
                .withoutBody()
                .addRelatedRef(JsonApiRestResponseWrapper.API_COLLECTION, URIUtil.apiCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.CLASSIFICATION_COLLECTION, URIUtil.classificationCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.TAXONOMY_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.STATISTICS_COLLECTION, URIUtil.statisticsFromURI(request.uri()).uriValue())
                .okResource();
    }
}
