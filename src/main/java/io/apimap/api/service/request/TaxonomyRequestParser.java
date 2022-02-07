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

package io.apimap.api.service.request;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.utils.RequestUtil;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class TaxonomyRequestParser extends RequestParser<TaxonomyRequestParser> {

    public static TaxonomyRequestParser parser() {
        return new TaxonomyRequestParser();
    }

    public static TaxonomyCollectionVersionURN fromTaxonomyDataRestEntity(TaxonomyDataRestEntity RestEntity) {
        return new TaxonomyCollectionVersionURN(
                RestEntity.getUrn(),
                RestEntity.getUrl(),
                RestEntity.getTitle(),
                RestEntity.getDescription(),
                "",
                RestEntity.getTaxonomyVersion(),
                RestEntity.getReferenceType().toString()
        );
    }

    public Optional<TaxonomyCollectionVersionURN> dataRestEntity() {
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyDataRestEntity.class);
        AtomicReference<TaxonomyDataRestEntity> taxonomyDataRestEntity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> taxonomyDataRestEntity.set((TaxonomyDataRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();

        if (taxonomyDataRestEntity.get() == null) {
            return Optional.empty();
        }

        if (taxonomyVersion.equals("latest")) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollectionVersionURN(
                taxonomyDataRestEntity.get().getUrn(),
                taxonomyDataRestEntity.get().getUrl(),
                taxonomyDataRestEntity.get().getTitle(),
                taxonomyDataRestEntity.get().getDescription(),
                taxonomyNid,
                taxonomyVersion,
                taxonomyDataRestEntity.get().getReferenceType().getValue()
        ));
    }

    public Optional<TaxonomyCollectionVersionURN> taxonomyTree() {
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyTreeDataRestEntity.class);
        AtomicReference<TaxonomyTreeDataRestEntity> taxonomyTreeDataRestEntity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> taxonomyTreeDataRestEntity.set((TaxonomyTreeDataRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();

        if (taxonomyTreeDataRestEntity.get() == null) {
            return Optional.empty();
        }

        if (taxonomyVersion.equals("latest")) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollectionVersionURN(
                taxonomyTreeDataRestEntity.get().getUrn(),
                taxonomyTreeDataRestEntity.get().getUrl(),
                taxonomyTreeDataRestEntity.get().getTitle(),
                taxonomyTreeDataRestEntity.get().getDescription(),
                taxonomyNid,
                taxonomyVersion,
                null
        ));
    }

    public Optional<TaxonomyCollectionVersion> taxonomyVersionCollection() {
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyVersionCollectionDataRestEntity.class);
        AtomicReference<TaxonomyVersionCollectionDataRestEntity> taxonomyVersionCollectionDataRestEntity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> taxonomyVersionCollectionDataRestEntity.set((TaxonomyVersionCollectionDataRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();

        if (taxonomyVersionCollectionDataRestEntity.get() == null) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollectionVersion(
                taxonomyNid,
                taxonomyVersionCollectionDataRestEntity.get().getVersion()
        ));
    }

    public Optional<TaxonomyCollection> taxonomyCollection() {
        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyCollectionDataRestEntity.class);
        AtomicReference<TaxonomyCollectionDataRestEntity> taxonomyCollectionDataRestEntity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> taxonomyCollectionDataRestEntity.set((TaxonomyCollectionDataRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();

        if (taxonomyCollectionDataRestEntity.get() == null) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollection(
                taxonomyCollectionDataRestEntity.get().getName(),
                taxonomyCollectionDataRestEntity.get().getDescription(),
                taxonomyCollectionDataRestEntity.get().getNid()
        ));
    }
}
