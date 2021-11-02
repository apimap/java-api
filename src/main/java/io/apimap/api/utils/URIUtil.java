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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URIUtil {
    protected java.net.URI uri;

    public URIUtil(java.net.URI uri) {
        this.uri = uri;
    }

    public static URIUtil rootLevelFromURI(java.net.URI uri) {
        java.net.URI rootURI = null;

        try {
            if (uri.getPort() <= 0) {
                rootURI = new java.net.URI(uri.getScheme() + "://" + uri.getHost() + "/");
            } else {

                rootURI = new java.net.URI(uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/");
            }
        } catch (Exception e) {
            return null;
        }

        return new URIUtil(rootURI);
    }

    public static URIUtil fromURI(java.net.URI uri) {
        return new URIUtil(uri);
    }

    public static URIUtil taxonomyCollectionFromURI(java.net.URI uri) {
        if(URIUtil.rootLevelFromURI(uri) == null){
            return null;
        }else{
            return URIUtil.rootLevelFromURI(uri).append("taxonomy");
        }
    }

    public static URIUtil classificationCollectionFromURI(java.net.URI uri) {
        if(URIUtil.rootLevelFromURI(uri) == null){
            return null;
        }else {
            return URIUtil.rootLevelFromURI(uri).append("classification");
        }
    }

    public static URIUtil apiCollectionFromURI(java.net.URI uri) {
        if(URIUtil.rootLevelFromURI(uri) == null){
            return null;
        }else {
            return URIUtil.rootLevelFromURI(uri).append("api");
        }
    }

    public static URIUtil statisticsFromURI(java.net.URI uri) {
        if(URIUtil.rootLevelFromURI(uri) == null){
            return null;
        }else {
            return URIUtil.rootLevelFromURI(uri).append("statistics");
        }
    }

    public URIUtil append(String path) {
        try {
            path = URLEncoder.encode(path, StandardCharsets.UTF_8.toString());

            String basePath = this.uri.toString();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }

            this.uri = new java.net.URI(basePath + "/" + path);
        } catch (Exception e) {
            return this;
        }

        return this;
    }

    public String stringValue() {
        return this.uri.toString();
    }

    public java.net.URI uriValue() {
        return this.uri;
    }
}
