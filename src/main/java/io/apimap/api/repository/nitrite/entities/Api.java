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

import io.apimap.api.repository.interfaces.IApi;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.time.Instant;

@Indices({
        @Index(value = "name", type = IndexType.NonUnique),
})
public class Api implements IApi {
    protected String name;
    protected String codeRepositoryUrl;
    protected String token;
    protected Instant created;

    @Id
    private String id;

    public Api() {
    }

    public Api(String name,
               String codeRepositoryUrl) {
        this.name = name;
        this.codeRepositoryUrl = codeRepositoryUrl;
        this.id = IApi.createId();
        this.created = Instant.now();
    }

    public Api(String name,
               String codeRepositoryUrl,
               String token) {
        this.name = name;
        this.codeRepositoryUrl = codeRepositoryUrl;
        this.id = IApi.createId();
        this.token = token;
        this.created = Instant.now();
    }

    public void generateToken() {
        this.token = IApi.createToken();
    }

    public void clearToken() {
        this.token = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodeRepositoryUrl() {
        return codeRepositoryUrl;
    }

    public void setCodeRepositoryUrl(String codeRepositoryUrl) {
        this.codeRepositoryUrl = codeRepositoryUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Api{" +
                "name='" + name + '\'' +
                ", codeRepositoryUrl='" + codeRepositoryUrl + '\'' +
                ", token='" + token + '\'' +
                ", created=" + created +
                ", id='" + id + '\'' +
                '}';
    }
}

