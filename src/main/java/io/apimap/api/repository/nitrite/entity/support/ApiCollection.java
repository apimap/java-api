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

package io.apimap.api.repository.nitrite.entity.support;

import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.db.Metadata;

import java.util.List;
import java.util.Optional;

public class ApiCollection {
    private final List<Item> items;
    private final List<String> parents;

    public ApiCollection(List<Item> items, List<String> parents) {
        this.items = items;
        this.parents = parents;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<String> getParents() {
        return parents;
    }

    @Override
    public String toString() {
        return "ApiCollection{" +
                "items=" + items +
                ", parents=" + parents +
                '}';
    }

    public static class Item {
        private Api api;
        private Optional<ApiVersion> version;
        private Optional<Metadata> metadata;

        public Item(Api api, Optional<ApiVersion> version, Optional<Metadata> metadata) {
            this.api = api;
            this.version = version;
            this.metadata = metadata;
        }

        public Api getApi() {
            return api;
        }

        public Optional<ApiVersion> getVersion() {
            return version;
        }

        public Optional<Metadata> getMetadata() {
            return metadata;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "api=" + api +
                    ", version=" + version +
                    ", metadata=" + metadata +
                    '}';
        }
    }
}
