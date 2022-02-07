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
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.query.MetadataQueryFilter;
import io.apimap.api.repository.nitrite.entity.query.QueryFilter;
import io.apimap.api.repository.nitrite.entity.support.MetadataCollection;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toCollection;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.or;

@Repository
public class NitriteMetadataRepository extends NitriteRepository implements IMetadataRepository {
    public NitriteMetadataRepository(NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "metadata");
    }

    public void clear() {
        database.getRepository(Metadata.class).remove(ObjectFilters.ALL);
    }

    public MetadataCollection filteredCollection(List<ObjectFilter> filters) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        ObjectFilter objectFilter = and(filters.toArray(ObjectFilter[]::new));
        Cursor<Metadata> cursor = repository.find(objectFilter);
        return new MetadataCollection(cursor.toList());
    }

    public MetadataCollection all(String apiId) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        Cursor<Metadata> cursor = repository.find(eq("apiId", apiId));
        return new MetadataCollection(cursor.toList());
    }

    @Override
    public MetadataCollection all() {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        Cursor<Metadata> cursor = repository.find();
        return new MetadataCollection(cursor.toList());
    }

    public Optional<Metadata> add(Metadata entity) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Optional<Metadata> update(Metadata entity) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Optional.ofNullable(repository.getById(repository.update(
                eq("id", entity.getId())
                , entity
                , true
        ).iterator().next()));
    }

    public Optional<Metadata> get(String apiId, String version) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        return Optional.ofNullable(repository.find(
                and(eq("apiId", apiId), eq("apiVersion", version))
        ).firstOrDefault());
    }

    public void delete(String apiId) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        repository.remove(eq("apiId", apiId));
    }

    public void delete(String apiId, String version) {
        ObjectRepository<Metadata> repository = database.getRepository(Metadata.class);
        repository.remove(
                and(eq("apiId", apiId), eq("apiVersion", version))
        );
    }

    public MetadataCollection queryFilters(List<QueryFilter> filters) {
        HashMap<String, ArrayList<ObjectFilter>> tmp = new HashMap<>();

        filters
                .stream()
                .filter(e -> e instanceof MetadataQueryFilter)
                .forEach(e -> {
                    String key = e.getKey();
                    ArrayList<ObjectFilter> f = tmp.getOrDefault(key, new ArrayList<>());
                    f.add(e.equalsObjectFilter());
                    tmp.put(key, f);
                });

        ArrayList<ObjectFilter> objectFilters = tmp
                .values()
                .stream()
                .map(objectFilterArrayList -> or(objectFilterArrayList.toArray(ObjectFilter[]::new)))
                .collect(toCollection(ArrayList::new));

        if (!objectFilters.isEmpty()) {
            return filteredCollection(objectFilters);
        }

        return null;
    }
}
