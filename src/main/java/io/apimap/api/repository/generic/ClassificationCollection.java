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

package io.apimap.api.repository.generic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.repository.interfaces.IMetadata;
import io.apimap.api.repository.interfaces.ITaxonomyCollectionVersionURN;

import java.util.ArrayList;
import java.util.List;

public class ClassificationCollection {
    protected final ITaxonomyCollectionVersionURN taxonomy;
    protected final List<IMetadata> items;

    @SuppressFBWarnings
    public ClassificationCollection(final ITaxonomyCollectionVersionURN taxonomy,
                                    final List<IMetadata> items) {
        this.taxonomy = taxonomy;
        this.items = new ArrayList<IMetadata>(items);
    }

    @SuppressFBWarnings
    public ITaxonomyCollectionVersionURN getTaxonomy() {
        return taxonomy;
    }

    public String getId() {
        return this.taxonomy.getId();
    }

    public List<IMetadata> getItems() {
        return new ArrayList<>(items);
    }

    @Override
    public String toString() {
        return "ClassificationCollection{" +
                ", taxonomy=" + taxonomy +
                ", items=" + items +
                '}';
    }
}