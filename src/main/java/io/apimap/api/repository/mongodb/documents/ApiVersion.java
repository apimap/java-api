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

import io.apimap.api.repository.interfaces.IApiVersion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document
public class ApiVersion implements IApiVersion {

    protected String version;
    protected Instant created;
    protected String apiId;

    @Id
    private String id;

    public ApiVersion() {
    }

    public ApiVersion(final String version,
                      final Object created,
                      final String apiId) {
        this.version = version;
        this.apiId = apiId;

        if(created instanceof Date){
            this.created = ((Date) created).toInstant();
        }else if(created instanceof Instant){
            this.created = (Instant) created;
        }else{
            this.created = Instant.now();
        }

        this.id = IApiVersion.createId(apiId, version);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getCreated() {
        return created;
    }

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

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ApiVersion{" +
                "version='" + version + '\'' +
                ", created=" + created +
                ", apiId='" + apiId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
