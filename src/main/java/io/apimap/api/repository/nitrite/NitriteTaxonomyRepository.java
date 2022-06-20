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
import io.apimap.api.repository.entities.ITaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entities.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.regex;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.NitriteConfiguration.class)
public class NitriteTaxonomyRepository extends NitriteRepository implements ITaxonomyRepository<TaxonomyCollection, TaxonomyCollectionVersion, TaxonomyCollectionVersionURN> {

    public NitriteTaxonomyRepository(NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "taxonomy");
    }

    @Override
    public Mono<Long> numberOfTaxonomies() {
        if (database == null) {
            return Mono.just(Long.valueOf(0));
        }
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        Cursor<TaxonomyCollection> cursor = repository.find();
        return Mono.just(Long.valueOf(cursor.totalCount()));
    }

    /* TC */

    @Override
    public Flux<TaxonomyCollection> allTaxonomyCollection() {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        Cursor<TaxonomyCollection> cursor = repository.find();
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Mono<TaxonomyCollection> addTaxonomyCollection(TaxonomyCollection entity) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        entity.generateToken();
        entity.setCreated(new Date());

        return getTaxonomyCollection(entity.getNid())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The Taxonomy '" + entity.getName() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next())))
                );
    }

    @Override
    public Mono<TaxonomyCollection> getTaxonomyCollection(String nid) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        return Mono.justOrEmpty(repository.find((eq("nid", nid))).firstOrDefault());
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollection(String nid) {
        ObjectRepository<TaxonomyCollection> repository = database.getRepository(TaxonomyCollection.class);
        return Mono.justOrEmpty(repository.remove(eq("nid", nid)).getAffectedCount() > 0);
    }

    /* TCV */

    @Override
    public Flux<TaxonomyCollectionVersion> allTaxonomyCollectionVersion(String nid) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        Cursor<TaxonomyCollectionVersion> cursor = repository.find(eq("nid", nid));
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Mono<TaxonomyCollectionVersion> addTaxonomyCollectionVersion(TaxonomyCollectionVersion entity) {
        entity.setCreated(new Date());

        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    @Override
    public Mono<TaxonomyCollectionVersion> getTaxonomyCollectionVersion(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);

        if ("latest".equals(version)) {
            return Mono.justOrEmpty(repository.find(
                    ObjectFilters.eq("nid", nid),
                    FindOptions.sort("created", SortOrder.Ascending)
            ).firstOrDefault());
        } else {
            return Mono.justOrEmpty(repository.find(
                    and(eq("nid", nid), eq("version", version))
            ).firstOrDefault());
        }
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersion(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Mono.just(repository.remove(
                and(eq("nid", nid), eq("version", version))
        ).getAffectedCount() > 0);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersions(String nid) {
        ObjectRepository<TaxonomyCollectionVersion> repository = database.getRepository(TaxonomyCollectionVersion.class);
        return Mono.justOrEmpty(repository.remove(eq("nid", nid)).getAffectedCount() > 0);
    }

    /* TCVU */

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURN(String taxonomyCollectionVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(eq("taxonomyVersion", taxonomyCollectionVersion));
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNCollection(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        if ("latest".equals(version)) {
            return getTaxonomyCollectionVersion(nid, version)
                    .flatMapMany(cv -> {
                        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(
                                and(eq("nid", nid), eq("version", cv.getVersion()))
                        );
                        return Flux.fromIterable(cursor.toList());
                    });
        }

        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(
                and(eq("nid", nid), eq("version", version))
        );
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNsBellowUrl(String url) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        Cursor<TaxonomyCollectionVersionURN> cursor = repository.find(regex("url", "^(" + url + ").*"));
        return Flux.fromIterable(cursor.toList());
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String urn, String taxonomyVersion, TaxonomyDataRestEntity.ReferenceType type) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);

        if ("latest".equals(taxonomyVersion)) {
            return Mono.justOrEmpty(repository.find(
                    and(
                        eq("id", ITaxonomyCollectionVersionURN.createId(urn, taxonomyVersion)),
                        eq("type", type.getValue())
                    ),
                    FindOptions.sort("created", SortOrder.Ascending)
            ).firstOrDefault());
        }


        if (!type.getValue().equals(TaxonomyDataRestEntity.ReferenceType.UNKNOWN.getValue())) {
            return Mono.justOrEmpty(repository.find(and(
                    eq("id", ITaxonomyCollectionVersionURN.createId(urn, taxonomyVersion)),
                    eq("type", type.getValue()))).firstOrDefault());
        }

        return Mono.justOrEmpty(repository.find(and(
                eq("id", ITaxonomyCollectionVersionURN.createId(urn, taxonomyVersion)))).firstOrDefault());
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> addTaxonomyCollectionVersionURN(TaxonomyCollectionVersionURN entity) {
        entity.setCreated(new Date());

        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURN(String urn, String taxonomyVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Mono.justOrEmpty(repository.remove(eq("id", ITaxonomyCollectionVersionURN.createId(urn, taxonomyVersion))).getAffectedCount() > 0);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid, String version) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Mono.justOrEmpty(repository.remove(and(
                eq("nid", nid), eq("version", version)
        )).getAffectedCount() > 0);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Mono.just(repository.remove(eq("nid", nid)).getAffectedCount() > 0);
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> update(TaxonomyCollectionVersionURN entity, String urn, String taxonomyVersion) {
        ObjectRepository<TaxonomyCollectionVersionURN> repository = database.getRepository(TaxonomyCollectionVersionURN.class);
        return Mono.justOrEmpty(repository.getById(repository.update(eq("id", ITaxonomyCollectionVersionURN.createId(urn, taxonomyVersion)), entity).iterator().next()));
    }
}
