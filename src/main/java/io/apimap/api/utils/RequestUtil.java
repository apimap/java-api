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

package io.apimap.api.utils;

import io.apimap.api.router.ApiRouter;
import io.apimap.api.router.ClassificationRouter;
import io.apimap.api.router.TaxonomyRouter;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.context.AuthorizationContext;
import io.apimap.api.service.context.ClassificationContext;
import io.apimap.api.service.context.TaxonomyContext;
import io.apimap.api.service.query.*;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestUtil {

    public static final String FILTER_METADATA_KEY = "filter[metadata]";
    public static final String FILTER_CLASSIFICATION_KEY = "filter[classification]";
    public static final String FILTER_NAME_KEY = "filter[name]";
    public static final String QUERY_STRING = "query[value]";
    public static final String QUERY_FIELD = "query[field]";
    public static final int MAX_QUERY_VALUE_LENGTH = 100;
    public static final int MAX_QUERY_FIELD_LENGTH = 20;
    private RequestUtil() {
    }

    protected static QueryFilter requestQuery(ServerRequest request) {
        if (request.queryParam(QUERY_FIELD).isPresent()
                && request.queryParam(QUERY_STRING).isPresent()
                && request.queryParam(QUERY_FIELD).get().length() < MAX_QUERY_VALUE_LENGTH
                && request.queryParam(QUERY_STRING).get().length() < MAX_QUERY_FIELD_LENGTH) {
            return new QueryFilter(request.queryParam(QUERY_FIELD).get(), request.queryParam(QUERY_STRING).get());
        }

        return null;
    }

    protected static List<Filter> requestFilters(ServerRequest request) {
        ArrayList<Filter> returnValue = new ArrayList<>();

        request.queryParams().forEach((filterName, filterValue) -> {
            if (filterName.startsWith(FILTER_METADATA_KEY)) {
                String key = filterName.substring(FILTER_METADATA_KEY.length() + 1, filterName.length() - 1);
                filterValue.forEach(distinctValue -> {
                    Arrays.stream(distinctValue.split(",")).distinct().forEach(splitDistinctValue -> {
                        MetadataFilter queryFilter = new MetadataFilter(key, splitDistinctValue);
                        if (queryFilter.getKey() != null) {
                            returnValue.add(queryFilter);
                        }
                    });
                });
            }

            if (filterName.startsWith(FILTER_CLASSIFICATION_KEY)) {
                String key = filterName.substring(FILTER_CLASSIFICATION_KEY.length() + 1, filterName.length() - 1);
                filterValue.forEach(distinctValue -> {
                    Arrays.stream(distinctValue.split(",")).distinct().forEach(splitDistinctValue -> {
                        ClassificationFilter queryFilter = new ClassificationFilter(key, splitDistinctValue);
                        if (queryFilter.getKey() != null) {
                            returnValue.add(queryFilter);
                        }
                    });
                });
            }

            if (filterName.startsWith(FILTER_NAME_KEY)) {
                filterValue.forEach(distinctValue -> {
                    Arrays.stream(distinctValue.split(",")).distinct().forEach(splitDistinctValue -> {
                        NameFilter queryFilter = new NameFilter(splitDistinctValue);
                        returnValue.add(queryFilter);
                    });
                });
            }
        });

        return returnValue;
    }

    public static AuthorizationContext authorizationContextFromRequest(final ServerRequest request) {
        return new AuthorizationContext(
                RequestUtil.bearerTokenFromRequest(request)
        );
    }

    public static ClassificationContext classificationContextFromRequest(final ServerRequest request) {
        return new ClassificationContext(
                classificationFromRequest(request),
                requestFilters(request),
                requestQuery(request)
        );
    }

    public static TaxonomyContext taxonomyContextFromRequest(final ServerRequest request) {
        return new TaxonomyContext(
                taxonomyNidFromRequest(request),
                taxonomyVersionFromRequest(request),
                taxonomyUrnFromRequest(request)
        );
    }

    public static ApiContext apiContextFromRequest(final ServerRequest request) {
        return new ApiContext(
                apiNameFromRequest(request),
                apiVersionFromRequest(request),
                requestFilters(request),
                requestQuery(request)
        );
    }

    public static String classificationFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ClassificationRouter.CLASSIFICATION_URN_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String apiNameFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ApiRouter.API_NAME_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String apiVersionFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ApiRouter.API_VERSION_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String taxonomyNidFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_NID_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String taxonomyVersionFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_VERSION_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String taxonomyUrnFromRequest(final ServerRequest request) {
        try {
            return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_URN_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String bearerTokenFromRequest(final ServerRequest request) {
        if (request.headers().firstHeader("Authorization") == null) {
            return null;
        } else {
            return request.headers().firstHeader("Authorization").substring(7);
        }
    }
}
