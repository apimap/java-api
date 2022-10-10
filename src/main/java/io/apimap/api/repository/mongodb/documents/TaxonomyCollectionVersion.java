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

import io.apimap.api.repository.interfaces.ITaxonomyCollectionVersion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document
public class TaxonomyCollectionVersion implements ITaxonomyCollectionVersion {

    protected String nid;
    protected String version;
    protected Instant created;

    @Id
    private String id;

    public TaxonomyCollectionVersion() {
    }

    public TaxonomyCollectionVersion(final String nid,
                                     final String version,
                                     final Object created) {
        this.version = version;
        this.nid = nid;

        if(created instanceof Date){
            this.created = ((Date) created).toInstant();
        }else if(created instanceof Instant){
            this.created = (Instant) created;
        }else{
            this.created = Instant.now();
        }

        this.id = ITaxonomyCollectionVersion.createId(nid, version);
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
    public Instant getCreated() {
        return this.created;
    }

    @Override
    public void setCreated(Instant created) {
        this.created = created;
    }

    public void setCreated(Date created) {
        if(created != null){
            this.created = created.toInstant();
        }else{
            this.created = null;
        }
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
    public String toString() {
        return "TaxonomyCollectionVersion{" +
                "nid='" + nid + '\'' +
                ", version='" + version + '\'' +
                ", created=" + created +
                ", id='" + id + '\'' +
                '}';
    }
}
