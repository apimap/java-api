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

package io.apimap.api.repository;

import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.support.MetadataCollection;
import org.dizitart.no2.objects.ObjectFilter;

import java.util.List;
import java.util.Optional;

public interface IMetadataRepository {
    void clear();
    MetadataCollection filteredCollection(List<ObjectFilter> filters);
    MetadataCollection all(String apiId);
    MetadataCollection all();
    Optional<Metadata> add(Metadata entity);
    Optional<Metadata> update(Metadata entity);
    Optional<Metadata> get(String apiId, String version);
    void delete(String apiId);
    void delete(String apiId, String version);
}
