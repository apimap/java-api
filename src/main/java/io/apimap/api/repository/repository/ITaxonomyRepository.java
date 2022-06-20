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

package io.apimap.api.repository.repository;

import io.apimap.api.repository.entities.ITaxonomyCollection;
import io.apimap.api.repository.entities.ITaxonomyCollectionVersion;
import io.apimap.api.repository.entities.ITaxonomyCollectionVersionURN;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITaxonomyRepository<TITaxonomyCollection extends ITaxonomyCollection,
                                     TITaxonomyCollectionVersion extends ITaxonomyCollectionVersion,
                                     TITaxonomyCollectionVersionURN extends ITaxonomyCollectionVersionURN> {
    String DEFAULT_TAXONOMY_TYPE = "classification";

    Mono<Long> numberOfTaxonomies();

    /* TC */
    Flux<TITaxonomyCollection> allTaxonomyCollection();

    Mono<TITaxonomyCollection> addTaxonomyCollection(TITaxonomyCollection entity);

    Mono<TITaxonomyCollection> getTaxonomyCollection(String nid);

    Mono<Boolean> deleteTaxonomyCollection(String nid);

    /* TCV */
    Mono<Boolean> deleteTaxonomyCollectionVersions(String nid);

    Mono<Boolean> deleteTaxonomyCollectionVersion(String nid, String version);

    Flux<TITaxonomyCollectionVersion> allTaxonomyCollectionVersion(String nid);

    Mono<TITaxonomyCollectionVersion> addTaxonomyCollectionVersion(TITaxonomyCollectionVersion entity);

    Mono<TITaxonomyCollectionVersion> getTaxonomyCollectionVersion(String nid, String version);

    /* TCVU */
    Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid);

    Mono<Boolean> deleteTaxonomyCollectionVersionURN(String urn, String taxonomyVersion);

    Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid, String version);

    Flux<TITaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURN(String taxonomyCollectionVersion);

    Flux<TITaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNCollection(String nid, String version);

    Flux<TITaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNsBellowUrl(String url);

    Mono<TITaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String urn, String taxonomyVersion, TaxonomyDataRestEntity.ReferenceType type);

    Mono<TITaxonomyCollectionVersionURN> addTaxonomyCollectionVersionURN(TITaxonomyCollectionVersionURN entity);

    Mono<TITaxonomyCollectionVersionURN> update(TITaxonomyCollectionVersionURN entity, String urn, String taxonomyVersion);
}
