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

import io.apimap.api.repository.interfaces.IMetadata;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document
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
        this.documentation = new ArrayList<String>(documentation);
        this.id = IMetadata.createId(apiId, apiVersion);
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getInterfaceDescriptionLanguage() {
        return interfaceDescriptionLanguage;
    }

    public void setInterfaceDescriptionLanguage(String interfaceDescriptionLanguage) {
        this.interfaceDescriptionLanguage = interfaceDescriptionLanguage;
    }

    public String getArchitectureLayer() {
        return architectureLayer;
    }

    public void setArchitectureLayer(String architectureLayer) {
        this.architectureLayer = architectureLayer;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getMetadataVersion() {
        return metadataVersion;
    }

    public void setMetadataVersion(String metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getInterfaceSpecification() {
        return interfaceSpecification;
    }

    public void setInterfaceSpecification(String interfaceSpecification) {
        this.interfaceSpecification = interfaceSpecification;
    }

    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    public void setSystemIdentifier(String systemIdentifier) {
        this.systemIdentifier = systemIdentifier;
    }

    public List<String> getDocumentation() {
        return new ArrayList<String>(documentation);
    }

    public void setDocumentation(List<String> documentation) {
        this.documentation = new ArrayList<String>(documentation);
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
