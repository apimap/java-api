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

import io.apimap.api.repository.interfaces.IApiClassification;
import org.dizitart.no2.objects.Id;

import java.time.Instant;

public class ApiClassification implements IApiClassification {
    protected String apiId;
    protected String apiVersion;
    protected String taxonomyVersion;
    protected String taxonomyUrn;
    protected String taxonomyNid;
    protected Instant created;

    @Id
    private String id;

    public ApiClassification() {
    }

    public ApiClassification(String apiId,
                             String apiVersion,
                             String taxonomyVersion,
                             String taxonomyUrn,
                             String taxonomyNid,
                             Instant created) {
        this.apiId = apiId;
        this.taxonomyUrn = taxonomyUrn;
        this.taxonomyVersion = taxonomyVersion;
        this.apiVersion = apiVersion;
        this.taxonomyNid = taxonomyNid;
        this.created = created;
        this.id = IApiClassification.createId(apiId, apiVersion, taxonomyUrn);
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
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String getTaxonomyVersion() {
        return taxonomyVersion;
    }

    @Override
    public void setTaxonomyVersion(String taxonomyVersion) {
        this.taxonomyVersion = taxonomyVersion;
    }

    @Override
    public String getTaxonomyUrn() {
        return taxonomyUrn;
    }

    @Override
    public void setTaxonomyUrn(String taxonomyUrn) {
        this.taxonomyUrn = taxonomyUrn;
    }

    @Override
    public String getTaxonomyNid() {
        return taxonomyNid;
    }

    @Override
    public void setTaxonomyNid(String taxonomyNid) {
        this.taxonomyNid = taxonomyNid;
    }

    @Override
    public Instant getCreated() {
        return created;
    }

    @Override
    public void setCreated(Instant created) {
        this.created = created;
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
        return "ApiClassification{" +
                "apiId='" + apiId + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", taxonomyVersion='" + taxonomyVersion + '\'' +
                ", taxonomyUrn='" + taxonomyUrn + '\'' +
                ", taxonomyNid='" + taxonomyNid + '\'' +
                ", created=" + created +
                ", id='" + id + '\'' +
                '}';
    }
}
