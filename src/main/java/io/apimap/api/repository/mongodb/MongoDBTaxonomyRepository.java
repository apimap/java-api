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

package io.apimap.api.repository.mongodb;

import io.apimap.api.repository.mongodb.documents.TaxonomyCollection;
import io.apimap.api.repository.mongodb.documents.TaxonomyCollectionVersion;
import io.apimap.api.repository.mongodb.documents.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Objects;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBTaxonomyRepository extends MongoDBRepository implements ITaxonomyRepository<TaxonomyCollection, TaxonomyCollectionVersion, TaxonomyCollectionVersionURN> {

    public MongoDBTaxonomyRepository(ReactiveMongoTemplate template) {
        super(template);
    }

    @Override
    public Mono<Long> numberOfTaxonomies() {
        return template
                .estimatedCount(TaxonomyCollection.class);
    }

    /* TC */

    @Override
    public Flux<TaxonomyCollection> allTaxonomyCollection() {
        return template
                .findAll(TaxonomyCollection.class);
    }

    @Override
    public Mono<TaxonomyCollection> addTaxonomyCollection(TaxonomyCollection entity) {
        entity.generateToken();
        entity.setCreated(new Date());

        return getTaxonomyCollection(entity.getNid())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The Taxonomy '" + entity.getName() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<TaxonomyCollection> getTaxonomyCollection(String nid) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        return template.findOne(query, TaxonomyCollection.class);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollection(String nid) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        return template
                .remove(query, TaxonomyCollection.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    /* TCV */

    @Override
    public Flux<TaxonomyCollectionVersion> allTaxonomyCollectionVersion(String nid) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid)).with(Sort.by(Sort.Direction.ASC, "created"));
        return template.find(query, TaxonomyCollectionVersion.class);
    }

    @Override
    public Mono<TaxonomyCollectionVersion> addTaxonomyCollectionVersion(TaxonomyCollectionVersion entity) {
        entity.setCreated(new Date());

        return getTaxonomyCollectionVersion(entity.getNid(), entity.getVersion())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The Taxonomy Version '" + entity.getVersion() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<TaxonomyCollectionVersion> getTaxonomyCollectionVersion(String nid, String version) {
        final Query query;

        if ("latest".equals(version)) {
            query = new Query().addCriteria(Criteria.where("nid").is(nid)).with(Sort.by(Sort.Direction.ASC, "created"));
        } else {
            query = new Query().addCriteria(Criteria.where("nid").is(nid));
            query.addCriteria(Criteria.where("version").is(version));
        }

        return template.findOne(query, TaxonomyCollectionVersion.class);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersion(String nid, String version) {
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersions(String nid) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        return template
                .remove(query, TaxonomyCollectionVersion.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    /* TCVU */

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURN(String taxonomyCollectionVersion) {
        final Query query = new Query().addCriteria(Criteria.where("taxonomyVersion").is(taxonomyCollectionVersion));
        return template
                .find(query, TaxonomyCollectionVersionURN.class);
    }

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNCollection(String nid, String version) {
        if ("latest".equals(version)) {
            return allTaxonomyCollectionVersion(nid)
                    .take(1, true)
                    .flatMap(latest -> {
                        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
                        query.addCriteria(Criteria.where("version").is(latest.getVersion()));
                        return template.find(query, TaxonomyCollectionVersionURN.class);
                    });
        }

        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        query.addCriteria(Criteria.where("version").is(version));

        return template.find(query, TaxonomyCollectionVersionURN.class);
    }

    @Override
    public Flux<TaxonomyCollectionVersionURN> allTaxonomyCollectionVersionURNsBellowUrl(String url) {
        final Query query = new Query().addCriteria(Criteria.where("url").regex("^(" + url + ").*"));
        return template.find(query, TaxonomyCollectionVersionURN.class);
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> addTaxonomyCollectionVersionURN(TaxonomyCollectionVersionURN entity) {
        entity.setCreated(new Date());

        return getTaxonomyCollectionVersionURN(entity.getUrn(), entity.getVersion(), TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The Classification '" + entity.getUrn() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> getTaxonomyCollectionVersionURN(String urn, String taxonomyVersion, TaxonomyDataRestEntity.ReferenceType type) {
        final Query query;

        if ("latest".equals(taxonomyVersion)) {
            query = new Query().addCriteria(Criteria.where("id").is(urn + "#" + taxonomyVersion)).with(Sort.by(Sort.Direction.ASC, "created"));
        } else {
            query = new Query().addCriteria(Criteria.where("id").is(urn + "#" + taxonomyVersion));
        }

        if (!type.getValue().equals(TaxonomyDataRestEntity.ReferenceType.UNKNOWN.getValue())) {
            query.addCriteria(Criteria.where("type").is(type.getValue()));
        }

        return template.findOne(query, TaxonomyCollectionVersionURN.class);
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURN(String urn, String taxonomyVersion) {
        final Query query = new Query().addCriteria(Criteria.where("id").is(urn + "#" + taxonomyVersion));
        return template
                .remove(query, TaxonomyCollectionVersionURN.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid, String version) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        query.addCriteria(Criteria.where("version").is(version));
        return template
                .remove(query, TaxonomyCollectionVersionURN.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Boolean> deleteTaxonomyCollectionVersionURNs(String nid) {
        final Query query = new Query().addCriteria(Criteria.where("nid").is(nid));
        return template
                .remove(query, TaxonomyCollectionVersionURN.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<TaxonomyCollectionVersionURN> update(TaxonomyCollectionVersionURN entity, String urn, String taxonomyVersion) {
        final Query query = new Query().addCriteria(Criteria.where("id").is(urn + "#" + taxonomyVersion));
        return getTaxonomyCollectionVersionURN(urn, taxonomyVersion, TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                .filter(Objects::nonNull)
                .flatMap(versionURN -> {
                    Update update = new Update();
                    update.set("title", entity.getTitle());
                    update.set("description", entity.getDescription());
                    update.set("type", entity.getType());
                    return template.findAndModify(query, update, TaxonomyCollectionVersionURN.class);
                });

    }
}
