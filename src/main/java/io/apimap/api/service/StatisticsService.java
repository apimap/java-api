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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.generic.StatisticsCollection;
import io.apimap.api.repository.generic.StatisticsValue;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IMetadata;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.StatisticsCollectionCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsCollectionRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.response.ResponseBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {
    final protected IApiRepository apiRepository;
    final protected ITaxonomyRepository taxonomyRepository;
    final protected IMetadataRepository metadataRepository;
    final protected ApimapConfiguration apimapConfiguration;
    final protected IRESTConverter entityMapper;

    @SuppressFBWarnings
    public StatisticsService(final IApiRepository apiRepository,
                             final ITaxonomyRepository taxonomyRepository,
                             final IMetadataRepository metadataRepository,
                             final ApimapConfiguration apimapConfiguration,
                             final IRESTConverter entityMapper) {
        this.apiRepository = apiRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.metadataRepository = metadataRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }

    @NotNull
    public Mono<ServerResponse> allStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        final ArrayList<StatisticsCollection> collections = new ArrayList<>();
        collections.add(new StatisticsCollection("apis", "Number of APIs"));
        collections.add(new StatisticsCollection("taxonomies", "Number of taxonomies"));
        collections.add(new StatisticsCollection("interface-specification", "Interface specification"));
        collections.add(new StatisticsCollection("architecture-layer", "Architecture layer"));
        collections.add(new StatisticsCollection("apis-history", "Creation date of APIs"));

        return Flux
                .fromIterable(collections)
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatisticsCollection(uri, collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getApiCountStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return apiRepository
                .numberOfApis()
                .flatMapMany(value -> Mono.just(new StatisticsValue("global", value.toString())))
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatistics(uri, (List<StatisticsValue>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getTaxonomiesStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return taxonomyRepository
                .numberOfTaxonomies()
                .flatMapMany(value -> Mono.just(new StatisticsValue("global", value.toString())))
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatistics(uri, (List<StatisticsValue>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getInterfaceSpecificationStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return metadataRepository
                .all()
                .groupBy(metadata -> ((IMetadata) metadata).getInterfaceSpecification())
                .flatMap(group -> ((GroupedFlux) group)
                        .count()
                        .flatMapMany(value -> Mono.just(new StatisticsValue(((GroupedFlux) group).key().toString(), String.valueOf(value))))
                )
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatistics(uri, (List<StatisticsValue>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getArchitectureLayerStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return metadataRepository
                .all()
                .groupBy(metadata -> ((IMetadata) metadata).getArchitectureLayer())
                .flatMap(group -> ((GroupedFlux) group)
                        .count()
                        .flatMapMany(value -> Mono.just(new StatisticsValue(((GroupedFlux) group).key().toString(), String.valueOf(value))))
                )
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatistics(uri, (List<StatisticsValue>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getApiCreatedStatistics(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return apiRepository
                .all()
                .flatMap(api -> Mono.just(new StatisticsValue(((IApi) api).getName(), ((IApi) api).getCreated().toString())))
                .collectList()
                .flatMap(collection -> entityMapper.encodeStatistics(uri, (List<StatisticsValue>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }
}
