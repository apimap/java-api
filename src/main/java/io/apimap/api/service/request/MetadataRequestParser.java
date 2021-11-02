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
import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.utils.RequestUtil;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MetadataRequestParser extends RequestParser<MetadataRequestParser> {
    protected IApiRepository apiRepository;

    public static MetadataRequestParser parser() {
        return new MetadataRequestParser();
    }

    public MetadataRequestParser withApiRepository(IApiRepository apiRepository) {
        this.apiRepository = apiRepository;
        return this;
    }

    public Optional<Metadata> metadatDbEntity() {
        final String apiId = apiRepository.apiId(RequestUtil.apiNameFromRequest(request));
        final String apiVersion = RequestUtil.apiVersionFromRequest(request);

        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, MetadataDataRestEntity.class);
        AtomicReference<MetadataDataRestEntity> entity = new AtomicReference<>();
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> entity.set((MetadataDataRestEntity) ((JsonApiRestRequestWrapper) result).getData()))
                .subscribe();

        if (entity.get() == null || entity.get() == null) {
            return Optional.empty();
        }

        return Optional.of(new Metadata(
                apiId,
                entity.get().getName(),
                entity.get().getDescription(),
                entity.get().getVisibility(),
                entity.get().getInterfaceDescriptionLanguage(),
                entity.get().getArchitectureLayer(),
                entity.get().getBusinessUnit(),
                apiVersion,
                "1",
                entity.get().getReleaseStatus(),
                entity.get().getInterfaceSpecification(),
                entity.get().getSystemIdentifier(),
                entity.get().getDocumentation(),
                new Date()
        ));
    }
}
