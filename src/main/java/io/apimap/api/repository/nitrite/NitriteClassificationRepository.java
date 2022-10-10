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

package io.apimap.api.repository.nitrite;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.configuration.NitriteConfiguration;
import io.apimap.api.repository.nitrite.entities.ApiClassification;
import io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.service.query.Filter;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.dizitart.no2.objects.filters.ObjectFilters.*;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.NitriteConfiguration.class)
public class NitriteClassificationRepository extends NitriteRepository implements IClassificationRepository<ApiClassification, ObjectFilter> {

    public static final String TAXONOMY_VERSION = "1";

    protected NitriteTaxonomyRepository taxonomyRepository;

    @SuppressFBWarnings
    public NitriteClassificationRepository(NitriteConfiguration nitriteConfiguration,
                                           NitriteTaxonomyRepository taxonomyRepository) {
        super(nitriteConfiguration, "classification");
        this.taxonomyRepository = taxonomyRepository;
    }

    /* C */

    @Override
    public Flux<ApiClassification> all() {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find();
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Flux<ApiClassification> allByURN(String taxonomyUrn) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(eq("taxonomyUrn", taxonomyUrn));
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Flux<ApiClassification> allByFilters(Mono<List<ObjectFilter>> filters) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);

        return filters
                .filter(Objects::nonNull)
                .filter(list -> list.size() > 0)
                .flatMapMany(filterList -> {
                    ObjectFilter objectFilter = or(filterList.toArray(ObjectFilter[]::new));
                    Cursor<ApiClassification> cursor = repository.find(objectFilter);
                    return Flux.fromIterable(cursor.toList());
                })
                .groupBy(ApiClassification::getApiId)
                .flatMap(classification -> classification.reduce((api1, api2) -> api1.getCreated().compareTo(api2.getCreated()) > 0 ? api1 : api2));
    }

    @Override
    public Flux<ApiClassification> all(String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(eq("apiId", apiId));
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Flux<ApiClassification> all(String apiId, String apiVersion) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        );
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Mono<ApiClassification> update(ApiClassification entity, String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Mono.justOrEmpty(repository.getById(repository.update(eq("apiId", apiId), entity).iterator().next()));
    }

    @Override
    public Mono<ApiClassification> add(ApiClassification entity) {
        entity.setCreated(Instant.now());

        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    @Override
    public Mono<ApiClassification> get(String apiId, String api, String taxonomyUrn) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Mono.justOrEmpty(repository.find(
                and(eq("apiId", apiId),
                        eq("taxonomyUrn", taxonomyUrn)
                )
        ).firstOrDefault());
    }

    @Override
    public Mono<Boolean> delete(String apiId, String apiVersion) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Mono.just(repository.remove(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        ).getAffectedCount() > 0);
    }

    @Override
    public Mono<Boolean> delete(String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Mono.just(repository.remove(eq("apiId", apiId)).getAffectedCount() > 0);
    }

    /* OB */

    @Override
    public Mono<List<ObjectFilter>> queryFilters(List<Filter> filters) {
        return Flux.fromStream(filters
                        .stream()
                        .filter(filter -> filter.type() == Filter.TYPE.CLASSIFICATION)
                        .map(filter -> taxonomyRepository
                                .getTaxonomyCollectionVersionURN(filter.getValue(), TAXONOMY_VERSION, TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                                .map(TaxonomyCollectionVersionURN::getUrl)))
                .flatMap(url -> url)
                .flatMap(url -> taxonomyRepository
                        .allTaxonomyCollectionVersionURNsBellowUrl(url)
                        .flatMap(version -> Mono.just(eq("taxonomyUrn", version.getUrn()))))
                .collectList();
    }
}
