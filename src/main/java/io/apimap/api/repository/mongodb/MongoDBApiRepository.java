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
import java.util.List;
import java.util.Objects;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBApiRepository extends MongoDBRepository implements IApiRepository<Api, ApiVersion> {

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
                    Update update = new Update();
                    update.set("name", api.getName());
                    update.set("codeRepositoryUrl", api.getCodeRepositoryUrl());
                    return template.findAndModify(query, update, Api.class);
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
        final Query query = new Query().addCriteria(Criteria.where("apiName").is(apiName));
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
}
