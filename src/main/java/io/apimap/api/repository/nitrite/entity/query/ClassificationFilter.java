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

package io.apimap.api.repository.nitrite.entity.query;

import org.dizitart.no2.objects.ObjectFilter;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

public class ClassificationFilter extends Filter {
    private String nid;

    public ClassificationFilter(String nid, String value) {
        super(value);
        this.nid = nid;
    }

    public String getKey() {
        return null;
    }

    public String getNid() {
        return this.nid;
    }

    @Override
    public ObjectFilter objectFilter() {
        return and(eq("taxonomyUrn", this.getValue()), eq("taxonomyNid", this.nid));
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
