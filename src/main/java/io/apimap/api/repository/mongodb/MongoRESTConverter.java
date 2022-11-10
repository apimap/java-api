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

import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.generic.ClassificationCollection;
import io.apimap.api.repository.interfaces.*;
import io.apimap.api.repository.mongodb.documents.*;
import io.apimap.api.rest.*;
import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.context.TaxonomyContext;
import io.apimap.api.utils.Comparator;
import io.apimap.api.utils.TaxonomyTreeBuilder;
import io.apimap.api.utils.URIUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoRESTConverter implements IRESTConverter {


    /*
     From REST
     */

    /* API */

    @Override
    public Mono<IApi> decodeApi(final JsonApiRestRequestWrapper<ApiDataRestEntity> object) {
        validateApiDataRestEntity(object.getData());

        return Mono.just(new Api(
                object.getData().getName(),
                object.getData().getCodeRepository()
        ));
    }

    /* API Version */

    @Override
    public Mono<IApiVersion> decodeApiVersion(final IApi api, final JsonApiRestRequestWrapper<ApiVersionDataRestEntity> object) {
        return Mono.just(new ApiVersion(
                object.getData().getVersion(),
                object.getData().getCreated() != null ? object.getData().getCreated().toInstant() : null,
                api.getId()
        ));
    }

    /* Metadata */

    @Override
    public Mono<IMetadata> decodeMetadata(final ApiContext apiContext, final JsonApiRestRequestWrapper<MetadataDataRestEntity> object) {
        return Mono.just(
                new Metadata(
                        apiContext.getApiName(),
                        object.getData().getDescription(),
                        apiContext.getApiVersion(),
                        object.getData().getName(),
                        object.getData().getVisibility(),
                        object.getData().getInterfaceDescriptionLanguage(),
                        object.getData().getArchitectureLayer(),
                        object.getData().getBusinessUnit(),
                        SUPPORTED_METADATA_VERSION,
                        object.getData().getReleaseStatus(),
                        object.getData().getInterfaceSpecification(),
                        object.getData().getSystemIdentifier(),
                        object.getData().getDocumentation(),
                        Instant.now()
                )
        );
    }

    @Override
    public Mono<IDocument> decodeMetadataDocument(ApiContext apiContext, ByteArrayResource resource, IDocument.DocumentType type){
        return Mono.just(
            new Document(
                apiContext.getApiId(),
                apiContext.getApiVersion(),
                new String(resource.getByteArray(), StandardCharsets.UTF_8),
                Instant.now(),
                type
            )
        );
    }

    /* Taxonomy */

    @Override
    public Mono<ITaxonomyCollection> decodeTaxonomyCollection(JsonApiRestRequestWrapper<TaxonomyCollectionDataRestEntity> object) {
        return Mono.justOrEmpty(new TaxonomyCollection(
                object.getData().getName(),
                object.getData().getNid(),
                object.getData().getDescription(),
                null
        ));
    }

    @Override
    public Mono<ITaxonomyCollectionVersion> decodeTaxonomyCollectionVersion(JsonApiRestRequestWrapper<TaxonomyVersionCollectionDataRestEntity> object) {
        return Mono.justOrEmpty(new TaxonomyCollectionVersion(
                object.getData().getNid(),
                object.getData().getVersion(),
                Instant.now()
        ));
    }

    @Override
    public Mono<ITaxonomyCollectionVersionURN> decodeTaxonomyCollectionVersionURN(TaxonomyContext taxonomyContext, JsonApiRestRequestWrapper<TaxonomyDataRestEntity> object) {
        return Mono.justOrEmpty(new TaxonomyCollectionVersionURN(
                object.getData().getUrl(),
                object.getData().getTitle(),
                object.getData().getDescription(),
                taxonomyContext.getNid(),
                taxonomyContext.getVersion(),
                object.getData().getUrn(),
                object.getData().getReferenceType().getValue()
        ));
    }

    /* ApiClassification */

    @Override
    public Mono<IApiClassification> decodeClassification(final ApiContext apiContext, final ClassificationDataRestEntity object) {
        return Mono.just(
                new ApiClassification(
                        apiContext.getApiId(),
                        apiContext.getApiVersion(),
                        object.getTaxonomyVersion(),
                        object.getUrn(),
                        object.getNid(),
                        Instant.now()
                )
        );
    }

    /* Vote */

    public Mono<IVote> decodeVote(ApiContext apiContext, JsonApiRestRequestWrapper<VoteDataRestEntity> object){
        return Mono.just(
                new Vote(
                    apiContext.getApiId(),
                        apiContext.getApiVersion(),
                        object.getData().getRating()
                )
        );
    }

    /*
        To REST
     */

    /* API */

    @Override
    public Mono<JsonApiRestResponseWrapper<ApiDataRestEntity>> encodeApi(final URI uri, final IApi object) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        relationships.addRelationshipRef(
                JsonApiRestResponseWrapper.VERSION_COLLECTION,
                URIUtil.rootLevelFromURI(uri).append("api").append(object.getName()).append("version").uriValue());

        ApiDataRestEntity apiRootRestEntity = new ApiDataRestEntity(
                new ApiDataMetadataEntity(object.getToken()),
                object.getName(),
                object.getCodeRepositoryUrl(),
                uri.toString(),
                relationships
        );

        return Mono.just(new JsonApiRestResponseWrapper<ApiDataRestEntity>(apiRootRestEntity));
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>> encodeApis(final URI uri, final List<reactor.util.function.Tuple3<Optional<IApi>, Optional<IMetadata>, Optional<IApiVersion>>> collection) {
        ArrayList<ApiCollectionDataRestEntity> content = collection
                .stream()
                .map(e -> {
                    if(e.getT1().isPresent()){
                        JsonApiRelationships relationships = new JsonApiRelationships();
                        relationships.addRelationshipRef(
                                JsonApiRestResponseWrapper.VERSION_COLLECTION,
                                URIUtil.rootLevelFromURI(uri).append("api").append(e.getT1().get().getName()).append("version").uriValue());

                        return new ApiCollectionDataRestEntity(
                                e.getT1().get().getName(),
                                e.getT1().get().getCodeRepositoryUrl(),
                                (e.getT2().isPresent()) ? e.getT2().get().getDescription() : "",
                                (e.getT2().isPresent()) ? e.getT2().get().getReleaseStatus() : "",
                                (e.getT3().isPresent()) ? e.getT3().get().getVersion() : "",
                                (e.getT2().isPresent()) ? e.getT2().get().getDocumentation() : new ArrayList<>(),
                                URIUtil.rootLevelFromURI(uri).append("api").append(e.getT1().get().getName()).stringValue(),
                                relationships);
                    }

                    return new ApiCollectionDataRestEntity(
                            "",
                            "",
                            "",
                            "",
                            "",
                            new ArrayList<>(),
                            "",
                            null);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>(new ApiCollectionRootRestEntity(content))
        );
    }

    /* API Version */

    @Override
    public Mono<JsonApiRestResponseWrapper<ApiVersionDataRestEntity>> encodeApiVersion(final URI uri, final IApiVersion object, final Integer rating) {
        ApiVersionDataRestEntity content = new ApiVersionDataRestEntity(
                object.getVersion(),
                Date.from(object.getCreated()),
                new ApiVersionRatingEntity(rating),
                URIUtil.fromURI(uri).append(object.getVersion()).stringValue()
        );

        return Mono.just(
                new JsonApiRestResponseWrapper<>(content)
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<ApiVersionCollectionRootRestEntity>> encodeApiVersions(final URI uri, final List<Tuple2<IApiVersion, Integer>> collection) {
        ArrayList<ApiVersionDataRestEntity> content = collection
                .stream()
                .map(version -> new ApiVersionDataRestEntity(
                        version.getT1().getVersion(),
                        Date.from(version.getT1().getCreated()),
                        new ApiVersionRatingEntity(version.getT2()),
                        URIUtil.fromURI(uri).append(version.getT1().getVersion()).stringValue())
                )
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<ApiVersionCollectionRootRestEntity>(new ApiVersionCollectionRootRestEntity(content))
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<MetadataDataRestEntity>> encodeMetadata(final URI uri, final IMetadata object) {
        final MetadataDataRestEntity content = new MetadataDataRestEntity(
                object.getName(),
                object.getDescription(),
                object.getVisibility(),
                object.getApiVersion(),
                object.getReleaseStatus(),
                object.getInterfaceSpecification(),
                object.getInterfaceDescriptionLanguage(),
                object.getArchitectureLayer(),
                object.getBusinessUnit(),
                object.getSystemIdentifier(),
                object.getDocumentation(),
                uri.toString()
        );

        return Mono.just(
                new JsonApiRestResponseWrapper<MetadataDataRestEntity>(content)
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<ClassificationRootRestEntity>> encodeApiClassifications(final URI uri, final List<IApiClassification> collection) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        collection.forEach(e -> relationships.addRelationshipRef(
                e.getTaxonomyUrn(),
                URIUtil.rootLevelFromURI(uri).append("taxonomy").append(e.getTaxonomyUrn()).uriValue()
        ));

        ArrayList<ClassificationDataRestEntity> content = collection
                .parallelStream()
                .map(e -> new ClassificationDataRestEntity(
                        e.getTaxonomyUrn(),
                        e.getTaxonomyVersion(),
                        URIUtil.rootLevelFromURI(uri).append("taxonomy").append(e.getTaxonomyNid()).append("version").append(e.getTaxonomyVersion()).append("urn").append(e.getTaxonomyUrn()).stringValue(),
                        null))
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<ClassificationRootRestEntity>(new ClassificationRootRestEntity(content))
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<ClassificationTreeRootRestEntity>> encodeClassifications(URI uri, List<ClassificationCollection> classifications) {
        JsonApiRestResponseWrapper<ClassificationTreeRootRestEntity> response = new JsonApiRestResponseWrapper<>(null);

        ArrayList<ClassificationTreeDataRestEntity> entities = classifications.stream()
                .map(collection -> {

                    ArrayList<String> path = new ArrayList<>(Arrays.asList(collection.getTaxonomy().getUrl().substring(11).split("/")));

                    ClassificationTreeDataRestEntity classificationTreeDataRestEntity = new ClassificationTreeDataRestEntity(
                            collection.getTaxonomy().getUrn(),
                            collection.getTaxonomy().getTitle(),
                            URIUtil.classificationCollectionFromURI(uri).append(collection.getTaxonomy().getUrn()).stringValue(),
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
                                URIUtil.apiCollectionFromURI(uri).append(e.getName()).stringValue()
                        );

                        metadataDataRestEntity.addRelatedRef("version:collection", URIUtil.apiCollectionFromURI(uri).append(e.getName()).append("version").uriValue());
                        response.addIncludedObject(metadataDataRestEntity);

                        classificationTreeDataRestEntity.getRelationships().addDataRef(JsonApiRestResponseWrapper.METADATA_COLLECTION, JsonApiRestResponseWrapper.METADATA_ELEMENT, e.getName() + "#" + e.getApiVersion());
                    });
                    return classificationTreeDataRestEntity;
                })
                .sorted(Comparator::compareClassificationTreeDataRestEntity)
                .collect(Collectors.toCollection(ArrayList::new));

        ClassificationTreeRootRestEntity classificationTreeRootRestEntity = new ClassificationTreeRootRestEntity(entities);
        response.setData(classificationTreeRootRestEntity);

        return Mono.just(response);
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>> encodeTaxonomyCollection(URI uri, ITaxonomyCollection collection) {
        JsonApiRelationships relationships = new JsonApiRelationships();
        relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.fromURI(uri).append("api").append(collection.getNid()).append("version").uriValue());

        TaxonomyCollectionDataRestEntity content = new TaxonomyCollectionDataRestEntity(
                collection.getName(),
                collection.getDescription(),
                collection.getNid(),
                URIUtil.rootLevelFromURI(uri).append("taxonomy").append(collection.getNid()).stringValue(),
                relationships
        );

        content.setMeta(new ApiDataMetadataEntity(collection.getToken()));

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>(content)
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>> encodeTaxonomyCollectionVersion(URI uri, ITaxonomyCollectionVersion version) {
        TaxonomyVersionCollectionDataRestEntity content = new TaxonomyVersionCollectionDataRestEntity(
                version.getVersion(),
                version.getNid(),
                version.toString()
        );

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>(content)
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyVersionCollectionRootRestEntity>> encodeTaxonomyCollectionVersions(URI uri, List<ITaxonomyCollectionVersion> version) {
        ArrayList<TaxonomyVersionCollectionDataRestEntity> content = version
                .stream()
                .map(e -> new TaxonomyVersionCollectionDataRestEntity(
                        e.getVersion(),
                        e.getNid(),
                        URIUtil.fromURI(uri).append(e.getVersion()).stringValue())
                )
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyVersionCollectionRootRestEntity>(new TaxonomyVersionCollectionRootRestEntity(content))
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyDataRestEntity>> encodeTaxonomyCollectionVersionURN(URI uri, ITaxonomyCollectionVersionURN urn) {
        TaxonomyDataRestEntity content = new TaxonomyDataRestEntity(
                urn.getUrn(),
                urn.getTitle(),
                urn.getUrl(),
                urn.getDescription(),
                uri.toString(),
                urn.getNid(),
                TaxonomyDataRestEntity.ReferenceType.valueOf(urn.getType().toUpperCase())
        );

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyDataRestEntity>(content)
        );
    }

    public Mono<Object> encodeMetadataDocument(URI uri, IDocument object) {
        return Mono.just(
                object.getBody()
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyTreeRootRestEntity>> encodeTaxonomyCollectionVersionURNs(URI uri, List<ITaxonomyCollectionVersionURN> urns) {
        TaxonomyTreeBuilder taxonomyTreeBuilder = TaxonomyTreeBuilder.empty();

        urns
                .stream()
                .map(e -> {
                    if (e.getType() == null
                            || !("REFERENCE").equalsIgnoreCase(e.getType())) {
                        return new TaxonomyTreeDataRestEntity(
                                e.getUrn(),
                                e.getTitle(),
                                e.getUrl(),
                                e.getDescription(),
                                URIUtil.fromURI(uri).append(e.getUrn()).stringValue(),
                                e.getVersion(),
                                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                                null
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(taxonomyTreeBuilder::insert);

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyTreeRootRestEntity>(new TaxonomyTreeRootRestEntity(taxonomyTreeBuilder.getTree()))
        );
    }

    @Override
    public Mono<JsonApiRestResponseWrapper<TaxonomyCollectionRootRestEntity>> encodeTaxonomyCollections(URI uri, List<Tuple2<ITaxonomyCollection,ITaxonomyCollectionVersion>> collection) {
        ArrayList<TaxonomyCollectionDataRestEntity> content = collection
                .stream()
                .map(e -> {
                    JsonApiRelationships relationships = new JsonApiRelationships();
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_COLLECTION, URIUtil.fromURI(uri).append(e.getT1().getNid()).append("version").uriValue());
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.VERSION_ELEMENT, URIUtil.fromURI(uri).append(e.getT1().getNid()).append("version").append(e.getT2().getVersion()).uriValue());
                    relationships.addRelationshipRef(JsonApiRestResponseWrapper.URN_COLLECTION, URIUtil.fromURI(uri).append(e.getT1().getNid()).append("version").append(e.getT2().getVersion()).append("urn").uriValue());

                    return new TaxonomyCollectionDataRestEntity(
                            e.getT1().getName(),
                            e.getT1().getDescription(),
                            e.getT1().getNid(),
                            URIUtil.fromURI(uri).append(e.getT1().getNid()).stringValue(),
                            relationships
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<TaxonomyCollectionRootRestEntity>(new TaxonomyCollectionRootRestEntity(content))
        );
    }

    /* Votes */
    @Override
    public Mono<JsonApiRestResponseWrapper<VoteRootRestEntity>> encodeVotes(URI uri, List<IVote> votes){
        ArrayList<VoteDataRestEntity> content = votes
                .stream()
                .map(e -> {
                    return new VoteDataRestEntity(
                            e.getRating()
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.just(
                new JsonApiRestResponseWrapper<VoteRootRestEntity>(new VoteRootRestEntity(content))
        );
    }

    public Mono<JsonApiRestResponseWrapper<VoteDataRestEntity>> encodeVote(URI uri, IVote vote){
        VoteDataRestEntity content = new VoteDataRestEntity(
                vote.getRating()
        );

        return Mono.just(
                new JsonApiRestResponseWrapper<VoteDataRestEntity>(content)
        );
    }
}
