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

import io.apimap.api.repository.interfaces.ITaxonomyCollection;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.Date;
import java.util.UUID;

@Indices({
        @Index(value = "name", type = IndexType.Unique),
        @Index(value = "nid", type = IndexType.Unique)
})
public class TaxonomyCollection implements ITaxonomyCollection {
    protected String name;
    protected String nid;
    protected String description;
    protected String token;
    protected Date created;

    @Id
    private String id;

    public TaxonomyCollection() {
    }

    public TaxonomyCollection(String name,
                              String nid,
                              String description,
                              String token) {
        this.name = name;
        this.nid = nid;
        this.description = description;
        this.token = token;
        this.id = ITaxonomyCollection.createId(nid);
    }

    public TaxonomyCollection(String name,
                              String description,
                              String nid) {
        this.name = name;
        this.nid = nid;
        this.description = description;
        this.id = ITaxonomyCollection.createId(nid);
    }

    public void generateToken() {
        this.token = UUID.randomUUID().toString();
    }

    public void clearToken() {
        this.token = null;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
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
