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

import io.apimap.api.repository.entities.IApi;
import io.apimap.api.repository.entities.IApiVersion;
import io.apimap.api.service.query.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IApiRepository<TIApi extends IApi,
                                TIApiVersion extends IApiVersion,
                                OB> {
    /* A */
    Flux<TIApi> all();

    Flux<TIApi> allByFilters(Mono<List<OB>> filters);

    Flux<TIApi> allByApiIds(List<String> apiIds);

    Mono<TIApi> add(TIApi entity);

    Mono<TIApi> update(TIApi entity, String apiName);

    Mono<TIApi> get(String apiName);

    Mono<TIApi> getById(String apiId);

    Mono<Boolean> delete(String apiName);

    Mono<Long> numberOfApis();

    /* AV */
    Mono<TIApiVersion> getLatestApiVersion(String apiId);

    Mono<Boolean> deleteApiVersion(String apiId, String apiVersion);

    Mono<TIApiVersion> getApiVersion(String apiId, String apiVersion);

    Flux<TIApiVersion> allApiVersions(String apiId);

    Mono<TIApiVersion> addApiVersion(TIApiVersion entity);

    /* OB */
    Mono<List<OB>> queryFilters(List<Filter> filters);

}
