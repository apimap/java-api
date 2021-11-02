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

package io.apimap.api.router;

import io.apimap.api.service.RootResourceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RootRouter {
    public static final String ROOT_PATH = "";

    /*
     /
     */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.GET,
                    beanClass = RootResourceService.class,
                    beanMethod = "rootResource",
                    operation = @Operation(
                            operationId = "API starting point",
                            summary = "HATEOAS starting point.",
                            description = "This endpoint is mostly to enable HATEOAS navigation.",
                            tags = {"START"}
                    )),
    })
    RouterFunction<ServerResponse> rootRoutes(RootResourceService service) {
        return RouterFunctions
                .route(GET(ROOT_PATH).and(accept(APPLICATION_JSON)), service::rootResource);
    }
}
