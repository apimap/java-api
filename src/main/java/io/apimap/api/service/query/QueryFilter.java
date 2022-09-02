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

import java.util.Arrays;
import java.util.stream.Collectors;

public class QueryFilter extends Filter implements Cloneable {
    private final String field;

    public QueryFilter(String field, String value) {
        super(value);

        switch (field) {
            case MetadataDataRestEntity.NAME_KEY ->
                    this.field = MetadataDataRestEntity.NAME_KEY;
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY ->
                    this.field = MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY;
            case MetadataDataRestEntity.DESCRIPTION_KEY ->
                    this.field = MetadataDataRestEntity.DESCRIPTION_KEY;
            default -> this.field = null;
        }
    }

    @Override
    public String getKey() {
        return field;
    }

    public ObjectFilter objectFilter() {
        String queryString = createQueryString(getValue());

        return switch (this.field) {
            case MetadataDataRestEntity.NAME_KEY ->
                    org.dizitart.no2.objects.filters.ObjectFilters.regex("name", queryString);
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY ->
                    org.dizitart.no2.objects.filters.ObjectFilters.regex("systemIdentifier", queryString);
            case MetadataDataRestEntity.DESCRIPTION_KEY ->
                    org.dizitart.no2.objects.filters.ObjectFilters.regex("description", queryString);
            default -> null;
        };
    }

    @Override
    public Bson mongoObjectFilter() {
        String queryString = createQueryString(getValue());

        return switch (this.field) {
            case MetadataDataRestEntity.NAME_KEY ->
                    com.mongodb.client.model.Filters.regex("name", queryString, "i");
            case MetadataDataRestEntity.SYSTEM_IDENTIFIER_KEY ->
                    com.mongodb.client.model.Filters.regex("systemIdentifier", queryString, "i");
            case MetadataDataRestEntity.DESCRIPTION_KEY ->
                    com.mongodb.client.model.Filters.regex("description", queryString, "i");
            default -> null;
        };
    }

    @Override
    public TYPE type() {
        return TYPE.QUERY;
    }

    /*
    Split string by space and add * to enable a more generic find capability
     */
    protected String createQueryString(String string) {
        return Arrays.stream(string.split(" "))
                .collect(Collectors.joining("(.*)"));
    }

    @Override
    public String toString() {
        return "Query{" +
                "value='" + super.getValue() + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}
