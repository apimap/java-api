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

package io.apimap.api.repository.mongodb.documents;

import io.apimap.api.repository.interfaces.ITaxonomyCollectionVersionURN;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

import static io.apimap.api.repository.repository.ITaxonomyRepository.DEFAULT_TAXONOMY_TYPE;

@Document
public class TaxonomyCollectionVersionURN implements ITaxonomyCollectionVersionURN {

    protected String url;
    protected String title;
    protected String description;
    protected String nid;
    protected String version;
    protected Instant created;
    protected String urn;
    protected String type;

    @Id
    private String id;

    public TaxonomyCollectionVersionURN() {
    }

    public TaxonomyCollectionVersionURN(final String url,
                                        final String title,
                                        final String description,
                                        final String nid,
                                        final String version,
                                        final String urn,
                                        final String type) {
        this.urn = urn;
        this.url = url;
        this.title = title;
        this.description = description;
        this.version = version;
        this.nid = nid;
        this.type = type == null ? DEFAULT_TAXONOMY_TYPE : type;
        this.id = ITaxonomyCollectionVersionURN.createId(urn, version);
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setCreated(Instant created) {
        this.created = created;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getNid() {
        return this.nid;
    }

    @Override
    public void setNid(String nid) {
        this.nid = nid;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getUrn() {
        return this.urn;
    }

    @Override
    public void setUrn(String urn) {
        this.url = urn;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type == null ? DEFAULT_TAXONOMY_TYPE : type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Instant getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        if(created != null){
            this.created = created.toInstant();
        }else{
            this.created = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof TaxonomyCollectionVersionURN)) {
            return false;
        }

        return ((TaxonomyCollectionVersionURN) o).getId().equals(this.getId());
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
