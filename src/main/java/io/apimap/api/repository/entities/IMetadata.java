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

package io.apimap.api.repository.entities;

import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.MetadataFilter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.groupingBy;

public interface IMetadata {

    static String createId(String apiId, String apiVersion) {
        return apiId + "#" + apiVersion;
    }

    static boolean compliesWithFilters(final IMetadata metadata, final List<Filter> filters){
        if(filters == null || filters.size() < 1) return true;

        final Map<String, List<Filter>> orSortedFilters = filters
                .stream()
                .filter(filter -> filter instanceof MetadataFilter)
                .collect(groupingBy(Filter::getKey));

        AtomicBoolean value = new AtomicBoolean(true);

        orSortedFilters.forEach((key, list) -> {
            boolean orMatchingFilter = false;
            for( Filter filter : list){
                    if(filter.getKey().equals(MetadataDataRestEntity.NAME_KEY)) orMatchingFilter |= metadata.getName().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.VISIBILITY_KEY)) orMatchingFilter |= metadata.getVisibility().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.RELEASE_STATUS_KEY)) orMatchingFilter |= metadata.getReleaseStatus().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.INTERFACE_SPECIFICATION_KEY)) orMatchingFilter |= metadata.getInterfaceSpecification().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.INTERFACE_DESCRIPTION_LANGUAGE_KEY)) orMatchingFilter |= metadata.getInterfaceDescriptionLanguage().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.ARCHITECTURE_LAYER_KEY)) orMatchingFilter |= metadata.getArchitectureLayer().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.BUSINESS_UNIT_KEY)) orMatchingFilter |= metadata.getBusinessUnit().equals(filter.getValue());
                    if(filter.getKey().equals(MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY)) orMatchingFilter |= metadata.getSystemIdentifier().equals(filter.getValue());
            }
            value.set(value.get() & orMatchingFilter);
        });
        return value.get();
    }

    String getApiId();

    void setApiId(String apiId);

    String getDescription();

    void setDescription(String description);

    String getApiVersion();

    void setApiVersion(String apiVersion);

    String getName();

    void setName(String name);

    String getVisibility();

    void setVisibility(String visibility);

    String getInterfaceDescriptionLanguage();

    void setInterfaceDescriptionLanguage(String interfaceDescriptionLanguage);

    String getArchitectureLayer();

    void setArchitectureLayer(String architectureLayer);

    String getBusinessUnit();

    void setBusinessUnit(String businessUnit);

    String getMetadataVersion();

    void setMetadataVersion(String metadataVersion);

    String getReleaseStatus();

    void setReleaseStatus(String releaseStatus);

    String getInterfaceSpecification();

    void setInterfaceSpecification(String interfaceSpecification);

    String getSystemIdentifier();

    void setSystemIdentifier(String systemIdentifier);

    List<String> getDocumentation();

    void setDocumentation(List<String> documentation);

    Date getCreated();

    void setCreated(Date created);

    String getId();

    void setId(String id);
}
