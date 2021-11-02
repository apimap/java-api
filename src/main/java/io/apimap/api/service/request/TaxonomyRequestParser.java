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

import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionDataRestEntity;
import io.apimap.api.utils.RequestUtil;

import java.util.Optional;

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
                RestEntity.getTaxonomyVersion()
        );
    }

    public Optional<TaxonomyCollectionVersionURN> taxonomyTree() {
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        final TaxonomyTreeDataRestEntity entity = request.bodyToMono(TaxonomyTreeDataRestEntity.class).block();

        if (entity == null) {
            return Optional.empty();
        }

        if (taxonomyVersion.equals("latest")) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollectionVersionURN(
                entity.getUrn(),
                entity.getUrl(),
                entity.getTitle(),
                entity.getDescription(),
                taxonomyNid,
                taxonomyVersion
        ));
    }

    public Optional<TaxonomyCollectionVersion> taxonomyVersionCollection() {
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);
        final TaxonomyVersionCollectionDataRestEntity entity = request.bodyToMono(TaxonomyVersionCollectionDataRestEntity.class).block();

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollectionVersion(
                taxonomyNid,
                entity.getVersion()
        ));
    }

    public Optional<TaxonomyCollection> taxonomyCollection() {
        TaxonomyCollectionDataRestEntity taxonomyCollectionDataRestEntity = request.bodyToMono(TaxonomyCollectionDataRestEntity.class).block();

        if (taxonomyCollectionDataRestEntity == null) {
            return Optional.empty();
        }

        return Optional.of(new TaxonomyCollection(
                taxonomyCollectionDataRestEntity.getName(),
                taxonomyCollectionDataRestEntity.getDescription(),
                taxonomyCollectionDataRestEntity.getNid()
        ));
    }
}
