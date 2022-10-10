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

import io.apimap.api.repository.interfaces.ITaxonomyCollection;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Document
public class TaxonomyCollection implements ITaxonomyCollection {

    protected String name;
    protected String nid;
    protected String description;
    protected String token;
    protected Instant created;

    @Id
    private String id;

    public TaxonomyCollection() {
    }

    public TaxonomyCollection(final String name,
                              final String nid,
                              final String description,
                              final String token) {
        this.name = name;
        this.nid = nid;
        this.description = description;
        this.token = token;
        this.id = ITaxonomyCollection.createId(nid);
    }

    public void generateToken() {
        this.token = UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

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
    public String toString() {
        return "TaxonomyCollection{" +
                "name='" + name + '\'' +
                ", nid='" + nid + '\'' +
                ", description='" + description + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
