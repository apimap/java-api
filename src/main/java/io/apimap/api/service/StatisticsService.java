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
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.ITaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.support.StatisticsCollection;
import io.apimap.api.repository.nitrite.entity.support.StatisticsCollectionCollection;
import io.apimap.api.repository.nitrite.entity.support.StatisticsValue;
import io.apimap.api.repository.nitrite.entity.support.StatisticsValueCollection;
import io.apimap.api.service.response.StatisticsResponseBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class StatisticsService {
    protected IApiRepository apiRepository;
    protected ITaxonomyRepository taxonomyRepository;
    protected IMetadataRepository metadataRepository;
    protected ApimapConfiguration apimapConfiguration;

    public StatisticsService(IApiRepository apiRepository,
                             ITaxonomyRepository taxonomyRepository,
                             IMetadataRepository metadataRepository,
                             ApimapConfiguration apimapConfiguration) {
        this.apiRepository = apiRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.metadataRepository = metadataRepository;
        this.apimapConfiguration = apimapConfiguration;
    }

    @NotNull
    public Mono<ServerResponse> allStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final ArrayList<StatisticsCollection> allStatisticsCollectionList = new ArrayList<>();
        allStatisticsCollectionList.add(new StatisticsCollection("apis", "Number of APIs"));
        allStatisticsCollectionList.add(new StatisticsCollection("taxonomies", "Number of taxonomies"));
        allStatisticsCollectionList.add(new StatisticsCollection("interface-specification", "Interface specification"));
        allStatisticsCollectionList.add(new StatisticsCollection("architecture-layer", "Architecture layer"));
        allStatisticsCollectionList.add(new StatisticsCollection("apis-history", "Creation date of APIs"));
        final StatisticsCollectionCollection statisticsCollectionCollection = new StatisticsCollectionCollection(allStatisticsCollectionList);

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsCollectionCollectionBody(statisticsCollectionCollection)
                .okCollection();
    }

    @NotNull
    public Mono<ServerResponse> getApiCountStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final ArrayList<StatisticsValue> apiCountStatisticsList = new ArrayList<>();
        apiCountStatisticsList.add(new StatisticsValue("global", this.apiRepository.numberOfApis().toString()));
        final StatisticsValueCollection statisticsValueCollection = new StatisticsValueCollection(apiCountStatisticsList);

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsValueCollectionBody(statisticsValueCollection)
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getTaxonomiesStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final ArrayList<StatisticsValue> numberOfTaxonomiesStatistic = new ArrayList<>();
        numberOfTaxonomiesStatistic.add(new StatisticsValue("global", this.taxonomyRepository.numberOfTaxonomies().toString()));
        final StatisticsValueCollection statisticsValueCollection = new StatisticsValueCollection(numberOfTaxonomiesStatistic);

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsValueCollectionBody(statisticsValueCollection)
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getInterfaceSpecificationStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final StatisticsValueCollection statisticsValueCollection = new StatisticsValueCollection(interfaceSpecificationsStatistics());

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsValueCollectionBody(statisticsValueCollection)
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getArchitectureLayerStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final StatisticsValueCollection statisticsValueCollection = new StatisticsValueCollection(architectureLayerStatistics());

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsValueCollectionBody(statisticsValueCollection)
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> getApiCreatedStatistics(ServerRequest request) {
        StatisticsResponseBuilder responseBuilder = StatisticsResponseBuilder.builder(apimapConfiguration);

        final StatisticsValueCollection statisticsValueCollection = new StatisticsValueCollection(apiCreatedStatistics());

        return responseBuilder
                .withResourceURI(request.uri())
                .withStatisticsValueCollectionBody(statisticsValueCollection)
                .okResource();
    }

    private List<StatisticsValue> interfaceSpecificationsStatistics() {
        return getStatisticsValues(Metadata::getInterfaceSpecification);
    }

    private List<StatisticsValue> architectureLayerStatistics() {
        return getStatisticsValues(Metadata::getArchitectureLayer);
    }

    private List<StatisticsValue> getStatisticsValues( Function<Metadata,String> groupingBy) {
        return apiRepository.all().getItems()
                .stream()
                .map(api -> api.getMetadata().isPresent() ? api.getMetadata().get() : null)
                .filter(Objects::nonNull)
                .collect(groupingBy(groupingBy, Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> new StatisticsValue(e.getKey(), e.getValue().toString()))
                .collect(toList());
    }

    private List<StatisticsValue> apiCreatedStatistics() {
        return apiRepository.all().getItems()
                .stream()
                .map(api -> api.getMetadata().isPresent() ? api.getMetadata().get() : null)
                .filter(Objects::nonNull)
                .map(metadata -> new StatisticsValue(metadata.getName(), metadata.getCreated().toString()))
                .collect(toList());
    }
}
