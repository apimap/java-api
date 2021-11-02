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

package io.apimap.api.service;

import io.apimap.api.repository.nitrite.entity.query.ClassificationQueryFilter;
import io.apimap.api.repository.nitrite.entity.query.MetadataQueryFilter;
import io.apimap.api.repository.nitrite.entity.query.QueryFilter;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilteredResourceService {
    public static final String FILTER_METADATA_KEY = "filter[metadata]";
    public static final String FILTER_CLASSIFICATION_KEY = "filter[classification]";

    protected List<QueryFilter> requestQueryFilters(ServerRequest request) {
        ArrayList<QueryFilter> returnValue = new ArrayList<>();

        request.queryParams().forEach((filterName, filterValue) -> {
            if (filterName.startsWith(FILTER_METADATA_KEY)) {
                String key = filterName.substring(FILTER_METADATA_KEY.length() + 1, filterName.length() - 1);
                filterValue.forEach(distinctValue -> {
                    Arrays.stream(distinctValue.split(",")).distinct().forEach(splitDistinctValue -> {
                        MetadataQueryFilter queryFilter = new MetadataQueryFilter(key, splitDistinctValue);
                        returnValue.add(queryFilter);
                    });
                });
            }

            if (filterName.startsWith(FILTER_CLASSIFICATION_KEY)) {
                String key = filterName.substring(FILTER_CLASSIFICATION_KEY.length() + 1, filterName.length() - 1);
                filterValue.forEach(distinctValue -> {
                    Arrays.stream(distinctValue.split(",")).distinct().forEach(splitDistinctValue -> {
                        ClassificationQueryFilter queryFilter = new ClassificationQueryFilter(key, splitDistinctValue);
                        returnValue.add(queryFilter);
                    });
                });
            }
        });

        return returnValue;
    }
}
