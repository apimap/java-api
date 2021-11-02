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

import java.util.UUID;

@Indices({
        @Index(value = "name", type = IndexType.Unique)
})
public class TaxonomyCollection {
    protected String name;
    protected String nid;
    protected String description;
    protected String token;

    @Id
    private String id;

    public TaxonomyCollection() {
    }

    public TaxonomyCollection(String name, String description, String nid) {
        this.name = name;
        this.nid = nid;
        this.description = description;
        this.id = createId(nid);
    }

    public void generateToken() {
        this.token = UUID.randomUUID().toString();
    }

    public void clearToken() {
        this.token = null;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getNid() {
        return nid;
    }

    public String getId() {
        return id;
    }

    protected String createId(String nid) {
        return nid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
