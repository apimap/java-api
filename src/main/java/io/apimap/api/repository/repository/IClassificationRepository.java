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

import io.apimap.api.repository.interfaces.IApiClassification;
import io.apimap.api.service.query.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IClassificationRepository<TApiClassification extends IApiClassification,
                                           OB> {
    /* C */
    Flux<TApiClassification> all();

    Flux<TApiClassification> allByFilters(Mono<List<OB>> filters);

    Flux<TApiClassification> all(String apiId);

    Flux<TApiClassification> all(String apiId, String apiVersion);

    Mono<TApiClassification> update(TApiClassification entity, String apiId);

    Mono<TApiClassification> add(TApiClassification entity);

    Mono<TApiClassification> get(String apiId, String api, String taxonomyUrn);

    Flux<TApiClassification> allByURN(String taxonomyUrn);

    Mono<Boolean> delete(String apiId, String apiVersion);

    Mono<Boolean> delete(String apiId);

    /* OB */
    Mono<List<OB>> queryFilters(List<Filter> filters);
}
