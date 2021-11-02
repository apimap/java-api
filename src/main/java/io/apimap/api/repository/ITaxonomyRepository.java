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

package io.apimap.api.repository;

import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionURNCollection;

import java.util.Optional;

public interface ITaxonomyRepository {
    Integer numberOfTaxonomies();
    void clearTaxonomyCollection();
    TaxonomyCollectionCollection allTaxonomyCollection();
    TaxonomyCollectionVersionCollection allTaxonomyCollectionVersion(String nid);
    Optional<TaxonomyCollection> addTaxonomyCollection(TaxonomyCollection entity);
    Optional<TaxonomyCollection> getTaxonomyCollection(String nid);
    Optional<TaxonomyCollection> getTaxonomyCollection(String nid, Boolean returnWithToken);
    void deleteTaxonomyCollection(String nid);
    void clearTaxonomyCollectionVersion();
    TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURN(String taxonomyCollectionVersion);
    Optional<TaxonomyCollectionVersion> addTaxonomyCollectionVersion(TaxonomyCollectionVersion entity);
    Optional<TaxonomyCollectionVersion> getLatestTaxonomyCollectionVersion(String nid);
    Optional<TaxonomyCollectionVersion> getTaxonomyCollectionVersion(String nid, String version);
    void deleteTaxonomyCollectionVersion(String nid, String version);
    void deleteTaxonomyCollectionVersions(String nid);
    TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURNCollection(String taxonomyCollectionVersion, String nid, String version);
    TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURNsBellowUrl(String taxonomyCollectionVersion, String url);
    void clearTaxonomyCollectionVersionURN();
    Optional<TaxonomyCollectionVersionURN> addTaxonomyCollectionVersionURN(TaxonomyCollectionVersionURN entity);
    Optional<TaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String urn, String taxonomyVersion);
    void deleteTaxonomyCollectionVersionURN(String urn, String taxonomyVersion);
    void deleteTaxonomyCollectionVersionURNs(String nid, String version);
    void deleteTaxonomyCollectionVersionURNs(String nid);
    Optional<TaxonomyCollectionVersionURN> update(TaxonomyCollectionVersionURN entity, String urn, String taxonomyVersion);
}
