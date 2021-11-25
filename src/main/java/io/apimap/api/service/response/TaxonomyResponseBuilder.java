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

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionCollection;
import io.apimap.api.repository.nitrite.entity.support.TaxonomyCollectionVersionURNCollection;
import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyCollectionRootRestEntity;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeRootRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.utils.TaxonomyTreeUtil;
import io.apimap.api.utils.URIUtil;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TaxonomyResponseBuilder extends ResponseBuilder<TaxonomyResponseBuilder> {

    public TaxonomyResponseBuilder(long startTime, ApimapConfiguration apimapConfiguration) {
        super(startTime, apimapConfiguration);
    }

    public static TaxonomyResponseBuilder builder(ApimapConfiguration apimapConfiguration) {
        return new TaxonomyResponseBuilder(System.currentTimeMillis(), apimapConfiguration);
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionVersionURNBody(TaxonomyCollectionVersionURN value) {
        TaxonomyDataRestEntity taxonomyDataRestEntity = new TaxonomyDataRestEntity(
                value.getUrn(),
                value.getTitle(),
                value.getUrl(),
                value.getDescription(),
                resourceURI.toString(),
                value.getNid()
        );

        this.body = new JsonApiRestResponseWrapper<>(taxonomyDataRestEntity);
        return this;
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionVersionURNCollectionBody(TaxonomyCollectionVersionURNCollection value) {
        TaxonomyTreeRootRestEntity taxonomyTreeRootRestEntity = new TaxonomyTreeRootRestEntity(
                value.getItems().stream().map(e -> new TaxonomyTreeDataRestEntity(
                        e.getUrn(),
                        e.getUrl(),
                        e.getTitle(),
                        e.getDescription(),
                        URIUtil.fromURI(resourceURI).append(e.getUrn()).stringValue()
                )).collect(Collectors.toCollection(ArrayList::new)));

        TaxonomyTreeUtil taxonomyTreeUtil = TaxonomyTreeUtil.empty();

        for (TaxonomyTreeDataRestEntity e : taxonomyTreeRootRestEntity.getData()) {
            taxonomyTreeUtil.insert(e);
        }

        this.body = new JsonApiRestResponseWrapper<>(new TaxonomyTreeRootRestEntity(taxonomyTreeUtil.getRootEntity().getEntities()));
        return this;
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionVersionCollectionBody(TaxonomyCollectionVersionCollection value) {
        ArrayList<TaxonomyVersionCollectionDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> new TaxonomyVersionCollectionDataRestEntity(
                        e.getVersion(),
                        e.getNid(),
                        URIUtil.fromURI(resourceURI).append(e.getVersion()).stringValue())
                )
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        this.body = new JsonApiRestResponseWrapper<>(new TaxonomyVersionCollectionRootRestEntity(items));
        return this;
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionVersionBody(TaxonomyCollectionVersion value) {
        TaxonomyVersionCollectionDataRestEntity taxonomyVersionCollectionDataRestEntity = new TaxonomyVersionCollectionDataRestEntity(
                value.getVersion(),
                value.getNid(),
                resourceURI.toString()
        );

        this.body = new JsonApiRestResponseWrapper<>(taxonomyVersionCollectionDataRestEntity);
        return this;
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionBody(TaxonomyCollection value) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.fromURI(resourceURI).append("api").append(value.getNid()).append("version").uriValue());

        TaxonomyCollectionDataRestEntity taxonomyCollectionDataRestEntity = new TaxonomyCollectionDataRestEntity(
                value.getName(),
                value.getDescription(),
                value.getNid(),
                URIUtil.fromURI(resourceURI).append(value.getNid()).stringValue(),
                value.getToken(),
                relationships
        );

        this.body = new JsonApiRestResponseWrapper<>(taxonomyCollectionDataRestEntity);
        return this;
    }

    public TaxonomyResponseBuilder withTaxonomyCollectionCollectionBody(TaxonomyCollectionCollection value) {
        ArrayList<TaxonomyCollectionDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> {
                    JsonApiRelationships relationships = new JsonApiRelationships();
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.fromURI(resourceURI).append(e.getNid()).append("version").uriValue());
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_ELEMENT, URIUtil.fromURI(resourceURI).append(e.getNid()).append("version").append("latest").uriValue());
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.URN_COLLECTION, URIUtil.fromURI(resourceURI).append(e.getNid()).append("version").append("latest").append("urn").uriValue());

                    return new TaxonomyCollectionDataRestEntity(
                            e.getName(),
                            e.getDescription(),
                            e.getNid(),
                            URIUtil.fromURI(resourceURI).append(e.getNid()).stringValue(),
                            null,
                            relationships
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));

        this.body = new JsonApiRestResponseWrapper<>(new TaxonomyCollectionRootRestEntity(items));
        return this;
    }
}
