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

package io.apimap.api.error;

import io.apimap.api.rest.jsonapi.JsonApiError;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class ErrorExceptionHandler extends AbstractErrorWebExceptionHandler {
    public ErrorExceptionHandler(ErrorAttributes errorAttributes,
                                 ServerCodecConfigurer serverCodecConfigurer,
                                 WebProperties webproperties,
                                 ApplicationContext applicationContext) {
        super(errorAttributes, webproperties.getResources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.EXCEPTION
        ));

        JsonApiRestResponseWrapper<JsonApiError> wrapper = new JsonApiRestResponseWrapper<JsonApiError>();
        wrapper.addErorr(new JsonApiError(
                errorPropertiesMap.get("message").toString(),
                errorPropertiesMap.get("error").toString()
        ));

        return ServerResponse.status(HttpStatus.valueOf((Integer) errorPropertiesMap.get("status")))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(org.springframework.boot.web.reactive.error.ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
}
