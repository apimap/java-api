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
import io.apimap.api.repository.ITaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionURNCollection;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.regex;

@Repository
public class NitriteTaxonomyRepository extends NitriteRepository implements ITaxonomyRepository {

    public NitriteTaxonomyRepository(NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "taxonomy");
    }

    public Integer numberOfTaxonomies() {
        if (database == null) {
            return 0;
        }
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        Cursor<TaxonomyCollection> cursor = repository.find();
        return cursor.totalCount();
    }

    /*
    TaxonomyCollection
     */

    public void clearTaxonomyCollection() {
        database.getRepository(TaxonomyCollection.class).remove(ObjectFilters.ALL);
    }

    public TaxonomyCollectionCollection allTaxonomyCollection() {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        Cursor<TaxonomyCollection> cursor = repository.find();
        return new TaxonomyCollectionCollection(cursor.toList());
    }

    public TaxonomyCollectionVersionCollection allTaxonomyCollectionVersion(String nid) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        Cursor<TaxonomyCollectionVersion> cursor = repository.find(eq("nid", nid));
        return new TaxonomyCollectionVersionCollection(cursor.toList());
    }

    public Optional<TaxonomyCollection> addTaxonomyCollection(TaxonomyCollection entity) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        entity.generateToken();
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Optional<TaxonomyCollection> getTaxonomyCollection(String nid) {
        return getTaxonomyCollection(nid, false);
    }

    public Optional<TaxonomyCollection> getTaxonomyCollection(String nid, Boolean returnWithToken) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        Optional<TaxonomyCollection> returnvalue = Optional.ofNullable(repository.find((eq("nid", nid))).firstOrDefault());

        if (Boolean.FALSE.equals(returnWithToken)) {
            returnvalue.ifPresent(TaxonomyCollection::clearToken);
        }

        return returnvalue;
    }

    public void deleteTaxonomyCollection(String nid) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        repository.remove(eq("nid", nid));
    }

    /*
    TaxonomyCollectionVersion
     */

    public void clearTaxonomyCollectionVersion() {
        database.getRepository(TaxonomyCollectionVersion.class).remove(ObjectFilters.ALL);
    }

    public TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURN(String taxonomyCollectionVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(eq("taxonomyVersion", taxonomyCollectionVersion));
        return new TaxonomyCollectionVersionURNCollection(cursor.toList(), taxonomyCollectionVersion);
    }

    public Optional<TaxonomyCollectionVersion> addTaxonomyCollectionVersion(TaxonomyCollectionVersion entity) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Optional<TaxonomyCollectionVersion> getLatestTaxonomyCollectionVersion(String nid) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Optional.ofNullable(repository.find(
                ObjectFilters.eq("nid", nid),
                FindOptions.sort("created", SortOrder.Ascending)
        ).firstOrDefault());
    }

    public Optional<TaxonomyCollectionVersion> getTaxonomyCollectionVersion(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Optional.ofNullable(repository.find(
                and(eq("nid", nid), eq("version", version))
        ).firstOrDefault());
    }

    public void deleteTaxonomyCollectionVersion(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        repository.remove(
                and(eq("nid", nid), eq("version", version))
        );
    }

    public void deleteTaxonomyCollectionVersions(String nid) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        repository.remove(eq("nid", nid));
    }

    /*
    TaxonomyCollectionVersionURN
     */

    public TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURNCollection(String taxonomyCollectionVersion, String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(
                and(eq("nid", nid), eq("version", version))
        );
        return new TaxonomyCollectionVersionURNCollection(cursor.toList(), taxonomyCollectionVersion);
    }

    public TaxonomyCollectionVersionURNCollection allTaxonomyCollectionVersionURNsBellowUrl(String taxonomyCollectionVersion, String url) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(regex("url", "^(" + url + ").*"));
        return new TaxonomyCollectionVersionURNCollection(cursor.toList(), taxonomyCollectionVersion);
    }

    public void clearTaxonomyCollectionVersionURN() {
        database.getRepository(TaxonomyCollectionVersionURN.class).remove(ObjectFilters.ALL);
    }

    public Optional<TaxonomyCollectionVersionURN> addTaxonomyCollectionVersionURN(TaxonomyCollectionVersionURN entity) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Optional.ofNullable(repository.getById(repository.insert(entity).iterator().next()));
    }

    public Optional<TaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String url, TaxonomyDataRestEntity.ReferenceType type) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Optional.ofNullable(repository.find(and(
                eq("url", url),
                eq("type", type.getValue()))).firstOrDefault());
    }

    public Optional<TaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String urn, String taxonomyVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Optional.ofNullable(repository.find(eq("id", urn + "#" + taxonomyVersion)).firstOrDefault());
    }

    public void deleteTaxonomyCollectionVersionURN(String urn, String taxonomyVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        repository.remove(eq("id", urn + "#" + taxonomyVersion));
    }

    public void deleteTaxonomyCollectionVersionURNs(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        repository.remove(and(
                eq("nid", nid), eq("version", version)
        ));
    }

    public void deleteTaxonomyCollectionVersionURNs(String nid) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        repository.remove(eq("nid", nid));
    }

    public Optional<TaxonomyCollectionVersionURN> update(TaxonomyCollectionVersionURN entity, String urn, String taxonomyVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Optional.ofNullable(repository.getById(repository.update(eq("id", urn + "#" + taxonomyVersion), entity).iterator().next()));
    }
}
