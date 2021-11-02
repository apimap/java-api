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

package io.apimap.api.service.request;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.repository.nitrite.entity.db.ApiClassification;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.utils.RequestUtil;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ClassificationRequestParser extends RequestParser<ClassificationRequestParser> {
    protected String apiId;

    public static ClassificationRequestParser parser() {
        return new ClassificationRequestParser();
    }

    public ClassificationRequestParser withApiId(String apiId) {
        this.apiId = apiId;
        return this;
    }

    public Optional<List<ApiClassification>> classificationArray() {
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);

        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ClassificationRootRestEntity.class);
        AtomicReference<ClassificationRootRestEntity> classificationRootRestEntity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> classificationRootRestEntity.set((ClassificationRootRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();


        if (classificationRootRestEntity.get() == null) {
            return Optional.empty();
        }

        return Optional.of(classificationRootRestEntity.get()
                .getData()
                .stream()
                .map(e -> new ApiClassification(
                        apiId,
                        e.getUrn(),
                        e.getTaxonomyVersion(),
                        apiVersion,
                        new Date(),
                        e.getNid()))
                .collect(Collectors.toList()));
    }
}
