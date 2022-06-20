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
import io.apimap.api.repository.SearchRepository;
import io.apimap.api.repository.entities.IApi;
import io.apimap.api.repository.entities.IApiClassification;
import io.apimap.api.repository.entities.IMetadata;
import io.apimap.api.repository.entities.IRESTEntityMapper;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ClassificationContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.ClassificationTreeBuilder;
import io.apimap.api.utils.RequestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Service
public class ClassificationResourceService {

    final protected IRESTEntityMapper entityMapper;
    final protected IClassificationRepository classificationRepository;
    final protected IApiRepository apiRepository;
    final protected ApimapConfiguration apimapConfiguration;
    final protected IMetadataRepository metadataRepository;
    final protected ClassificationTreeBuilder classificationTreeBuilder;
    final protected SearchRepository searchRepository;

    public ClassificationResourceService(final IClassificationRepository classificationRepository,
                                         final IApiRepository apiRepository,
                                         final IMetadataRepository metadataRepository,
                                         final ITaxonomyRepository taxonomyRepository,
                                         final ApimapConfiguration apimapConfiguration,
                                         final SearchRepository searchRepository,
                                         IRESTEntityMapper entityMapper) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
        this.metadataRepository = metadataRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
        this.classificationTreeBuilder = new ClassificationTreeBuilder(this.classificationRepository, this.apiRepository, this.metadataRepository, taxonomyRepository);
        this.searchRepository = searchRepository;
    }

    @NotNull
    public Mono<ServerResponse> allClassifications(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ClassificationContext context = RequestUtil.classificationContextFromRequest(request);

        if (context.isEmpty()) return ServerResponse.noContent().build();

        return searchRepository
                .find(context.getFilters(), context.getQuery())
                .collectList()
                .flatMap(apis -> classificationTreeBuilder.build(context, (List) apis))
                .flatMap(tree -> entityMapper.encodeClassifications(uri, (List) tree))
                .flatMap(classifications -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) classifications)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.noContent().build()));
    }

    @NotNull
    public Mono<ServerResponse> getClassification(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ClassificationContext context = RequestUtil.classificationContextFromRequest(request);

        if (context.isEmpty()) return ServerResponse.noContent().build();

        return classificationRepository
                .allByURN(context.getClassificationURN())
                .flatMap(classification -> metadataRepository
                        .get(((IApiClassification) classification).getApiId(), ((IApiClassification) classification).getApiVersion())
                        .flatMap(metadata -> apiRepository.getById(((IMetadata) metadata).getApiId())
                                .flatMap(api -> Mono.just(Tuples.of(api, metadata)))
                                .flatMap(result -> apiRepository.getApiVersion(((IApi) ((Tuple2) result).getT1()).getId(), ((IMetadata) ((Tuple2) result).getT2()).getApiVersion())
                                        .flatMap(apiVersion -> Mono.just(Tuples.of(((Tuple2<?, ?>) result).getT1(), ((Tuple2<?, ?>) result).getT2(), apiVersion)))
                                )
                        ))
                .collectList()
                .flatMap(apis -> classificationTreeBuilder.build(context, (List) apis))
                .flatMap(tree -> entityMapper.encodeClassifications(uri, (List) tree))
                .flatMap(classifications -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) classifications)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.noContent().build()));
    }
}
