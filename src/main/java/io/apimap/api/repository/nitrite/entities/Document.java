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

import io.apimap.api.repository.interfaces.IDocument;
import org.dizitart.no2.objects.Id;

import java.time.Instant;

public class Document implements IDocument {
    protected String apiId;
    protected String apiVersion;
    protected String body;
    protected Instant created;
    protected DocumentType type;

    @Id
    private String id;

    public Document() {
    }

    public Document(String apiId, String apiVersion, String body, Instant created, DocumentType type) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.body = body;
        this.created = created;
        this.type = type;
        this.id = IDocument.createId(apiId, apiVersion, type);
    }

    @Override
    public String getApiId() {
        return apiId;
    }

    @Override
    public void setApiId(String apiId) {
        this.apiId = apiId;
        this.id = IDocument.createId(this.apiId, this.apiVersion, this.type);
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        this.id = IDocument.createId(this.apiId, this.apiVersion, this.type);
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
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
    public DocumentType getType() {
        return type;
    }

    @Override
    public void setType(DocumentType type) {
        this.type = type;
        this.id = IDocument.createId(this.apiId, this.apiVersion, this.type);
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
        return "MetadataDocument{" +
                "apiId='" + apiId + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", body='" + body + '\'' +
                ", created=" + created +
                ", type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}
