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

import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;

import java.util.List;

public class TaxonomyCollectionVersionURNCollection {
    private final List<TaxonomyCollectionVersionURN> items;
    private final String taxonomyVersion;

    public TaxonomyCollectionVersionURNCollection(List<TaxonomyCollectionVersionURN> items, String taxonomyVersion) {
        this.items = items;
        this.taxonomyVersion = taxonomyVersion;
    }

    public String getTaxonomyVersion() {
        return taxonomyVersion;
    }

    public List<TaxonomyCollectionVersionURN> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "TaxonomyCollectionVersionURNCollection{" +
                "items=" + items +
                ", taxonomyVersion='" + taxonomyVersion + '\'' +
                '}';
    }
}
