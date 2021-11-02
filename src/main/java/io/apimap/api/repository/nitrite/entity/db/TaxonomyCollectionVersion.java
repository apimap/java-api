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

@Indices({
        @Index(value = "nid", type = IndexType.NonUnique)
})
public class TaxonomyCollectionVersion {
    protected String nid;
    protected String version;

    protected Date created;

    @Id
    private String id;

    public TaxonomyCollectionVersion(String nid, String version) {
        this.version = version;
        this.nid = nid;
        this.created = new Date();
        this.id = createId(nid, version);
    }

    public String getId() {
        return id;
    }

    public String getNid() {
        return nid;
    }

    public String getVersion() {
        return version;
    }

    protected String createId(String nid, String version) {
        return nid + "#" + version;
    }

    @Override
    public String toString() {
        return "TaxonomyCollectionVersion{" +
                "id='" + id + '\'' +
                ", nid='" + nid + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
