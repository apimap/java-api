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

package io.apimap.api.service.context;

import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.QueryFilter;

import java.util.List;

public class ApiContext {
    final protected String apiName;
    final protected String apiVersion;
    final protected List<Filter> filters;
    final protected QueryFilter query;
    protected String apiId;

    public ApiContext(String apiName, String apiVersion, List<Filter> filters, QueryFilter query) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.filters = filters;
        this.query = query;
    }

    public ApiContext(String apiName, String apiVersion, List<Filter> filters, QueryFilter query, String apiId) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.filters = filters;
        this.query = query;
        this.apiId = apiId;
    }

    public ApiContext withApiId(String apiId) {
        return new ApiContext(
                this.getApiName(),
                this.getApiVersion(),
                this.filters,
                this.query,
                apiId
        );
    }

    public String getApiName() {
        return apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiId() {
        return apiId;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public QueryFilter getQuery() {
        return query;
    }
}
