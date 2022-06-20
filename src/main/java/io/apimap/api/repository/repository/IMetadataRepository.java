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

package io.apimap.api.repository.repository;

import io.apimap.api.repository.entities.IMetadata;
import io.apimap.api.repository.entities.ITaxonomyCollection;
import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.QueryFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMetadataRepository<TIMetadata extends IMetadata,
                                     OB> {
    /* M */
    Flux<TIMetadata> allByFilters(Mono<List<OB>> filters);

    Flux<TIMetadata> all();

    Flux<TIMetadata> allByApiId(String apiId);

    Mono<TIMetadata> add(TIMetadata entity);

    Mono<TIMetadata> update(TIMetadata entity);

    Mono<TIMetadata> get(String apiId, String apiVersion);

    Mono<Boolean> delete(String apiId);

    Mono<Boolean> delete(String apiId, String apiVersion);

    /* OB */
    Mono<List<OB>> queryFilters(List<Filter> filters, QueryFilter queryFilter);
}
