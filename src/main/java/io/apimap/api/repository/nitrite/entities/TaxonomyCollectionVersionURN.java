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

package io.apimap.api.repository.nitrite.entities;

import io.apimap.api.repository.entities.ITaxonomyCollectionVersionURN;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.Date;

import static io.apimap.api.repository.repository.ITaxonomyRepository.DEFAULT_TAXONOMY_TYPE;

@Indices({
        @Index(value = "urn", type = IndexType.Unique)
})
public class TaxonomyCollectionVersionURN implements ITaxonomyCollectionVersionURN {
    protected String url;
    protected String title;
    protected String description;
    protected String nid;
    protected String version;
    protected String urn;
    protected String type;
    protected Date created;

    @Id
    private String id;

    public TaxonomyCollectionVersionURN() {
    }

    public TaxonomyCollectionVersionURN(String url,
                                        String title,
                                        String description,
                                        String nid,
                                        String version,
                                        String urn,
                                        String type) {
        this.urn = urn;
        this.url = url;
        this.title = title;
        this.description = description;
        this.version = version;
        this.nid = nid;
        this.type = type == null ? DEFAULT_TAXONOMY_TYPE : type;
        this.id = ITaxonomyCollectionVersionURN.createId(urn, version);
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getNid() {
        return nid;
    }

    @Override
    public void setNid(String nid) {
        this.nid = nid;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getUrn() {
        return urn;
    }

    @Override
    public void setUrn(String urn) {
        this.urn = urn;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setType(String type) {
        this.type = type == null ? DEFAULT_TAXONOMY_TYPE : type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersionURN)) {
            return false;
        }

        return ((io.apimap.api.repository.nitrite.entities.TaxonomyCollectionVersionURN) o).getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "TaxonomyCollectionVersionURN{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", nid='" + nid + '\'' +
                ", version='" + version + '\'' +
                ", urn='" + urn + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
