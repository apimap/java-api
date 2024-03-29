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
import io.apimap.api.repository.interfaces.IApiVersion;
import io.apimap.api.repository.nitrite.entities.Api;
import io.apimap.api.repository.nitrite.entities.ApiVersion;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.service.query.Filter;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static org.dizitart.no2.objects.filters.ObjectFilters.*;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.NitriteConfiguration.class)
public class NitriteApiRepository extends NitriteRepository implements IApiRepository<Api, ApiVersion, ObjectFilter> {

    @SuppressFBWarnings
    public NitriteApiRepository(NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "api");
    }

    /* A */
    @Override
    public Flux<Api> all() {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Cursor<Api> cursor = repository.find();
        return Flux.fromIterable(cursor);
    }

    @Override
    public Flux<Api> allByFilters(Mono<List<ObjectFilter>> filters) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);

        return filters
                .flatMapMany(filterList -> {
                    if(filterList.size() < 1){
                        return all();
                    }else{
                        ObjectFilter objectFilter = and(filterList.toArray(ObjectFilter[]::new));
                        Cursor<Api> cursor = repository.find(objectFilter);
                        return Flux.fromIterable(cursor.toList());
                    }
                });
    }

    @Override
    public Flux<Api> allByApiIds(List<String> apiIds) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Object[] id = apiIds.toArray();
        Cursor<Api> cursor = repository.find(in("id", id));
        return Flux.fromIterable(cursor);
    }

    @Override
    public Mono<Api> add(Api entity) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        entity.generateToken();
        entity.setCreated(Instant.now());
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    @Override
    public Mono<Api> update(Api entity, String apiName) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);

        return get(apiName)
                .flatMap(api -> {
                    api.setName(entity.getName());
                    api.setCodeRepositoryUrl(entity.getCodeRepositoryUrl());

                    Api updateValues = repository.getById(repository.update(eq("name", apiName), api).iterator().next());
                    updateValues.clearToken();
                    return Mono.just(updateValues);
                });
    }

    @Override
    public Mono<Api> get(String apiName) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        return Mono.justOrEmpty(
                repository
                        .find(eq("name", apiName))
                        .firstOrDefault()
        );
    }

    @Override
    public Mono<Api> getById(String apiId) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        return Mono.justOrEmpty(
                repository
                        .find(eq("id", apiId))
                        .firstOrDefault()
        );
    }

    @Override
    public Mono<Boolean> delete(String apiName) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        return Mono.justOrEmpty(repository.remove(eq("name", apiName)).getAffectedCount() > 0);
    }

    @Override
    public Mono<Long> numberOfApis() {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Cursor<Api> cursor = repository.find();
        return Mono.justOrEmpty((long) cursor.totalCount());
    }

    /* AV */

    @Override
    public Mono<ApiVersion> getLatestApiVersion(String apiId) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Mono.justOrEmpty(repository.find(
                ObjectFilters.eq("apiId", apiId),
                FindOptions.sort("created", SortOrder.Descending)
        ).firstOrDefault());
    }

    @Override
    public Mono<Boolean> deleteApiVersion(String apiName, String apiVersion) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Mono.just(repository.remove(eq("id", IApiVersion.createId(apiName, apiVersion))).getAffectedCount() > 0);
    }

    @Override
    public Mono<ApiVersion> getApiVersion(String apiId, String apiVersion) {

        if ("latest".equals(apiVersion)) {
            return getLatestApiVersion(apiId);
        }

        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Mono.justOrEmpty(repository.find(
                eq("id", IApiVersion.createId(apiId, apiVersion))
        ).firstOrDefault());
    }

    @Override
    public Flux<ApiVersion> allApiVersions(String apiId) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        Cursor<ApiVersion> cursor = repository.find(
                eq("apiId", apiId)
        );
        return Flux.fromIterable(cursor);
    }

    @Override
    public Mono<ApiVersion> addApiVersion(ApiVersion entity) {
        entity.setCreated(Instant.now());

        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    /* OB */

    public Mono<List<ObjectFilter>> queryFilters(List<Filter> filters) {
        HashMap<String, ArrayList<ObjectFilter>> tmp = new HashMap<>();

        filters
                .stream()
                .filter(e -> e.type() == Filter.TYPE.NAME)
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

        return Mono.just(objectFilters);
    }
}
