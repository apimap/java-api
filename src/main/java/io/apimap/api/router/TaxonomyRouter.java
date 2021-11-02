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

import io.apimap.api.rest.TaxonomyCollectionDataRestEntity;
import io.apimap.api.rest.TaxonomyCollectionRootRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.TaxonomyResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class TaxonomyRouter {

    public static final String TAXONOMY_NID_KEY = "taxonomyNid";
    public static final String TAXONOMY_VERSION_KEY = "taxonomyVersion";
    public static final String TAXONOMY_URN_KEY = "urn";

    public static final String ROOT_PATH = "/taxonomy";
    public static final String COLLECTION_PATH = ROOT_PATH + "/{" + TAXONOMY_NID_KEY + "}";
    public static final String VERSIONED_PATH = COLLECTION_PATH + "/version";
    public static final String VERSIONED_ITEM_PATH = VERSIONED_PATH + "/{" + TAXONOMY_VERSION_KEY + "}";
    public static final String URN_PATH = VERSIONED_ITEM_PATH + "/urn";
    public static final String URN_ITEM_PATH = URN_PATH + "/{" + TAXONOMY_URN_KEY + "}";

    /*
     /taxonomy
     /taxonomy/{taxonomyNid}
     /taxonomy/{taxonomyNid}/version
     /taxonomy/{taxonomyNid}/version/{taxonomyVersion}
     /taxonomy/{taxonomyNid}/version/{taxonomyVersion}/urn
     /taxonomy/{taxonomyNid}/version/{taxonomyVersion}/urn/{urn}
    */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "allCollections",
                    operation = @Operation(
                            operationId = "allCollections",
                            summary = "Get all available taxonomy collections",
                            tags = {"TAXONOMY"},
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaxonomyCollectionRootRestEntity.class)))
                            )
                    )),
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.POST,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "createCollection",
                    operation = @Operation(
                            operationId = "createCollection",
                            summary = "Create a new taxonomy collection.",
                            description = "IMPORTANT: Note that upon creation this will return a token. This token is " +
                                    "linked with the collection and must be used as a Bearer token with all following" +
                                    " request. It is not possible to recreate this token, keep it in a safe place.",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Taxonomy Collection Created Successfully", content = @Content(schema = @Schema(implementation = TaxonomyCollectionDataRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "409", description = "Conflict, a API with the same name already exists", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = TaxonomyCollectionDataRestEntity.Attributes.class)))
                    )),
            @RouterOperation(
                    path = COLLECTION_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "deleteCollection",
                    operation = @Operation(
                            operationId = "deleteCollection",
                            summary = "Delete a taxonomy collection.",
                            description = "This deletes all taxonomy values associated with the taxonomy. APIs already " +
                                    "classified with one of the options will retain their classification, although they will not be searchable.",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Taxonomy Collection Deleted Successfully"),
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "taxonomyNid", description = "URL encoded taxonomy NID"),
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = COLLECTION_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "getCollection",
                    operation = @Operation(
                            operationId = "getCollection",
                            summary = "Get a taxonomy collection.",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Taxonomy Collection Content", content = @Content(schema = @Schema(implementation = TaxonomyCollectionDataRestEntity.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing taxonomy found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "taxonomyNid", description = "URL encoded taxonomy NID"),
                            }
                    )),
            @RouterOperation(
                    path = COLLECTION_PATH,
                    method = RequestMethod.POST,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "createVersion",
                    operation = @Operation(
                            operationId = "createVersion",
                            summary = "Create a new taxonomy collection version",
                            tags = {"TAXONOMY"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "taxonomyNid", description = "URL encoded taxonomy NID"),
                            },
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Taxonomy Collection Version Create", content = @Content(schema = @Schema(implementation = TaxonomyCollectionDataRestEntity.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = TaxonomyCollectionDataRestEntity.Attributes.class)))
                    )),
            @RouterOperation(
                    path = VERSIONED_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "allVersions",
                    operation = @Operation(
                            operationId = "allVersions",
                            summary = "Get all taxonomy collection versions",
                            tags = {"TAXONOMY"}
                    )),
            @RouterOperation(
                    path = VERSIONED_ITEM_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "getVersion",
                    operation = @Operation(operationId = "getVersion", summary = "Get a specific taxonomy collection version", tags = {"TAXONOMY"})),
            @RouterOperation(
                    path = VERSIONED_ITEM_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "deleteVersion",
                    operation = @Operation(
                            operationId = "deleteVersion",
                            summary = "Delete a taxonomy collection version",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = URN_PATH,
                    method = RequestMethod.POST,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "createURN",
                    operation = @Operation(
                            operationId = "createURN",
                            summary = "Create a new classification tree option",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = TaxonomyTreeDataRestEntity.Attributes.class)))
                    )),
            @RouterOperation(
                    path = URN_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "allURNs",
                    operation = @Operation(
                            operationId = "allURNs",
                            summary = "Get all classification tree options",
                            tags = {"TAXONOMY"}
                    )),
            @RouterOperation(
                    path = URN_ITEM_PATH,
                    method = RequestMethod.GET,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "getURN",
                    operation = @Operation(
                            operationId = "getURN",
                            summary = "Get a specific classification tree option",
                            tags = {"TAXONOMY"}
                    )),
            @RouterOperation(
                    path = URN_ITEM_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = TaxonomyResourceService.class,
                    beanMethod = "deleteURN",
                    operation = @Operation(
                            operationId = "deleteURN",
                            summary = "Delete a classification tree option",
                            tags = {"TAXONOMY"},
                            responses = {
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    ))
    })
    RouterFunction<ServerResponse> taxonomyRoutes(TaxonomyResourceService service) {
        return RouterFunctions
                .route(GET(ROOT_PATH).and(accept(APPLICATION_JSON)), service::allCollections)
                .andRoute(POST(ROOT_PATH), service::createCollection)
                .andRoute(DELETE(COLLECTION_PATH), service::deleteCollection)
                .andRoute(GET(COLLECTION_PATH), service::getCollection)
                .andRoute(POST(COLLECTION_PATH), service::createVersion)
                .andRoute(GET(VERSIONED_PATH).and(accept(APPLICATION_JSON)), service::allVersions)
                .andRoute(GET(VERSIONED_ITEM_PATH).and(accept(APPLICATION_JSON)), service::getVersion)
                .andRoute(DELETE(VERSIONED_ITEM_PATH), service::deleteVersion)
                .andRoute(POST(URN_PATH).and(contentType(APPLICATION_JSON)), service::createURN)
                .andRoute(GET(URN_PATH).and(accept(APPLICATION_JSON)), service::allURNs)
                .andRoute(GET(URN_ITEM_PATH).and(accept(APPLICATION_JSON)), service::getURN)
                .andRoute(DELETE(URN_ITEM_PATH), service::deleteURN);
    }
}
