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

package io.apimap.api.service.query;

import io.apimap.api.rest.MetadataDataRestEntity;
import org.bson.conversions.Bson;
import org.dizitart.no2.objects.ObjectFilter;

public class MetadataFilter extends Filter {

    private String key;

    public MetadataFilter(String key, String value) {
        super(value);
        setKey(key);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        switch (key) {
            case MetadataDataRestEntity.NAME_KEY:
                this.key = MetadataDataRestEntity.NAME_KEY;
                break;
            case MetadataDataRestEntity.VISIBILITY_KEY:
                this.key = MetadataDataRestEntity.VISIBILITY_KEY;
                break;
            case MetadataDataRestEntity.RELEASE_STATUS_KEY:
                this.key = MetadataDataRestEntity.RELEASE_STATUS_KEY;
                break;
            case MetadataDataRestEntity.INTERFACE_SPECIFICATION_KEY:
                this.key = MetadataDataRestEntity.INTERFACE_SPECIFICATION_KEY;
                break;
            case MetadataDataRestEntity.INTERFACE_DESCRIPTION_LANGUAGE_KEY:
                this.key = MetadataDataRestEntity.INTERFACE_DESCRIPTION_LANGUAGE_KEY;
                break;
            case MetadataDataRestEntity.ARCHITECTURE_LAYER_KEY:
                this.key = MetadataDataRestEntity.ARCHITECTURE_LAYER_KEY;
                break;
            case MetadataDataRestEntity.BUSINESS_UNIT_KEY:
                this.key = MetadataDataRestEntity.BUSINESS_UNIT_KEY;
                break;
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY:
                this.key = MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY;
                break;
            default:
                this.key = null;
        }
    }

    public ObjectFilter objectFilter() {
        switch (this.key) {
            case MetadataDataRestEntity.NAME_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("name", this.getValue());
            case MetadataDataRestEntity.VISIBILITY_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("visibility", this.getValue());
            case MetadataDataRestEntity.RELEASE_STATUS_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("releaseStatus", this.getValue());
            case MetadataDataRestEntity.INTERFACE_SPECIFICATION_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("interfaceSpecification", this.getValue());
            case MetadataDataRestEntity.INTERFACE_DESCRIPTION_LANGUAGE_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("interfaceDescriptionLanguage", this.getValue());
            case MetadataDataRestEntity.ARCHITECTURE_LAYER_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("architectureLayer", this.getValue());
            case MetadataDataRestEntity.BUSINESS_UNIT_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("businessUnit", this.getValue());
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY:
                return org.dizitart.no2.objects.filters.ObjectFilters.eq("systemIdentifier", this.getValue());
            default:
                return null;
        }
    }

    @Override
    public Bson mongoObjectFilter() {
        switch (this.key) {
            case MetadataDataRestEntity.NAME_KEY:
                return com.mongodb.client.model.Filters.eq("name", this.getValue());
            case MetadataDataRestEntity.VISIBILITY_KEY:
                return com.mongodb.client.model.Filters.eq("visibility", this.getValue());
            case MetadataDataRestEntity.RELEASE_STATUS_KEY:
                return com.mongodb.client.model.Filters.eq("releaseStatus", this.getValue());
            case MetadataDataRestEntity.INTERFACE_SPECIFICATION_KEY:
                return com.mongodb.client.model.Filters.eq("interfaceSpecification", this.getValue());
            case MetadataDataRestEntity.INTERFACE_DESCRIPTION_LANGUAGE_KEY:
                return com.mongodb.client.model.Filters.eq("interfaceDescriptionLanguage", this.getValue());
            case MetadataDataRestEntity.ARCHITECTURE_LAYER_KEY:
                return com.mongodb.client.model.Filters.eq("architectureLayer", this.getValue());
            case MetadataDataRestEntity.BUSINESS_UNIT_KEY:
                return com.mongodb.client.model.Filters.eq("businessUnit", this.getValue());
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY:
                return com.mongodb.client.model.Filters.eq("systemIdentifier", this.getValue());
            default:
                return null;
        }
    }

    @Override
    public TYPE type() {
        return TYPE.METADATA;
    }

    @Override
    public String toString() {
        return "MetadataQueryFilter{" +
                "key='" + key + '\'' +
                ", value='" + getValue() + '\'' +
                '}';
    }
}
