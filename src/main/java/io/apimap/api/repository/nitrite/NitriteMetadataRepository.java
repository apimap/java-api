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

import io.apimap.api.configuration.NitriteConfiguration;
import io.apimap.api.repository.nitrite.entities.ApiClassification;
import io.apimap.api.repository.nitrite.entities.Metadata;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.QueryFilter;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toCollection;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.or;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.NitriteConfiguration.class)
public class NitriteMetadataRepository extends NitriteRepository implements IMetadataRepository<Metadata, ObjectFilter> {
    public NitriteMetadataRepository(NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "metadata");
    }

    /* M */

    public Flux<Metadata> allByFilters(Mono<List<ObjectFilter>> filters) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);

        return filters
                .filter(Objects::nonNull)
                .filter(list -> list.size() > 0)
                .flatMapMany(filterList -> {
                    ObjectFilter objectFilter = and(filterList.toArray(ObjectFilter[]::new));
                    Cursor<Metadata> cursor = repository.find(objectFilter);
                    return Flux.fromIterable(cursor.toList());
                });
    }

    public Flux<Metadata> allByApiId(String apiId) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        Cursor<Metadata> cursor = repository.find(eq("apiId", apiId));
        return Flux.fromIterable(cursor);
    }

    public Flux<Metadata> all() {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        Cursor<Metadata> cursor = repository.find();
        return Flux.fromIterable(cursor);
    }

    public Mono<Metadata> add(Metadata entity) {
        entity.setCreated(new Date());

        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Mono<Metadata> update(Metadata entity) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Mono.justOrEmpty(repository.getById(repository.update(
                eq("id", entity.getId())
                , entity
                , true
        ).iterator().next()));
    }

    public Mono<Metadata> get(String apiId, String version) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Mono.justOrEmpty(repository.find(
                and(eq("apiId", apiId), eq("apiVersion", version))
        ).firstOrDefault());
    }

    public Mono<Boolean> delete(String apiId) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        repository.remove(eq("apiId", apiId));
        return Mono.empty();
    }

    public Mono<Boolean> delete(String apiId, String version) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Mono.just(repository.remove(
                and(eq("apiId", apiId), eq("apiVersion", version))
        ).getAffectedCount() > 0);
    }

    /* OB */

    public Mono<List<ObjectFilter>> queryFilters(List<Filter> filters, QueryFilter queryFilter) {
        HashMap<String, ArrayList<ObjectFilter>> tmp = new HashMap<>();

        filters
                .stream()
                .filter(e -> e.type() == Filter.TYPE.METADATA)
                .forEach(e -> {
                    String key = e.getKey();
                    ArrayList<ObjectFilter> f = tmp.getOrDefault(key, new ArrayList<>());
                    f.add(e.objectFilter());
                    tmp.put(key, f);
                });

        ArrayList<ObjectFilter> objectFilters = tmp
                .values()
                .stream()
                .map(objectFilterArrayList -> or(objectFilterArrayList.toArray(ObjectFilter[]::new)))
                .collect(toCollection(ArrayList::new));

        if (queryFilter != null) {
            objectFilters.add(queryFilter.objectFilter());
        }

        return Mono.just(objectFilters);
    }
}
