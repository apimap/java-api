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
import org.springframework.web.reactive.function.server.ServerRequest;

public class RequestUtil {

    private RequestUtil() {
    }

    public static String classificationFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ClassificationRouter.CLASSIFICATION_URN_KEY));
    }

    public static String apiNameFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ApiRouter.API_NAME_KEY));
    }

    public static String apiVersionFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(ApiRouter.API_VERSION_KEY));
    }

    public static String taxonomyNidFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_NID_KEY));
    }

    public static String taxonomyVersionFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_VERSION_KEY));
    }

    public static String taxonomyUrnFromRequest(ServerRequest request) {
        return URLDecodeEncodeUtil.urlDecodeString(request.pathVariable(TaxonomyRouter.TAXONOMY_URN_KEY));
    }

    public static String bearerTokenFromRequest(ServerRequest request) {
        if (request.headers().firstHeader("Authorization") == null) {
            return null;
        } else {
            return request.headers().firstHeader("Authorization").substring(7);
        }
    }
}
