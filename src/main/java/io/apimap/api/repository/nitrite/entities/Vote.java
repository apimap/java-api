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

import io.apimap.api.repository.interfaces.IVote;
import org.dizitart.no2.objects.Id;

import java.time.Instant;

public class Vote implements IVote {
    protected String apiId;
    protected String apiVersion;
    protected Integer rating;
    protected Instant created;

    @Id
    private String id;

    public Vote() {
    }

    public Vote(String apiId, String apiVersion, Integer rating, Instant created) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = created;
        this.id = IVote.createId();
    }

    public Vote(String apiId, String apiVersion, Integer rating) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = Instant.now();
        this.id = IVote.createId();
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer value) {
        this.rating = value;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
