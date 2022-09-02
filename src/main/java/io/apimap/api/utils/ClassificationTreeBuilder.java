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

package io.apimap.api.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.repository.generic.ClassificationCollection;
import io.apimap.api.repository.interfaces.*;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.service.context.ClassificationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;

public class ClassificationTreeBuilder {
    final protected IClassificationRepository classificationRepository;
    final protected IApiRepository apiRepository;
    final protected IMetadataRepository metadataRepository;
    final protected ITaxonomyRepository taxonomyRepository;

    @SuppressFBWarnings
    public ClassificationTreeBuilder(final IClassificationRepository classificationRepository,
                                     final IApiRepository apiRepository,
                                     final IMetadataRepository metadataRepository,
                                     final ITaxonomyRepository taxonomyRepository) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
        this.metadataRepository = metadataRepository;
        this.taxonomyRepository = taxonomyRepository;
    }

    public Mono<List<ClassificationCollection>> build(ClassificationContext context, final List<reactor.util.function.Tuple3<IApi, IMetadata, IApiVersion>> apis) {
        return Flux.fromIterable(apis)
                .flatMap(api -> classificationRepository
                        .all(api.getT1().getId())
                        .flatMap(classification -> taxonomyRepository
                                .getTaxonomyCollectionVersionURN(((IApiClassification) classification).getTaxonomyUrn(), ((IApiClassification) classification).getTaxonomyVersion(), TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION)
                                .flatMap(urn -> Mono.just(new ClassificationCollection((ITaxonomyCollectionVersionURN) urn, Arrays.asList(api.getT2()))))
                        )
                )
                .groupBy(collection -> ((ClassificationCollection) collection).getTaxonomy())
                .flatMap(group -> ((GroupedFlux) group)
                        .flatMap(element -> Flux.fromIterable(((ClassificationCollection) element).getItems()))
                        .collectList()
                        .flatMap(items -> Mono.just(new ClassificationCollection((ITaxonomyCollectionVersionURN) ((GroupedFlux) group).key(), (List) items)))
                )
                .flatMap(collection -> {
                    if (context.getClassificationURN() != null) {
                        return taxonomyRepository.getTaxonomyCollectionVersionURN(context.getClassificationURN(), ((ClassificationCollection) collection).getTaxonomy().getVersion(), TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                                .map(taxonomy -> Tuples.of(taxonomy, collection));
                    } else {
                        return Mono.just(Tuples.of(new Object(), collection));
                    }
                })
                .filter(tuple -> {
                    if (context.getClassificationURN() != null) {
                        return ((ClassificationCollection) ((Tuple2) tuple).getT2()).getTaxonomy().getUrl().startsWith(
                                ((ITaxonomyCollectionVersionURN) ((Tuple2) tuple).getT1()).getUrl());
                    } else {
                        return true;
                    }
                })
                .flatMap(tuple -> Mono.just(((Tuple2) tuple).getT2()))
                .collectList();
    }

    /*
    TODO: Reimplements the following

                ---
                    if(taxonomyCollectionVersionURN.isPresent()){
                        Optional<TaxonomyCollectionVersionURN> finalTaxonomyCollectionVersionURN = taxonomyCollectionVersionURN;
                        if(apiCollection.getParents() != null
                                && apiCollection.getParents().stream().anyMatch(z -> finalTaxonomyCollectionVersionURN.get().getUrl().startsWith(z))) {
                            if (treeCollection.containsKey(taxonomyCollectionVersionURN.get().getId())) {
                                ClassificationTreeCollection tr = treeCollection.get(taxonomyCollectionVersionURN.get().getId());
                                tr.getItems().add(e.getMetadata().get());
                            } else {
                                if (taxonomyCollectionVersionURN.get().getId() != null) {
                                    ClassificationTreeCollection collection = new ClassificationTreeCollection();
                                    collection.setTaxonomy(taxonomyCollectionVersionURN.get());

                                    ArrayList<Metadata> items = new ArrayList<>();
                                    if (e.getMetadata().isPresent()) {
                                        items.add(e.getMetadata().get());
                                    }

                                    collection.setItems(items);

                                    treeCollection.put(taxonomyCollectionVersionURN.get().getId(), collection);
                                }
                            }
                        }
                    }
     */
}
