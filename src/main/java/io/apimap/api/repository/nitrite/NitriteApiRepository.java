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
import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiClassification;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.query.QueryFilter;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ApiVersionCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationCollection;
import io.apimap.api.repository.nitrite.entity.support.MetadataCollection;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.in;

@Repository
public class NitriteApiRepository extends NitriteRepository implements IApiRepository {
    protected NitriteMetadataRepository nitriteMetadataRepository;
    protected NitriteClassificationRepository nitriteClassificationRepository;

    public NitriteApiRepository(NitriteConfiguration nitriteConfiguration,
                                NitriteMetadataRepository nitriteMetadataRepository,
                                NitriteClassificationRepository nitriteClassificationRepository) {
        super(nitriteConfiguration, "api");
        this.nitriteMetadataRepository = nitriteMetadataRepository;
        this.nitriteClassificationRepository = nitriteClassificationRepository;
    }

    /*
    Api
     */

    public ApiCollection all() {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Cursor<Api> cursor = repository.find();

        return new ApiCollection(cursor.toList().stream().map(api -> {

            Optional<ApiVersion> apiVersion = this.getLatestApiVersion(api.getId());
            Optional<Metadata> metadata = Optional.empty();

            if (apiVersion.isPresent()) {
                metadata = this.nitriteMetadataRepository.get(api.getId(), apiVersion.get().getVersion());
            }

            return new ApiCollection.Item(api,
                    apiVersion,
                    metadata
            );
        }).collect(Collectors.toList()), null);
    }

    public ApiCollection all(List<QueryFilter> filters) {
        MetadataCollection metadataCollection = nitriteMetadataRepository.queryFilters(filters);
        ClassificationCollection classificationCollection = nitriteClassificationRepository.queryFilters(filters);

        // Combine elements
        ArrayList<String> apiIds = new ArrayList<>();

        if (metadataCollection != null && classificationCollection == null) {
            apiIds.addAll(metadataCollection.getItems().stream().map(Metadata::getApiId).distinct().collect(Collectors.toList()));
        }

        if (metadataCollection == null && classificationCollection != null) {
            apiIds.addAll(
                    classificationCollection
                            .getItems()
                            .stream()
                            .map(ApiClassification::getApiId)
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        if (metadataCollection != null && classificationCollection != null) {
            List<String> names = metadataCollection.getItems().stream().map(Metadata::getApiId).distinct().collect(Collectors.toList());

            classificationCollection.getItems().forEach(e -> {
                if (names.contains(e.getApiId())) {
                    apiIds.add(e.getApiId());
                }
            });
        }

        if (apiIds.isEmpty()) {
            return new ApiCollection(Collections.emptyList(), classificationCollection != null ? classificationCollection.getParents() : null);
        }

        return filteredCollection(apiIds, classificationCollection != null ? classificationCollection.getParents() : null);
    }

    public ApiCollection filteredCollection(List<String> ids, List<String> parents) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Object[] id = ids.toArray();
        Cursor<Api> cursor = repository.find(in("id", id));

        return new ApiCollection(cursor.toList().stream().map(api -> {
            Optional<ApiVersion> apiVersion = this.getLatestApiVersion(api.getId());
            Optional<Metadata> metadata = Optional.empty();

            if (apiVersion.isPresent()) {
                metadata = this.nitriteMetadataRepository.get(api.getId(), apiVersion.get().getVersion());
            }

            return new ApiCollection.Item(api,
                    apiVersion,
                    metadata
            );
        }).collect(Collectors.toList()), parents);
    }

    public Optional<Api> add(Api entity) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        entity.generateToken();
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Optional<Api> update(Api entity, String apiName) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);

        // Override static values
        Optional<Api> returnvalue = Optional.empty();
        Optional<Api> existingEntity = get(apiName, true);

        if (existingEntity.isPresent()) {
            existingEntity.get().setName(entity.getName());
            existingEntity.get().setCodeRepositoryUrl(entity.getCodeRepositoryUrl());

            returnvalue = Optional.ofNullable(repository.getById(repository.update(eq("name", apiName), existingEntity.get()).iterator().next()));
            returnvalue.ifPresent(Api::clearToken);
        }

        return returnvalue;
    }

    public Optional<Api> get(String apiName) {
        return get(apiName, false);
    }

    public String apiId(String apiName) {
        Optional<Api> api = get(apiName);
        return api.map(Api::getId).orElse(null);
    }

    public Optional<Api> get(String apiName, Boolean returnWithToken) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Optional<Api> returnvalue = Optional.ofNullable(repository.find(
                eq("name", apiName)
        ).firstOrDefault());

        if (Boolean.FALSE.equals(returnWithToken)) {
            returnvalue.ifPresent(Api::clearToken);
        }

        return returnvalue;
    }

    public void delete(String apiName) {
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        repository.remove(eq("name", apiName));
    }

    public Integer numberOfApis() {
        if (database == null) {
            return 0;
        }
        ObjectRepository<Api> repository = database.getRepository(Api.class);
        Cursor<Api> cursor = repository.find();
        return cursor.totalCount();
    }

    public Optional<ApiVersion> getLatestApiVersion(String apiId) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Optional.ofNullable(repository.find(
                ObjectFilters.eq("apiId", apiId),
                FindOptions.sort("created", SortOrder.Descending)
        ).firstOrDefault());
    }

    /*
    ApiVersion
     */

    public void deleteApiVersion(String apiName, String apiVersion) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        repository.remove(eq("id", ApiVersion.createId(apiName, apiVersion)));
    }

    public Optional<ApiVersion> getApiVersion(String apiId, String apiVersion) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Optional.ofNullable(repository.find(
                eq("id", ApiVersion.createId(apiId, apiVersion))
        ).firstOrDefault());
    }

    public ApiVersionCollection allApiVersions(String apiId) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        Cursor<ApiVersion> cursor = repository.find(
                eq("apiId", apiId)
        );
        return new ApiVersionCollection(cursor.toList());
    }

    public Optional<ApiVersion> addApiVersion(ApiVersion entity) {
        ObjectRepository<ApiVersion> repository = database.getRepository(ApiVersion.class);
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }
}
