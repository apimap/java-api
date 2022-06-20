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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.entities.IRESTEntityMapper;
import io.apimap.api.repository.entities.ITaxonomyCollection;
import io.apimap.api.repository.entities.ITaxonomyCollectionVersion;
import io.apimap.api.repository.entities.ITaxonomyCollectionVersionURN;
import io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersionURN;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.TaxonomyContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import io.apimap.api.utils.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Service
public class TaxonomyResourceService {

    final protected ITaxonomyRepository taxonomyRepository;
    final protected ApimapConfiguration apimapConfiguration;
    final protected IRESTEntityMapper entityMapper;

    public TaxonomyResourceService(final IRESTEntityMapper entityMapper,
                                   final ITaxonomyRepository taxonomyRepository,
                                   final ApimapConfiguration apimapConfiguration) {
        this.taxonomyRepository = taxonomyRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }

    /*
    TaxonomyCollection
     */

    @NotNull
    public Mono<ServerResponse> allCollections(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();

        return taxonomyRepository
                .allTaxonomyCollection()
                .collectList()
                .flatMap(collection -> entityMapper.encodeTaxonomyCollections(uri, (List<ITaxonomyCollection>) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) collection)
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.noContent().build()));
    }

    @NotNull
    public Mono<ServerResponse> createCollection(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyCollectionDataRestEntity.class);

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .flatMap(collection -> entityMapper.decodeTaxonomyCollection((JsonApiRestRequestWrapper<TaxonomyCollectionDataRestEntity>) collection))
                .flatMap(collection -> taxonomyRepository.addTaxonomyCollection(collection))
                .flatMap(collection -> entityMapper.encodeTaxonomyCollection(uri, (ITaxonomyCollection) collection))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>) collection)
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(uri).append(((JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>) collection).getData().getNid()).append("version").uriValue())
                        .created(true))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteCollection(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollection(context.getNid())
                .flatMap(collection -> taxonomyRepository.deleteTaxonomyCollection(((ITaxonomyCollection) collection).getNid())
                        .zipWith(taxonomyRepository.deleteTaxonomyCollectionVersions(((ITaxonomyCollection) collection).getNid()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue())
                        .zipWith(taxonomyRepository.deleteTaxonomyCollectionVersionURNs(((ITaxonomyCollection) collection).getNid()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getCollection(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollection(context.getNid())
                .flatMap(collection -> entityMapper.encodeTaxonomyCollection(uri, ((ITaxonomyCollection) collection)))
                .flatMap(collection -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>) collection)
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil
                                .rootLevelFromURI(request.uri())
                                .append("taxonomy")
                                .append(((JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>) collection).getData().getNid())
                                .append("version")
                                .uriValue())
                        .okResource())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    /*
    TaxonomyCollectionVersion
    */

    @NotNull
    public Mono<ServerResponse> getVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollection(context.getNid())
                .flatMap(collection -> taxonomyRepository.getTaxonomyCollectionVersion(((ITaxonomyCollection) collection).getNid(), context.getVersion()))
                .flatMap(version -> entityMapper.encodeTaxonomyCollectionVersion(uri, (ITaxonomyCollectionVersion) version))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>) version)
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(((JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>) version).getData().getNid()).append("version").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.URN_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(((JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>) version).getData().getNid()).append("version").append(((JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>) version).getData().getVersion()).append("urn").uriValue())
                        .okResource())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> allVersions(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollection(context.getNid())
                .flatMapMany(collection -> taxonomyRepository.allTaxonomyCollectionVersion(((ITaxonomyCollection) collection).getNid()))
                .collectList()
                .flatMap(version -> entityMapper.encodeTaxonomyCollectionVersions(uri, (List<ITaxonomyCollectionVersion>) version))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<TaxonomyVersionCollectionRootRestEntity>) version)
                        .addRelatedRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(context.getNid()).append("version").uriValue())
                        .addRelatedRef(JsonApiRestResponseWrapper.URN_COLLECTION, URIUtil.taxonomyCollectionFromURI(request.uri()).append(context.getNid()).append("version").append(context.getVersion()).append("urn").uriValue())
                        .okCollection())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> createVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyVersionCollectionDataRestEntity.class);

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .filter(Objects::nonNull)
                .flatMap(version -> entityMapper.decodeTaxonomyCollectionVersion((JsonApiRestRequestWrapper<TaxonomyVersionCollectionDataRestEntity>) version))
                .flatMap(version -> {
                    version.setNid(context.getNid());
                    return Mono.just(version);
                })
                .flatMap(version -> taxonomyRepository.addTaxonomyCollectionVersion(version))
                .flatMap(version -> entityMapper.encodeTaxonomyCollectionVersion(uri, (ITaxonomyCollectionVersion) version))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) version)
                        .created(true)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteVersion(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollection(context.getNid())
                .flatMap(taxonomy -> taxonomyRepository.deleteTaxonomyCollectionVersionURNs(((ITaxonomyCollection) taxonomy).getNid(), context.getVersion())
                        .zipWith(taxonomyRepository.deleteTaxonomyCollectionVersion(((ITaxonomyCollection) taxonomy).getNid(), context.getVersion()), (previous, current) -> (Boolean) previous && ((Boolean) current).booleanValue())
                )
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    /*
    TaxonomyCollectionVersionURN
    */

    @NotNull
    public Mono<ServerResponse> allURNs(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);
        final URI uri = request.uri();

        return taxonomyRepository
                .allTaxonomyCollectionVersionURNCollection(context.getNid(), context.getVersion())
                .collectList()
                .flatMap(urns -> entityMapper.encodeTaxonomyCollectionVersionURNs(uri, (List<ITaxonomyCollectionVersionURN>) urns))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) version)
                        .okCollection()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> getURN(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);
        final URI uri = request.uri();

        return taxonomyRepository
                .getTaxonomyCollectionVersionURN(context.getUrn(), context.getVersion(), TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                .flatMap(urn -> entityMapper.encodeTaxonomyCollectionVersionURN(uri, (ITaxonomyCollectionVersionURN) urn))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) version)
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> deleteURN(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);

        return taxonomyRepository
                .getTaxonomyCollectionVersionURN(context.getUrn(), context.getVersion(), TaxonomyDataRestEntity.ReferenceType.UNKNOWN)
                .flatMap(urn -> taxonomyRepository.deleteTaxonomyCollectionVersionURN(((ITaxonomyCollectionVersionURN) urn).getUrn(), ((ITaxonomyCollectionVersionURN) urn).getVersion()))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidTaxonomyToken(#request)")
    public Mono<ServerResponse> createURN(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final TaxonomyContext context = RequestUtil.taxonomyContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, TaxonomyDataRestEntity.class);
        final URI uri = request.uri();

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .filter(Objects::nonNull)
                .flatMap(urn -> entityMapper.decodeTaxonomyCollectionVersionURN(context, (JsonApiRestRequestWrapper<TaxonomyDataRestEntity>) urn))
                .flatMap(urn -> taxonomyRepository.addTaxonomyCollectionVersionURN(urn))
                .flatMap(urn -> entityMapper.encodeTaxonomyCollectionVersionURN(uri, (ITaxonomyCollectionVersionURN) urn))
                .flatMap(urn -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<TaxonomyCollectionVersionURN>) urn)
                        .created(false)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }
}
