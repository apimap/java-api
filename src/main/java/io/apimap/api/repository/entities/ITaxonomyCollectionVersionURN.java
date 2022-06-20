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

import java.util.Date;

public interface ITaxonomyCollectionVersionURN {

    static String createId(String urn, String version) {
        return urn + "#" + version;
    }

    String getUrl();

    void setUrl(String url);

    String getTitle();

    void setTitle(String title);

    String getDescription();

    void setDescription(String description);

    String getNid();

    void setNid(String nid);

    String getVersion();

    void setVersion(String version);

    String getUrn();

    void setUrn(String urn);

    String getType();

    void setType(String type);

    String getId();

    void setId(String id);

    Date getCreated();

    void setCreated(Date created);
}
