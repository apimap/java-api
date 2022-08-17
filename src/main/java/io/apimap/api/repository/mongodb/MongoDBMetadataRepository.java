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

import io.apimap.api.repository.interfaces.IDocument;
import io.apimap.api.repository.mongodb.documents.Document;
import io.apimap.api.repository.mongodb.documents.Metadata;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.QueryFilter;
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

import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.or;
import static java.util.stream.Collectors.toCollection;


@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBMetadataRepository extends MongoDBRepository implements IMetadataRepository<Metadata, Document, Bson> {

    public MongoDBMetadataRepository(ReactiveMongoTemplate template) {
        super(template);
    }

    /* M */

    @Override
    public Flux<Metadata> allByFilters(Mono<List<Bson>> filters) {
        return template
                .getCollection("metadata")
                .flatMapMany(collection -> filters
                        .filter(Objects::nonNull)
                        .filter(list -> list.size() > 0)
                        .flatMapMany(filterList -> {
                            return collection.find(and(filterList), org.bson.Document.class);
                        })
                )
                .flatMap(e -> Mono.just(new Metadata(
                        e.get("apiId", String.class),
                        e.get("description", String.class),
                        e.get("apiVersion", String.class),
                        e.get("name", String.class),
                        e.get("visibility", String.class),
                        e.get("interfaceDescriptionLanguage", String.class),
                        e.get("architectureLayer", String.class),
                        e.get("businessUnit", String.class),
                        e.get("metadataVersion", String.class),
                        e.get("releaseStatus", String.class),
                        e.get("interfaceSpecification", String.class),
                        e.get("systemIdentifier", String.class),
                        e.get("documentation", List.class),
                        e.get("created", Date.class)
                )));
    }

    @Override
    public Flux<Metadata> allByApiId(final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        return template.find(query, Metadata.class);
    }

    @Override
    public Flux<Metadata> all() {
        return template.findAll(Metadata.class);
    }

    @Override
    public Mono<Metadata> add(final Metadata entity) {
        entity.setCreated(new Date());

        return get(entity.getApiId(), entity.getApiVersion())
                .doOnNext(api -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Metadata '" + entity.getApiVersion() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<Metadata> update(final Metadata entity) {
        return get(entity.getApiId(), entity.getApiVersion())
                .flatMap(metadata -> {
                    final FindAndModifyOptions options = new FindAndModifyOptions();
                    options.returnNew(true);
                    options.upsert(true);

                    final Update update = new Update();
                    update.set("description", entity.getDescription());
                    update.set("name", entity.getName());
                    update.set("visibility", entity.getVisibility());
                    update.set("interfaceDescriptionLanguage", entity.getInterfaceDescriptionLanguage());
                    update.set("architectureLayer", entity.getArchitectureLayer());
                    update.set("businessUnit", entity.getBusinessUnit());
                    update.set("releaseStatus", entity.getReleaseStatus());
                    update.set("interfaceSpecification", entity.getInterfaceSpecification());
                    update.set("systemIdentifier", entity.getSystemIdentifier());
                    update.set("documentation", entity.getDocumentation());

                    final Query query = new Query().addCriteria(Criteria.where("id").is(metadata.getId()));
                    return template.findAndModify(query, update, options, Metadata.class);
                });
    }

    @Override
    public Mono<Metadata> get(final String apiId, final String apiVersion) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));

        return template.findOne(query, Metadata.class);
    }

    @Override
    public Mono<Boolean> delete(final String apiId) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));

        return template
                .remove(query, Metadata.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Boolean> delete(final String apiId, final String apiVersion) {
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));

        return template
                .remove(query, Metadata.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    /* OB */

    @Override
    public Mono<List<Bson>> queryFilters(List<Filter> filters, QueryFilter queryFilter) {
        HashMap<String, ArrayList<Bson>> tmp = new HashMap<>();

        filters
                .stream()
                .filter(e -> e.type() == Filter.TYPE.METADATA)
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

        if (queryFilter != null) {
            objectFilters.add(queryFilter.mongoObjectFilter());
        }

        return Mono.just(objectFilters);
    }

    /* TIMetadataDocument */
    @Override
    public Mono<Document> getDocument(String apiId, String apiVersion, IDocument.DocumentType documentType){
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));
        query.addCriteria(Criteria.where("type").is(documentType));

        return template.findOne(query, Document.class);
    }

    @Override
    public Mono<Boolean> deleteDocument(String apiId, String apiVersion, IDocument.DocumentType documentType){
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));
        query.addCriteria(Criteria.where("type").is(documentType));

        return template
                .remove(query, Document.class)
                .flatMap(result -> Mono.just((result.getDeletedCount() > 0)));
    }

    @Override
    public Mono<Document> addDocument(String apiId, String apiVersion, Document entity){
        entity.setCreated(new Date());
        entity.setApiId(apiId);
        entity.setApiVersion(apiVersion);

        return getDocument(entity.getApiId(), entity.getApiVersion(), entity.getType())
                .doOnNext(document -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Metadata Document of type '" + entity.getType() + "' already exists");
                })
                .switchIfEmpty(Mono.defer(() -> template.insert(entity)));
    }

    @Override
    public Mono<Document> updateDocument(String apiId, String apiVersion, Document entity){
        return getDocument(apiId, apiVersion, entity.getType())
                .flatMap(document -> {
                    final Update update = new Update();
                    update.set("body", entity.getBody());

                    final FindAndModifyOptions options = new FindAndModifyOptions();
                    options.returnNew(true);
                    options.upsert(true);

                    final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
                    query.addCriteria(Criteria.where("apiVersion").is(apiVersion));
                    query.addCriteria(Criteria.where("type").is(entity.getType()));

                    return template.findAndModify(query, update, options, Document.class);
                });
    }
}
