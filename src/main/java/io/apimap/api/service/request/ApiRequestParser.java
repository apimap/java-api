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
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.rest.ApiDataMetadataEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.utils.RequestUtil;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Date;
import java.util.Optional;


public class ApiRequestParser extends RequestParser<ApiRequestParser> {

    protected IApiRepository apiRepository;
    protected Object body;

    public static ApiRequestParser parser() {
        return new ApiRequestParser();
    }

    public ApiRequestParser withApiRepository(IApiRepository nitriteApiRepository) {
        this.apiRepository = nitriteApiRepository;
        return this;
    }

    public ApiRequestParser parse(Class objClass) {
        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, objClass);
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> this.body = ((JsonApiRestRequestWrapper) result).getData())
                .subscribe();
        return this;
    }

    public Optional<ApiVersion> apiVersionDbEntity() {
        final String apiId = apiRepository.apiId(RequestUtil.apiNameFromRequest(request));
        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, ApiVersionDataRestEntity.class);
        request.bodyToMono(ParameterizedTypeReference.forType(type))
                .doOnNext(result -> this.body = ((JsonApiRestRequestWrapper) result).getData())
                .subscribe();

        if (this.body == null) {
            return Optional.empty();
        }

        return Optional.of(new ApiVersion(
                ((ApiVersionDataRestEntity)this.body).getVersion(),
                new Date(),
                apiId
        ));
    }


    public Optional<Api> apiDbEntity() {
        final ApiDataRestEntity entity = (ApiDataRestEntity) this.body;

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(new Api(
                entity.getName(),
                entity.getCodeRepository()
        ));
    }

    public Optional<ApiDataMetadataEntity> apiMetadata() {
        final ApiDataRestEntity entity = (ApiDataRestEntity) this.body;

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entity.getMeta());
    }
}
