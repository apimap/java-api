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

import java.util.Date;
import java.util.List;

@Indices({
        @Index(value = "apiId", type = IndexType.NonUnique),
        @Index(value = "apiVersion", type = IndexType.NonUnique)
})
public class Metadata {
    protected String apiId;
    protected String description;
    protected String apiVersion;
    protected String name;
    protected String visibility;
    protected String interfaceDescriptionLanguage;
    protected String architectureLayer;
    protected String businessUnit;
    protected String metadataVersion;
    protected String releaseStatus;
    protected String interfaceSpecification;
    protected String systemIdentifier;
    protected List<String> documentation;

    protected Date created;
    @Id
    private String id;

    public Metadata() {
    }

    public Metadata(String apiId) {
        this.apiId = apiId;
    }

    public Metadata(String apiId,
                    String name,
                    String description,
                    String visibility,
                    String interfaceDescriptionLanguage,
                    String architectureLayer,
                    String businessUnit,
                    String apiVersion,
                    String metadataVersion,
                    String releaseStatus,
                    String interfaceSpecification,
                    String systemIdentifier,
                    List<String> documentation,
                    Date created) {
        this.apiId = apiId;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.created = created;
        this.interfaceDescriptionLanguage = interfaceDescriptionLanguage;
        this.architectureLayer = architectureLayer;
        this.businessUnit = businessUnit;
        this.apiVersion = apiVersion;
        this.metadataVersion = metadataVersion;
        this.releaseStatus = releaseStatus;
        this.interfaceSpecification = interfaceSpecification;
        this.systemIdentifier = systemIdentifier;
        this.documentation = documentation;
        this.id = createId(apiId, apiVersion);
    }

    public Date getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    public String getApiId() {
        return apiId;
    }

    public String getName() {
        return name;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInterfaceDescriptionLanguage() {
        return interfaceDescriptionLanguage;
    }

    public String getArchitectureLayer() {
        return architectureLayer;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getMetadataVersion() {
        return metadataVersion;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public String getInterfaceSpecification() {
        return interfaceSpecification;
    }

    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    public List<String> getDocumentation() {
        return documentation;
    }

    private String createId(String apiId, String apiVersion) {
        return apiId + "#" + apiVersion;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "apiId='" + apiId + '\'' +
                ", description='" + description + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", name='" + name + '\'' +
                ", visibility='" + visibility + '\'' +
                ", interfaceDescriptionLanguage='" + interfaceDescriptionLanguage + '\'' +
                ", architectureLayer='" + architectureLayer + '\'' +
                ", businessUnit='" + businessUnit + '\'' +
                ", metadataVersion='" + metadataVersion + '\'' +
                ", releaseStatus='" + releaseStatus + '\'' +
                ", interfaceSpecification='" + interfaceSpecification + '\'' +
                ", systemIdentifier='" + systemIdentifier + '\'' +
                ", documentation=" + documentation +
                ", created=" + created +
                ", id='" + id + '\'' +
                '}';
    }
}
