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

package io.apimap.api.service.response;

import io.apimap.api.repository.nitrite.entity.db.ApiClassification;
import io.apimap.api.repository.nitrite.entity.support.ClassificationCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationTreeCollection;
import io.apimap.api.rest.ClassificationDataRestEntity;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.ClassificationTreeDataRestEntity;
import io.apimap.api.rest.ClassificationTreeRootRestEntity;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.rest.jsonapi.JsonApiViews;
import io.apimap.api.utils.URIUtil;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassificationResponseBuilder extends ResponseBuilder<ClassificationResponseBuilder> {

    protected ArrayList<Object> includedObjects = new ArrayList<>();

    public ClassificationResponseBuilder(long startTime) {
        super(startTime);
    }

    public static ClassificationResponseBuilder builder() {
        return new ClassificationResponseBuilder(System.currentTimeMillis());
    }

    public ClassificationResponseBuilder includeObject(Object object) {
        includedObjects.add(object);
        return this;
    }

    public ClassificationResponseBuilder withClassificationTreeCollectionBody(List<ClassificationTreeCollection> value) {

        JsonApiRestResponseWrapper<ClassificationTreeRootRestEntity> JsonApiRestResponseWrapper = new JsonApiRestResponseWrapper<>(null);

        ArrayList<ClassificationTreeDataRestEntity> entities = value.stream().map(collection -> {

            ArrayList<String> path = new ArrayList<>(Arrays.asList(collection.getTaxonomy().getUrl().substring(11).split("/")));

            ClassificationTreeDataRestEntity classificationTreeDataRestEntity = new ClassificationTreeDataRestEntity(
                    collection.getTaxonomy().getUrn(),
                    collection.getTaxonomy().getTitle(),
                    URIUtil.classificationCollectionFromURI(resourceURI).append(collection.getTaxonomy().getUrn()).stringValue(),
                    path,
                    new JsonApiRelationships());

            collection.getItems().forEach(e -> {
                MetadataDataRestEntity metadataDataRestEntity = new MetadataDataRestEntity(
                        e.getName(),
                        e.getDescription(),
                        e.getVisibility(),
                        e.getApiVersion(),
                        e.getReleaseStatus(),
                        e.getInterfaceSpecification(),
                        e.getInterfaceDescriptionLanguage(),
                        e.getArchitectureLayer(),
                        e.getBusinessUnit(),
                        e.getSystemIdentifier(),
                        e.getDocumentation(),
                        URIUtil.apiCollectionFromURI(resourceURI).append(e.getName()).stringValue()
                );

                metadataDataRestEntity.addRelatedRef("version:collection", URIUtil.apiCollectionFromURI(resourceURI).append(e.getName()).append("version").uriValue());
                JsonApiRestResponseWrapper.addIncludedObject(metadataDataRestEntity);

                classificationTreeDataRestEntity.getRelationships().addDataRef(JsonApiRestResponseWrapper.METADATA_COLLECTION, JsonApiRestResponseWrapper.METADATA_ELEMENT, e.getName() + "#" + e.getApiVersion());
            });
            return classificationTreeDataRestEntity;
        }).collect(Collectors.toCollection(ArrayList::new));

        ClassificationTreeRootRestEntity classificationTreeRootRestEntity = new ClassificationTreeRootRestEntity(entities);
        JsonApiRestResponseWrapper.setData(classificationTreeRootRestEntity);

        this.body = JsonApiRestResponseWrapper;
        return this;
    }

    public ClassificationResponseBuilder withClassificationCollectionBody(ClassificationCollection value) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        value.getItems().forEach(e -> relationships.addRelationshipRef(
                e.getTaxonomyUrn(),
                URIUtil.fromURI(resourceURI).append("taxonomy").append(e.getTaxonomyUrn()).uriValue()
        ));

        ArrayList<ClassificationDataRestEntity> classificationDataApiEntities = value
                .getItems()
                .parallelStream()
                .map(e -> new ClassificationDataRestEntity(
                        e.getTaxonomyUrn(),
                        e.getTaxonomyVersion(),
                        URIUtil.fromURI(resourceURI).append("taxonomy").append(e.getTaxonomyNid()).append("version").append(e.getTaxonomyVersion()).append("urn").append(e.getTaxonomyUrn()).stringValue(),
                        null))
                .collect(Collectors.toCollection(ArrayList::new));

        ClassificationRootRestEntity classificationRootRestEntity = new ClassificationRootRestEntity(classificationDataApiEntities);
        this.body = new JsonApiRestResponseWrapper<>(classificationRootRestEntity);
        return this;
    }

    @Override
    public Mono<ServerResponse> okCollection() {
        JsonApiRestResponseWrapper body = super.bodyWithMetadata(this.body);
        body.setSelf(resourceURI);
        this.relatedReferences.forEach(rel -> body.addRelatedRef((String) rel.get("rel"), (URI) rel.get("href")));

        includedObjects.forEach(e -> {
            if (e instanceof ApiClassification) {
                ClassificationDataRestEntity object = new ClassificationDataRestEntity(
                        ((ApiClassification) e).getTaxonomyUrn(),
                        ((ApiClassification) e).getTaxonomyVersion()
                );

                body.addIncludedObject(object);
            }
        });

        body.appendDuration(responseMetricsStartTime, System.currentTimeMillis());

        return ServerResponse.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .hint(Jackson2CodecSupport.JSON_VIEW_HINT, JsonApiViews.Collection.class)
                .body(Mono.just(body), JsonApiRestResponseWrapper.class);
    }
}
