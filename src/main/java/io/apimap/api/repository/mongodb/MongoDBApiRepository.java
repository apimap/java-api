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

import io.apimap.api.repository.mongodb.documents.Api;
import io.apimap.api.repository.mongodb.documents.ApiVersion;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.service.query.Filter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Sort;
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

import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.or;
import static java.util.stream.Collectors.toCollection;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBApiRepository extends MongoDBRepository implements IApiRepository<Api, ApiVersion, Bson> {

    final protected MongoDBMetadataRepository metadataRepository;

    public MongoDBApiRepository(final ReactiveMongoTemplate template,
                                final MongoDBMetadataRepository metadataRepository) {
        super(template);
        this.metadataRepository = metadataRepository;
    }

    /* A */

    @Override
    public Flux<Api> all() {
        return template
                .findAll(Api.class)
                .doOnNext(Api::clearToken);
    }

    @Override
    public Flux<Api> allByFilters(Mono<List<Bson>> filters) {
        return template
                .getCollection("api")
                .flatMapMany(collection -> filters
                        .flatMapMany(filterList -> {
                            if(filterList.size() < 1){
                                return collection.find(Document.class);
                            }else{
                                return collection.find(and(filterList), Document.class);
                            }
                        })
                        .switchIfEmpty(collection.find(Document.class))
                )
                .flatMap(e -> Mono.just(new Api(
                        e.get("name", String.class),
                        e.get("codeRepositoryUrl", String.class),
                        null,
                        e.get("created", Date.class),
                        e.get("_id", String.class)
                )));
    }

    @Override
    public Flux<Api> allByApiIds(List<String> apiIds) {
        final Query query = new Query().addCriteria(Criteria.where("id").in(apiIds));
        return template
                .find(query, Api.class)
                .doOnNext(Api::clearToken);
    }

    @Override
    public Mono<Api> add(final Api entity) {
        entity.generateToken();
        entity.setCreated(new Date());

        return get(entity.getName())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The API '" + entity.getName() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<Api> update(final Api entity, final String apiName) {
        final Query query = new Query().addCriteria(Criteria.where("name").is(apiName));
        return get(apiName)
                .filter(Objects::nonNull)
                .flatMap(api -> {
                    api.setName(entity.getName());
                    api.setCodeRepositoryUrl(entity.getCodeRepositoryUrl());
                    return Mono.justOrEmpty(api);
                })
                .flatMap(api -> {
                    final FindAndModifyOptions options = new FindAndModifyOptions();
                    options.returnNew(true);

                    final Update update = new Update();
                    update.set("name", api.getName());
                    update.set("codeRepositoryUrl", api.getCodeRepositoryUrl());

                    return template.findAndModify(query, update, options, Api.class);
                })
                .flatMap(api -> {
                    api.clearToken();
                    return Mono.justOrEmpty(api);
                });
    }

    @Override
    public Mono<Api> get(final String apiName) {
        final Query query = new Query().addCriteria(Criteria.where("name").is(apiName));
        return template.findOne(query, Api.class);
    }

    @Override
    public Mono<Api> getById(String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("id").is(apiId));
        return template.findOne(query, Api.class);
    }

    @Override
    public Mono<Boolean> delete(final String apiName) {
        final Query query = new Query().addCriteria(Criteria.where("name").is(apiName));
        return template
                .remove(query, Api.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Long> numberOfApis() {
        return template.estimatedCount(Api.class);
    }

    /* AV */

    @Override
    public Mono<ApiVersion> getLatestApiVersion(final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId)).with(Sort.by(Sort.Direction.DESC, "created"));
        return template.findOne(query, ApiVersion.class);
    }

    @Override
    public Mono<Boolean> deleteApiVersion(final String apiId, final String apiVersion) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("version").is(apiVersion));

        return template
                .remove(query, ApiVersion.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<ApiVersion> getApiVersion(final String apiId, final String apiVersion) {
        if ("latest".equals(apiVersion)) {
            return getLatestApiVersion(apiId);
        }

        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("version").is(apiVersion));
        return template.findOne(query, ApiVersion.class);
    }

    @Override
    public Flux<ApiVersion> allApiVersions(final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        return template.find(query, ApiVersion.class);
    }

    @Override
    public Mono<ApiVersion> addApiVersion(final ApiVersion entity) {
        entity.setCreated(new Date());

        return getApiVersion(entity.getApiId(), entity.getVersion())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "The API Version '" + entity.getVersion() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    /* OB */

    @Override
    public Mono<List<Bson>> queryFilters(List<Filter> filters) {
        HashMap<String, ArrayList<Bson>> tmp = new HashMap<>();

        filters
                .stream()
                .filter(e -> e.type() == Filter.TYPE.NAME)
                .forEach(e -> {
                    String key = e.getKey();
                    ArrayList<Bson> f = tmp.getOrDefault(key, new ArrayList<>());
                    if (key != null) {
                        f.add(e.mongoObjectFilter());
                        tmp.put(key, f);
                    }
                });

        ArrayList<Bson> objectFilters = tmp
                .values()
                .stream()
                .map(objectFilterArrayList -> or(objectFilterArrayList.toArray(Bson[]::new)))
                .collect(toCollection(ArrayList::new));

        return Mono.just(objectFilters);
    }
}
