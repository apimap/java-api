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

import io.apimap.api.repository.interfaces.IMetadata;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Indices({
        @Index(value = "apiId", type = IndexType.NonUnique),
        @Index(value = "apiVersion", type = IndexType.NonUnique),
        @Index(value = "name", type = IndexType.Fulltext),
        @Index(value = "systemIdentifier", type = IndexType.Fulltext),
        @Index(value = "description", type = IndexType.Fulltext)
})
public class Metadata implements IMetadata {
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

    protected Instant created;
    @Id
    private String id;

    public Metadata() {
    }

    public Metadata(final String apiId) {
        this.apiId = apiId;
    }

    public Metadata(final String apiId,
                    final String description,
                    final String apiVersion,
                    final String name,
                    final String visibility,
                    final String interfaceDescriptionLanguage,
                    final String architectureLayer,
                    final String businessUnit,
                    final String metadataVersion,
                    final String releaseStatus,
                    final String interfaceSpecification,
                    final String systemIdentifier,
                    final List<String> documentation,
                    final Instant created) {
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
        this.documentation = new ArrayList<>(documentation);
        this.id = IMetadata.createId(apiId, apiVersion);
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
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public String getInterfaceDescriptionLanguage() {
        return interfaceDescriptionLanguage;
    }

    @Override
    public void setInterfaceDescriptionLanguage(String interfaceDescriptionLanguage) {
        this.interfaceDescriptionLanguage = interfaceDescriptionLanguage;
    }

    @Override
    public String getArchitectureLayer() {
        return architectureLayer;
    }

    @Override
    public void setArchitectureLayer(String architectureLayer) {
        this.architectureLayer = architectureLayer;
    }

    @Override
    public String getBusinessUnit() {
        return businessUnit;
    }

    @Override
    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    @Override
    public String getMetadataVersion() {
        return metadataVersion;
    }

    @Override
    public void setMetadataVersion(String metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    @Override
    public String getReleaseStatus() {
        return releaseStatus;
    }

    @Override
    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    @Override
    public String getInterfaceSpecification() {
        return interfaceSpecification;
    }

    @Override
    public void setInterfaceSpecification(String interfaceSpecification) {
        this.interfaceSpecification = interfaceSpecification;
    }

    @Override
    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    @Override
    public void setSystemIdentifier(String systemIdentifier) {
        this.systemIdentifier = systemIdentifier;
    }

    @Override
    public List<String> getDocumentation() {
        return new ArrayList<String>(documentation);
    }

    @Override
    public void setDocumentation(List<String> documentation) {
        this.documentation = new ArrayList<String>(documentation);
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
