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

import io.apimap.api.repository.entities.IApiVersion;
import org.dizitart.no2.objects.Id;

import java.util.Date;
import java.util.Objects;

public class ApiVersion implements IApiVersion {
    protected String version;
    protected Date created;
    protected String apiId;

    @Id
    private String id;

    public ApiVersion() {
    }

    public ApiVersion(final String apiVersion,
                      final Date created,
                      final String apiId) {
        this.version = apiVersion;
        this.created = created;
        this.apiId = apiId;
        this.id = IApiVersion.createId(apiId, apiVersion);
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
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String getApiId() {
        return apiId;
    }

    @Override
    public void setApiId(String apiId) {
        this.apiId = apiId;
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
        return "ApiVersion{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", created=" + created +
                ", apiId='" + apiId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiVersion that = (ApiVersion) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
