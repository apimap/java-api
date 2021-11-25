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
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationTreeCollection;
import io.apimap.api.service.response.ClassificationResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ClassificationResourceService extends FilteredResourceService {
    protected IClassificationRepository classificationRepository;
    protected IApiRepository apiRepository;
    protected ApimapConfiguration apimapConfiguration;

    public ClassificationResourceService(IClassificationRepository classificationRepository,
                                         IApiRepository apiRepository,
                                         ApimapConfiguration apimapConfiguration) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
        this.apimapConfiguration = apimapConfiguration;
    }

    @NotNull
    public Mono<ServerResponse> allClassifications(ServerRequest request) {
        if (!requestQueryFilters(request).isEmpty()) {
            return allFilteredClassifications(request, null);
        }

        return ClassificationResponseBuilder
                .builder(apimapConfiguration)
                .withResourceURI(request.uri())
                .withEmptyBody()
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getClassification(ServerRequest request) {
        String parentClassificationURN = RequestUtil.classificationFromRequest(request);

        if (!requestQueryFilters(request).isEmpty()) {
            return allFilteredClassifications(request, parentClassificationURN);
        }

        return ClassificationResponseBuilder
                .builder(apimapConfiguration)
                .withResourceURI(request.uri())
                .withEmptyBody()
                .okResource();
    }

    protected Mono<ServerResponse> allFilteredClassifications(ServerRequest request, String parentClassificationURN) {
        ApiCollection apiCollection = apiRepository.allApis(requestQueryFilters(request));
        List<ClassificationTreeCollection> collection = classificationRepository.classificationTree(apiCollection, parentClassificationURN);

        ClassificationResponseBuilder builder = ClassificationResponseBuilder
                .builder(apimapConfiguration)
                .withResourceURI(request.uri())
                .withClassificationTreeCollectionBody(collection);

        return builder.okCollection();
    }
}
