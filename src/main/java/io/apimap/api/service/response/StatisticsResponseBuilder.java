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

package io.apimap.api.service.response;

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.nitrite.entity.support.StatisticsCollectionCollection;
import io.apimap.api.repository.nitrite.entity.support.StatisticsValueCollection;
import io.apimap.api.rest.StatisticsCollectionCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsCollectionDataRestEntity;
import io.apimap.api.rest.StatisticsCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.utils.URIUtil;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class StatisticsResponseBuilder extends ResponseBuilder<StatisticsResponseBuilder> {
    public StatisticsResponseBuilder(long startTime, ApimapConfiguration apimapConfiguration) {
        super(startTime, apimapConfiguration);
    }

    public static StatisticsResponseBuilder builder(ApimapConfiguration apimapConfiguration) {
        return new StatisticsResponseBuilder(System.currentTimeMillis(), apimapConfiguration);
    }

    public StatisticsResponseBuilder withStatisticsValueCollectionBody(StatisticsValueCollection value) {
        ArrayList<StatisticsDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> new StatisticsDataRestEntity(
                        e.getKey(),
                        e.getKey(),
                        e.getValue()
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        this.body = new JsonApiRestResponseWrapper<>(new StatisticsCollectionRootRestEntity(items));
        return this;
    }

    public StatisticsResponseBuilder withStatisticsCollectionCollectionBody(StatisticsCollectionCollection value) {
        ArrayList<StatisticsCollectionDataRestEntity> items = value
                .getItems()
                .stream()
                .map(e -> new StatisticsCollectionDataRestEntity(
                        e.getId(),
                        e.getDescription(),
                        URIUtil.fromURI(resourceURI).append(e.getId()).stringValue()
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        this.body = new JsonApiRestResponseWrapper<>(new StatisticsCollectionCollectionRootRestEntity(items));
        return this;
    }
}
