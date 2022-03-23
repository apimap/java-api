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

package io.apimap.api.repository;

import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.query.Filter;
import io.apimap.api.repository.nitrite.entity.query.QueryFilter;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ApiVersionCollection;

import java.util.List;
import java.util.Optional;

public interface IApiRepository {
    ApiCollection all();
    ApiCollection all(List<Filter> filters, QueryFilter queryFilter);
    ApiCollection filteredCollection(List<String> ids, List<String> parents);
    Optional<Api> add(Api entity);
    Optional<Api> update(Api entity, String apiName);
    Optional<Api> get(String apiName);
    Optional<Api> get(String apiName, Boolean returnWithToken);
    void delete(String apiName);
    String apiId(String apiName);
    Integer numberOfApis();
    Optional<ApiVersion> getLatestApiVersion(String apiId);
    void deleteApiVersion(String apiName, String apiVersion);
    Optional<ApiVersion> getApiVersion(String apiId, String apiVersion);
    ApiVersionCollection allApiVersions(String apiId);
    Optional<ApiVersion> addApiVersion(ApiVersion entity);
}
