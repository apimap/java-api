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

import com.mongodb.client.model.Filters;
import io.apimap.api.repository.mongodb.documents.ApiClassification;
import io.apimap.api.repository.mongodb.documents.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.service.query.Filter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
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
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.or;
import static io.apimap.api.repository.nitrite.NitriteClassificationRepository.TAXONOMY_VERSION;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBClassificationRepository extends MongoDBRepository implements IClassificationRepository<ApiClassification, Bson> {

    final protected MongoDBTaxonomyRepository taxonomyRepository;

    public MongoDBClassificationRepository(final ReactiveMongoTemplate template,
                                           final MongoDBTaxonomyRepository taxonomyRepository) {
        super(template);
        this.taxonomyRepository = taxonomyRepository;
    }

    /* C */

    @Override
    public Flux<ApiClassification> all() {
        return template
                .findAll(ApiClassification.class);
    }

    @Override
    public Flux<ApiClassification> allByURN(String taxonomyUrn) {
        final Query query = new Query().addCriteria(Criteria.where("taxonomyUrn").is(taxonomyUrn));
        return template.find(query, ApiClassification.class);
    }

    @Override
    public Flux<ApiClassification> allByFilters(Mono<List<Bson>> filters) {
        return template
                .getCollection("apiClassification")
                .flatMapMany(collection -> filters
                        .filter(Objects::nonNull)
                        .filter(list -> list.size() > 0)
                        .flatMapMany(filterList -> {
                                return collection.find(or(filterList), Document.class);
                        })
                )
                .flatMap(e -> Mono.just(new ApiClassification(
                        e.get("apiId", String.class),
                        e.get("apiVersion", String.class),
                        e.get("taxonomyVersion", String.class),
                        e.get("taxonomyUrn", String.class),
                        e.get("taxonomyNid", String.class),
                        e.get("created", Date.class)
                )));
    }

    @Override
    public Flux<ApiClassification> all(final String apiId) {
        return all(apiId, null);
    }

    @Override
    public Flux<ApiClassification> all(final String apiId, final String apiVersion) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));

        if (apiVersion != null) {
            query.addCriteria(Criteria.where("apiVersion").is(apiVersion));
        }

        return template
                .find(query, ApiClassification.class);
    }

    @Override
    public Mono<ApiClassification> update(final ApiClassification entity, final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));

        return get(entity.getApiId(), entity.getApiVersion(), entity.getTaxonomyUrn())
                .filter(Objects::nonNull)
                .flatMap(classification -> {
                    classification.setTaxonomyVersion(entity.getTaxonomyVersion());
                    classification.setTaxonomyNid(entity.getTaxonomyNid());
                    return Mono.just(classification);
                })
                .flatMap(classification -> {
                    final FindAndModifyOptions options = new FindAndModifyOptions();
                    options.returnNew(true);
                    options.upsert(true);

                    final Update update = new Update();
                    update.set("taxonomyVersion", entity.getTaxonomyVersion());
                    update.set("taxonomyNid", entity.getTaxonomyNid());
                    return template.findAndModify(query, update, options, ApiClassification.class)
                            .switchIfEmpty(add(entity));
                });
    }

    @Override
    public Mono<ApiClassification> add(final ApiClassification entity) {
        entity.setCreated(new Date());

        return get(entity.getApiId(), entity.getApiVersion(), entity.getTaxonomyUrn())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Classification combination '" + entity.getApiId() + " - " + entity.getApiVersion() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<ApiClassification> get(final String apiId, final String apiVersion, final String taxonomyUrn) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));
        query.addCriteria(Criteria.where("taxonomyUrn").is(taxonomyUrn));

        return template.findOne(query, ApiClassification.class);
    }

    @Override
    public Mono<Boolean> delete(final String apiId, final String apiVersion) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));

        return template
                .remove(query, ApiClassification.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Boolean> delete(final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        return template
                .remove(query, ApiClassification.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    /* OB */

    @Override
    public Mono<List<Bson>> queryFilters(List<Filter> filters) {
        return Flux.fromStream(filters
                        .stream()
                        .filter(filter -> filter.type() == Filter.TYPE.CLASSIFICATION)
                        .map(filter -> taxonomyRepository
                                .getTaxonomyCollectionVersionURN(filter.getValue(), TAXONOMY_VERSION, TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                                .map(TaxonomyCollectionVersionURN::getUrl)))
                .flatMap(url -> url)
                .flatMap(url -> taxonomyRepository
                        .allTaxonomyCollectionVersionURNsBellowUrl(url)
                        .flatMap(version -> Mono.just(Filters.eq("taxonomyUrn", version.getUrn()))))
                .collectList();
    }
}
