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
import io.apimap.api.repository.IClassificationRepository;
import io.apimap.api.repository.nitrite.entity.db.ApiClassification;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entity.query.ClassificationQueryFilter;
import io.apimap.api.repository.nitrite.entity.query.QueryFilter;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationTreeCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionURNCollection;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toCollection;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.or;

@Repository
public class NitriteClassificationRepository extends NitriteRepository implements IClassificationRepository {
    protected NitriteTaxonomyRepository nitriteTaxonomyRepository;

    public NitriteClassificationRepository(NitriteConfiguration nitriteConfiguration,
                                           NitriteTaxonomyRepository nitriteTaxonomyRepository) {
        super(nitriteConfiguration, "classification");
        this.nitriteTaxonomyRepository = nitriteTaxonomyRepository;
    }

    public void clear() {
        database.getRepository(ApiClassification.class).remove(ObjectFilters.ALL);
    }

    public ClassificationCollection filteredCollection(List<ObjectFilter> filters) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        ObjectFilter combinedFilter = or(filters.toArray(ObjectFilter[]::new));
        Cursor<ApiClassification> cursor = repository.find(combinedFilter);
        return new ClassificationCollection(cursor.toList(), "1");
    }

    public ClassificationCollection all() {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find();
        return new ClassificationCollection(cursor.toList(), "1");
    }

    public ClassificationCollection all(String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(eq("apiId", apiId));
        return new ClassificationCollection(cursor.toList(), "1");
    }

    public ClassificationCollection all(String apiId, String apiVersion) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        );
        return new ClassificationCollection(cursor.toList(), "1");
    }

    public Optional<ApiClassification> update(ApiClassification entity, String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Optional.ofNullable(repository.getById(repository.update(eq("apiId", apiId), entity).iterator().next()));
    }

    public Optional<ApiClassification> add(ApiClassification entity) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public ClassificationCollection add(String apiId, List<ApiClassification> entities) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        repository.insert(entities.toArray(new ApiClassification[entities.size()]));
        return get(apiId);
    }

    public Optional<ApiClassification> get(String uri, String api) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        return Optional.ofNullable(repository.find(eq("id", api + "#" + uri)
        ).firstOrDefault());
    }

    public ClassificationCollection get(String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        Cursor<ApiClassification> cursor = repository.find(eq("apiId", apiId));
        return new ClassificationCollection(cursor.toList(), "1");
    }

    public void delete(String apiId, String apiVersion) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        repository.remove(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        );
    }

    public void delete(String apiId) {
        ObjectRepository<ApiClassification> repository = database.getRepository(ApiClassification.class);
        repository.remove(eq("apiId", apiId));
    }

    public ClassificationCollection queryFilters(List<QueryFilter> filters) {
        ArrayList<String> classificationFilters = filters
                .stream()
                .filter(e -> e instanceof ClassificationQueryFilter)
                .map(e -> {
                    Optional<TaxonomyCollectionVersionURN> data = nitriteTaxonomyRepository.getTaxonomyCollectionVersionURN(((ClassificationQueryFilter) e).getValue(), "1");
                    return data.map(TaxonomyCollectionVersionURN::getUrl).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(toCollection(ArrayList::new));

        ArrayList<ObjectFilter> urns = new ArrayList<>();

        for (String ff : classificationFilters) {
            TaxonomyCollectionVersionURNCollection f = nitriteTaxonomyRepository.allTaxonomyCollectionVersionURNsBellowUrl("1", ff);
            if (f != null) {
                for (TaxonomyCollectionVersionURN item : f.getItems()) {
                    if (item.getUrn() != null) {
                        urns.add(eq("taxonomyUrn", item.getUrn()));
                    }
                }
            }
        }

        if (!urns.isEmpty()) {
            return filteredCollection(urns);
        }

        return null;
    }

    public List<ClassificationTreeCollection> classificationTree(ApiCollection apiCollection, String parentClassificationURN) {
        HashMap<String, ClassificationTreeCollection> treeCollection = new HashMap<>();

        final Optional<TaxonomyCollectionVersionURN> parentTaxonomyCollectionVersionURN;

        if (parentClassificationURN != null) {
            parentTaxonomyCollectionVersionURN = nitriteTaxonomyRepository.getTaxonomyCollectionVersionURN(parentClassificationURN, "1");
        } else {
            parentTaxonomyCollectionVersionURN = Optional.empty();
        }

        apiCollection.getItems().forEach(e -> {
            ClassificationCollection classification = get(e.getApi().getId());

            classification.getItems().forEach(f -> {
                Optional<TaxonomyCollectionVersionURN> taxonomyCollectionVersionURN = nitriteTaxonomyRepository.getTaxonomyCollectionVersionURN(f.getTaxonomyUrn(), f.getTaxonomyVersion());

                if (parentTaxonomyCollectionVersionURN.isPresent()) {
                    if (taxonomyCollectionVersionURN.get().getUrl().startsWith(parentTaxonomyCollectionVersionURN.get().getUrl())) {
                        if (treeCollection.containsKey(taxonomyCollectionVersionURN.get().getId())) {
                            ClassificationTreeCollection tr = treeCollection.get(taxonomyCollectionVersionURN.get().getId());
                            tr.getItems().add(e.getMetadata().get());
                        } else {
                            ClassificationTreeCollection collection = new ClassificationTreeCollection();
                            collection.setTaxonomy(taxonomyCollectionVersionURN.get());

                            ArrayList<Metadata> items = new ArrayList<>();
                            items.add(e.getMetadata().get());

                            collection.setItems(items);

                            treeCollection.put(taxonomyCollectionVersionURN.get().getId(), collection);
                        }
                    }
                } else {
                    if (taxonomyCollectionVersionURN.isPresent() && treeCollection.containsKey(taxonomyCollectionVersionURN.get().getId())) {
                        ClassificationTreeCollection tr = treeCollection.get(taxonomyCollectionVersionURN.get().getId());
                        tr.getItems().add(e.getMetadata().get());
                    } else {
                        if(taxonomyCollectionVersionURN.isPresent() && taxonomyCollectionVersionURN.get().getId() != null) {
                            ClassificationTreeCollection collection = new ClassificationTreeCollection();
                            collection.setTaxonomy(taxonomyCollectionVersionURN.get());

                            ArrayList<Metadata> items = new ArrayList<>();
                            if(e.getMetadata().isPresent()) {
                                items.add(e.getMetadata().get());
                            }

                            collection.setItems(items);

                            treeCollection.put(taxonomyCollectionVersionURN.get().getId(), collection);
                        }
                    }
                }
            });
        });

        return new ArrayList<>(treeCollection.values());
    }
}
