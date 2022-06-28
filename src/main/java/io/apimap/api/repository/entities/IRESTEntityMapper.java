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

package io.apimap.api.repository.entities;

import io.apimap.api.repository.generic.ClassificationCollection;
import io.apimap.api.repository.generic.StatisticsCollection;
import io.apimap.api.repository.generic.StatisticsValue;
import io.apimap.api.rest.ApiCollectionRootRestEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionCollectionRootRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.ClassificationDataRestEntity;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.ClassificationTreeRootRestEntity;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.StatisticsCollectionCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsCollectionDataRestEntity;
import io.apimap.api.rest.StatisticsCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsDataRestEntity;
import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyCollectionRootRestEntity;
import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeRootRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyVersionCollectionRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.context.TaxonomyContext;
import io.apimap.api.utils.URIUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface IRESTEntityMapper {
    String SUPPORTED_METADATA_VERSION = "1";

    default void validateApiDataRestEntity(ApiDataRestEntity object) {
        if (object.getName() == null || object.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field 'name' missing");
        }
    }

    default Mono<JsonApiRestResponseWrapper<StatisticsCollectionRootRestEntity>> encodeStatistics(URI uri, List<StatisticsValue> statistics) {
        ArrayList<StatisticsDataRestEntity> items = statistics
                .stream()
                .map(e -> new StatisticsDataRestEntity(
                        e.getKey(),
                        e.getKey(),
                        e.getValue()
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.justOrEmpty(
                new JsonApiRestResponseWrapper<>(new StatisticsCollectionRootRestEntity(items))
        );
    }

    default Mono<JsonApiRestResponseWrapper<StatisticsCollectionCollectionRootRestEntity>> encodeStatisticsCollection(URI uri, List<StatisticsCollection> collections) {
        ArrayList<StatisticsCollectionDataRestEntity> items = collections
                .stream()
                .map(e -> new StatisticsCollectionDataRestEntity(
                        e.getId(),
                        e.getDescription(),
                        URIUtil.fromURI(uri).append(e.getId()).stringValue()
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        return Mono.justOrEmpty(
                new JsonApiRestResponseWrapper<>(new StatisticsCollectionCollectionRootRestEntity(items))
        );
    }

    /*
     From REST
     */

    /* API */
    Mono<IApi> decodeApi(JsonApiRestRequestWrapper<ApiDataRestEntity> object);

    /* API Version */
    Mono<IApiVersion> decodeApiVersion(IApi api, JsonApiRestRequestWrapper<ApiVersionDataRestEntity> object);

    /* Metadata */
    Mono<IMetadata> decodeMetadata(ApiContext apiContext, JsonApiRestRequestWrapper<MetadataDataRestEntity> object);

    /* Taxonomy */
    Mono<ITaxonomyCollection> decodeTaxonomyCollection(JsonApiRestRequestWrapper<TaxonomyCollectionDataRestEntity> object);

    Mono<ITaxonomyCollectionVersion> decodeTaxonomyCollectionVersion(JsonApiRestRequestWrapper<TaxonomyVersionCollectionDataRestEntity> object);

    Mono<ITaxonomyCollectionVersionURN> decodeTaxonomyCollectionVersionURN(TaxonomyContext taxonomyContext, JsonApiRestRequestWrapper<TaxonomyDataRestEntity> object);

    /* ApiClassification */
    Mono<IApiClassification> decodeClassification(ApiContext apiContext, ClassificationDataRestEntity object);

    /*
        To REST
     */

    /* API */
    Mono<JsonApiRestResponseWrapper<ApiDataRestEntity>> encodeApi(URI uri, IApi api);

    Mono<JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>> encodeApis(URI uri, List<reactor.util.function.Tuple3<IApi, IMetadata, IApiVersion>> apis);

    /* API Version */
    Mono<JsonApiRestResponseWrapper<ApiVersionDataRestEntity>> encodeApiVersion(URI uri, IApiVersion version);

    Mono<JsonApiRestResponseWrapper<ApiVersionCollectionRootRestEntity>> encodeApiVersions(URI uri, List<IApiVersion> versions);

    /* Metadata */
    Mono<JsonApiRestResponseWrapper<MetadataDataRestEntity>> encodeMetadata(URI uri, IMetadata object);

    /* API Classification */
    Mono<JsonApiRestResponseWrapper<ClassificationRootRestEntity>> encodeApiClassifications(URI uri, List<IApiClassification> classifications);

    /* Classification */
    Mono<JsonApiRestResponseWrapper<ClassificationTreeRootRestEntity>> encodeClassifications(URI uri, List<ClassificationCollection> classifications);

    /* Taxonomy */
    Mono<JsonApiRestResponseWrapper<TaxonomyCollectionDataRestEntity>> encodeTaxonomyCollection(URI uri, ITaxonomyCollection collection);

    Mono<JsonApiRestResponseWrapper<TaxonomyCollectionRootRestEntity>> encodeTaxonomyCollections(URI uri, List<ITaxonomyCollection> collections);

    Mono<JsonApiRestResponseWrapper<TaxonomyVersionCollectionDataRestEntity>> encodeTaxonomyCollectionVersion(URI uri, ITaxonomyCollectionVersion version);

    Mono<JsonApiRestResponseWrapper<TaxonomyVersionCollectionRootRestEntity>> encodeTaxonomyCollectionVersions(URI uri, List<ITaxonomyCollectionVersion> versions);

    Mono<JsonApiRestResponseWrapper<TaxonomyDataRestEntity>> encodeTaxonomyCollectionVersionURN(URI uri, ITaxonomyCollectionVersionURN urn);

    Mono<JsonApiRestResponseWrapper<TaxonomyTreeRootRestEntity>> encodeTaxonomyCollectionVersionURNs(URI uri, List<ITaxonomyCollectionVersionURN> urns);
}