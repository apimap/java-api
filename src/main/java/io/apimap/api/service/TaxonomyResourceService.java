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

package io.apimap.api.service;

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.ITaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersion;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.request.TaxonomyRequestParser;
import io.apimap.api.service.response.TaxonomyResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

@Service
public class TaxonomyResourceService {
    protected ITaxonomyRepository taxonomyRepository;

    protected ApimapConfiguration apimapConfiguration;

    public TaxonomyResourceService(ITaxonomyRepository taxonomyRepository,
                                   ApimapConfiguration apimapConfiguration) {
        this.taxonomyRepository = taxonomyRepository;
        this.apimapConfiguration = apimapConfiguration;
    }

    /*
    TaxonomyCollection
     */

    @NotNull
    public Mono<ServerResponse> allCollections(ServerRequest request) {
        return TaxonomyResponseBuilder
                .builder(apimapConfiguration)
                .withResourceURI(request.uri())
                .withTaxonomyCollectionCollectionBody(taxonomyRepository.allTaxonomyCollection())
                .okCollection();
    }

    @NotNull
    public Mono<ServerResponse> createCollection(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final Optional<TaxonomyCollection> entity = TaxonomyRequestParser
                .parser()
                .withRequest(request)
                .taxonomyCollection();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        if(taxonomyRepository.getTaxonomyCollection(entity.get().getNid()).isPresent()){
            return responseBuilder.conflict();
        }

        final Optional<TaxonomyCollection> insertedEntity = taxonomyRepository.addTaxonomyCollection(entity.get());

        if (insertedEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionBody(insertedEntity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(insertedEntity.get().getNid()).append("version").uriValue())
                .created(true);
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteCollection(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        taxonomyRepository.deleteTaxonomyCollection(taxonomyNid);
        taxonomyRepository.deleteTaxonomyCollectionVersions(taxonomyNid);
        taxonomyRepository.deleteTaxonomyCollectionVersionURNs(taxonomyNid);

        return responseBuilder.noContent();
    }

    @NotNull
    public Mono<ServerResponse> getCollection(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        final Optional<TaxonomyCollection> entity = taxonomyRepository.getTaxonomyCollection(taxonomyNid);

        if (entity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionBody(entity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(taxonomyNid).append("version").uriValue())
                .okResource();
    }

    /*
    TaxonomyCollectionVersion
    */

    @NotNull
    public Mono<ServerResponse> getVersion(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);

        Optional<TaxonomyCollectionVersion> entity = Optional.empty();

        if (taxonomyVersion.equals("latest")) {
            entity = taxonomyRepository.getLatestTaxonomyCollectionVersion(taxonomyNid);
        } else {
            entity = taxonomyRepository.getTaxonomyCollectionVersion(taxonomyNid, taxonomyVersion);
        }

        if (entity.isEmpty()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionBody(entity.get())
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(taxonomyNid).append("version").uriValue())
                .addRelatedRef(JsonApiRestResponseWrapper.URN_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(taxonomyNid).append("version").append(taxonomyVersion).append("urn").uriValue())
                .okResource();
    }

    @NotNull
    public Mono<ServerResponse> allVersions(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionCollectionBody(taxonomyRepository.allTaxonomyCollectionVersion(taxonomyNid))
                .okCollection();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> createVersion(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final Optional<TaxonomyCollectionVersion> entity = TaxonomyRequestParser
                .parser()
                .withRequest(request)
                .taxonomyVersionCollection();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<TaxonomyCollectionVersion> insertedEntity = taxonomyRepository.addTaxonomyCollectionVersion(entity.get());

        if (insertedEntity.isEmpty()) {
            return badRequest().build();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionBody(insertedEntity.get())
                .created(false);
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteVersion(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        if (taxonomyVersion.equals("latest")) {
            return responseBuilder.notImplemented();
        }

        taxonomyRepository.deleteTaxonomyCollectionVersionURNs(taxonomyNid, taxonomyVersion);
        taxonomyRepository.deleteTaxonomyCollectionVersion(taxonomyNid, taxonomyVersion);

        return responseBuilder.noContent();
    }

    /*
    TaxonomyCollectionVersionURN
    */

    @NotNull
    public Mono<ServerResponse> allURNs(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        String useVersionString = taxonomyVersion;
        if (taxonomyVersion.equals("latest")) {
            Optional<TaxonomyCollectionVersion> latestEntity = taxonomyRepository.getLatestTaxonomyCollectionVersion(taxonomyNid);
            if (latestEntity.isPresent()) {
                useVersionString = latestEntity.get().getVersion();
            }
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionURNCollectionBody(taxonomyRepository.allTaxonomyCollectionVersionURNCollection(taxonomyVersion, taxonomyNid, useVersionString))
                .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(taxonomyNid).append("version").uriValue())
                .okCollection();
    }

    @NotNull
    public Mono<ServerResponse> getURN(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyUrn = RequestUtil.taxonomyUrnFromRequest(request);
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);
        final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);

        String useVersionString = taxonomyVersion;
        if (taxonomyVersion.equals("latest")) {
            Optional<TaxonomyCollectionVersion> latestEntity = taxonomyRepository.getLatestTaxonomyCollectionVersion(taxonomyNid);
            if (latestEntity.isPresent()) {
                useVersionString = latestEntity.get().getVersion();
            }
        }

        Optional<TaxonomyCollectionVersionURN> entity = taxonomyRepository.getTaxonomyCollectionVersionURN(taxonomyUrn, useVersionString);

        if (!entity.isPresent()) {
            return responseBuilder.notFound();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionURNBody(entity.get())
                .okResource();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteURN(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final String taxonomyUrn = RequestUtil.taxonomyUrnFromRequest(request);
        final String taxonomyVersion = RequestUtil.taxonomyVersionFromRequest(request);

        if (taxonomyVersion.equals("latest")) {
            return responseBuilder.notImplemented();
        }

        taxonomyRepository.deleteTaxonomyCollectionVersionURN(taxonomyUrn, taxonomyVersion);
        return responseBuilder.noContent();
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> createURN(ServerRequest request) {
        TaxonomyResponseBuilder responseBuilder = TaxonomyResponseBuilder.builder(apimapConfiguration);

        final Optional<TaxonomyCollectionVersionURN> entity = TaxonomyRequestParser.parser().withRequest(request).taxonomyTree();

        if (entity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        final Optional<TaxonomyCollectionVersionURN> insertedEntity = taxonomyRepository.addTaxonomyCollectionVersionURN(entity.get());

        if (insertedEntity.isEmpty()) {
            return responseBuilder.badRequest();
        }

        return responseBuilder
                .withResourceURI(request.uri())
                .withTaxonomyCollectionVersionURNBody(insertedEntity.get())
                .created(false);
    }
}
