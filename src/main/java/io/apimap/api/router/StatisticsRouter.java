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

import io.apimap.api.rest.StatisticsCollectionDataRestEntity;
import io.apimap.api.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.HashMap;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class StatisticsRouter {

    public static final String ROOT_PATH = "/statistics";
    public static final String API_COUNT_STATISTICS_PATH = ROOT_PATH + "/apis";
    public static final String TAXONOMIES_COUNT_STATISTICS_PATH = ROOT_PATH + "/taxonomies";
    public static final String INTERFACE_SPECIFICATION_STATISTICS_PATH = ROOT_PATH + "/interface-specification";
    public static final String ARCHITECTURE_LAYER_STATISTICS_PATH = ROOT_PATH + "/architecture-layer";

    /*
     /statistics
     /statistics/apis
     /statistics/taxonomies
     /statistics/interface-specification
     /statistics/architecture-layer
    */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.GET,
                    beanClass = StatisticsService.class,
                    beanMethod = "allStatistics",
                    operation = @Operation(
                            operationId = "allStatistics",
                            summary = "System statistics overview.",
                            tags = {"STATISTICS"},
                            responses = {
                                @ApiResponse(responseCode = "200", description = "Statistic collection", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatisticsCollectionDataRestEntity.class)))),
                            }
                    )),
            @RouterOperation(
                    path = API_COUNT_STATISTICS_PATH,
                    method = RequestMethod.GET,
                    beanClass = StatisticsService.class,
                    beanMethod = "getApiCountStatistics",
                    operation = @Operation(
                            operationId = "getApiCountStatistics",
                            summary = "Number of APIs registered in the system.",
                            tags = {"STATISTICS"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Statistic collection", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HashMap.class)))),
                            }
                    )),
            @RouterOperation(
                    path = TAXONOMIES_COUNT_STATISTICS_PATH,
                    method = RequestMethod.GET,
                    beanClass = StatisticsService.class,
                    beanMethod = "getTaxonomiesStatistics",
                    operation = @Operation(
                            operationId = "getTaxonomiesStatistics",
                            summary = "Number of taxonomies registered in the system.",
                            tags = {"STATISTICS"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Statistic collection", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HashMap.class)))),
                            }
                    )),
            @RouterOperation(
                    path = INTERFACE_SPECIFICATION_STATISTICS_PATH,
                    method = RequestMethod.GET,
                    beanClass = StatisticsService.class,
                    beanMethod = "getInterfaceSpecificationStatistics",
                    operation = @Operation(
                            operationId = "getInterfaceSpecificationStatistics",
                            summary = "Number of APIs registered grouped by interface specification.",
                            tags = {"STATISTICS"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Statistic collection", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HashMap.class)))),
                            }
                    )),
            @RouterOperation(
                    path = ARCHITECTURE_LAYER_STATISTICS_PATH,
                    method = RequestMethod.GET,
                    beanClass = StatisticsService.class,
                    beanMethod = "getArchitectureLayerStatistics",
                    operation = @Operation(
                            operationId = "getArchitectureLayerStatistics",
                            summary = "Number of APIs registered grouped by architecture layer.",
                            tags = {"STATISTICS"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Statistic collection", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HashMap.class)))),
                            }
                    )),
    })
    RouterFunction<ServerResponse> statisticsRoutes(StatisticsService service) {
        return RouterFunctions
                .route(GET(ROOT_PATH).and(accept(APPLICATION_JSON)), service::allStatistics)
                .andRoute(GET(API_COUNT_STATISTICS_PATH), service::getApiCountStatistics)
                .andRoute(GET(TAXONOMIES_COUNT_STATISTICS_PATH), service::getTaxonomiesStatistics)
                .andRoute(GET(INTERFACE_SPECIFICATION_STATISTICS_PATH), service::getInterfaceSpecificationStatistics)
                .andRoute(GET(ARCHITECTURE_LAYER_STATISTICS_PATH), service::getArchitectureLayerStatistics);
    }
}
