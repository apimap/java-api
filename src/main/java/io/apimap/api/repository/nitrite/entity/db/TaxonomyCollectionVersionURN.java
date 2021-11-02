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

package io.apimap.api.repository.nitrite.entity.db;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@Indices({
        @Index(value = "urn", type = IndexType.Unique),
        @Index(value = "url", type = IndexType.Unique)
})
public class TaxonomyCollectionVersionURN {
    protected String url;
    protected String title;
    protected String description;
    protected String nid;
    protected String version;
    protected String urn;
    @Id
    private String id;

    public TaxonomyCollectionVersionURN() {
    }

    public TaxonomyCollectionVersionURN(String urn,
                                        String url,
                                        String title,
                                        String description,
                                        String nid,
                                        String version) {
        this.urn = urn;
        this.url = url;
        this.title = title;
        this.description = description;
        this.version = version;
        this.nid = nid;
        this.id = createId(urn, version);
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getNid() {
        return nid;
    }

    public String getVersion() {
        return version;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    protected String createId(String urn, String version) {
        return urn + "#" + version;
    }

    @Override
    public String toString() {
        return "TaxonomyCollectionVersionURN{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", nid='" + nid + '\'' +
                ", version='" + version + '\'' +
                ", urn='" + urn + '\'' +
                '}';
    }
}
