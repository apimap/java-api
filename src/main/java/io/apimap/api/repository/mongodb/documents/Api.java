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

import io.apimap.api.repository.interfaces.IApi;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document
public class Api implements IApi {

    protected String name;
    protected String codeRepositoryUrl;
    protected String token;
    protected Instant created;

    @Id
    private String id;

    public Api() {
    }

    public Api(final String name,
               final String codeRepositoryUrl) {
        this.token = null;
        this.name = name;
        this.codeRepositoryUrl = codeRepositoryUrl;
        this.id = IApi.createId();
        this.created = Instant.now();
    }

    public Api(final String name,
               final String codeRepositoryUrl,
               final String token) {
        this.token = token;
        this.name = name;
        this.codeRepositoryUrl = codeRepositoryUrl;
        this.id = IApi.createId();
        this.created = Instant.now();
    }

    public Api(final String name,
               final String codeRepositoryUrl,
               final String token,
               final Object created,
               final String id) {
        this.name = name;
        this.codeRepositoryUrl = codeRepositoryUrl;
        this.token = token;

        if(created instanceof Date){
            this.created = ((Date) created).toInstant();
        }else if(created instanceof Instant){
            this.created = (Instant) created;
        }else{
            this.created = Instant.now();
        }

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeRepositoryUrl() {
        return codeRepositoryUrl;
    }

    public void setCodeRepositoryUrl(String codeRepositoryUrl) {
        this.codeRepositoryUrl = codeRepositoryUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void generateToken() {
        this.token = IApi.createToken();
    }

    @Override
    public void clearToken() {
        this.token = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Instant getCreated() {
        return created;
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
