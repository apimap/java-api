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

import io.apimap.api.rest.ClassificationTreeDataRestEntity;
import io.apimap.api.service.ClassificationResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class ClassificationRouter {
    public static final String CLASSIFICATION_URN_KEY = "classificationURN";

    public static final String ROOT_PATH = "/classification";
    public static final String TREE_PATH = ROOT_PATH + "/{" + CLASSIFICATION_URN_KEY + "}";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.GET,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "allClassifications",
                    operation = @Operation(
                            operationId = "allClassifications",
                            summary = "Classification search entrypoint.",
                            description = "Please not that if no query is specified a blank array will always be returned. " +
                                    "This is done since a complete database dump shouldn't be of any interest.",
                            tags = {"CLASSIFICATION"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Search Completed Successfully", content = @Content(schema = @Schema(implementation = ClassificationTreeDataRestEntity.class))),
                            },
                            parameters = {
                                    @Parameter(
                                            name = "filter[metadata]",
                                            description = "Metadata filter built with the following expression: filter[metadata][<filter name>]=<filter value>. If more than one value use , to separate the values.",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            example = "filter[metadata][visibility]=Public"
                                    ),
                                    @Parameter(
                                            name = "filter[classification]",
                                            description = "Classification filter built with the following expression: filter[classification][<taxonomy name>]=<classification urn>. If more than one value use , to separate the values.",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            example = "filter[classification][apimap]=urn:apimap:89"
                                    )
                            }
                    )),
            @RouterOperation(
                    path = TREE_PATH,
                    method = RequestMethod.GET,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "getClassification",
                    operation = @Operation(
                            operationId = "getClassification",
                            summary = "List classification URN sub-nodes.",
                            description = "Please not that if no query is specified a blank array will always be returned. This is done since a complete database dump shouldn't be of any interest.",
                            tags = {"CLASSIFICATION"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "Classification URN", description = "Request a specific URN sub-tree.")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Search Completed Successfully", content = @Content(schema = @Schema(implementation = ClassificationTreeDataRestEntity.class))),
                            }
                    )),
    })
    RouterFunction<ServerResponse> classificationRoutes(ClassificationResourceService service) {
        return RouterFunctions
                .route(GET(ROOT_PATH).and(accept(APPLICATION_JSON)), service::allClassifications)
                .andRoute(GET(TREE_PATH).and(accept(APPLICATION_JSON)), service::getClassification);
    }
}
