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

import org.bson.conversions.Bson;
import org.dizitart.no2.objects.ObjectFilter;

public class ClassificationFilter extends Filter {
    private final String nid;

    public ClassificationFilter(String nid, String value) {
        super(value);
        this.nid = nid;
    }

    public String getKey() {
        return this.nid;
    }

    public String getNid() {
        return this.nid;
    }

    @Override
    public ObjectFilter objectFilter() {
        return org.dizitart.no2.objects.filters.ObjectFilters.and(
                org.dizitart.no2.objects.filters.ObjectFilters.eq("taxonomyUrn", this.getValue()),
                org.dizitart.no2.objects.filters.ObjectFilters.eq("taxonomyNid", this.nid)
        );
    }

    @Override
    public Bson mongoObjectFilter() {
        return com.mongodb.client.model.Filters.and(
                com.mongodb.client.model.Filters.eq("taxonomyUrn", this.getValue()),
                com.mongodb.client.model.Filters.eq("taxonomyNid", this.nid)
        );
    }

    @Override
    public TYPE type() {
        return TYPE.CLASSIFICATION;
    }

    @Override
    public String toString() {
        return "ClassificationQueryFilter{" +
                "nid='" + nid + '\'' +
                ", value='" + getValue() + '\'' +
                '}';
    }
}
