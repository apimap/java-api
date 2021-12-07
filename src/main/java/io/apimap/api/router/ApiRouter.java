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

import io.apimap.api.rest.ApiCollectionDataRestEntity;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.ClassificationDataRestEntity;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.ApiClassificationService;
import io.apimap.api.service.ApiMetadataService;
import io.apimap.api.service.ApiResourceService;
import io.apimap.api.service.ClassificationResourceService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class ApiRouter {

    public static final String API_NAME_KEY = "apiName";
    public static final String API_VERSION_KEY = "apiVersion";

    public static final String ROOT_PATH = "/api";
    public static final String ITEM_PATH = ROOT_PATH + "/{" + API_NAME_KEY + "}";
    public static final String VERSIONED_PATH = ITEM_PATH + "/version";
    public static final String VERSIONED_ITEM_PATH = VERSIONED_PATH + "/{" + API_VERSION_KEY + "}";
    public static final String METADATA_PATH = VERSIONED_ITEM_PATH + "/metadata";
    public static final String CLASSIFICATION_PATH = VERSIONED_ITEM_PATH + "/classification";

    /*
     /api
       - Query parameters:
           - filter[metadata][{attribute name}] = value
           - filter[classification][{taxonomy nid}] = [{urn}]
     /api/{apiName}
     /api/{apiName}/version
     /api/{apiName}/version/{version}/classification
     /api/{apiName}/version/{version}/metadata
    * */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.GET,
                    beanClass = ApiResourceService.class,
                    beanMethod = "allApis",
                    operation = @Operation(
                            operationId = "allApis",
                            summary = "Get all APIs registered in the system.",
                            tags = {"API"},
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
                            },
                            responses = {
                                @ApiResponse(responseCode = "200", description = "List of all available APIs", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiCollectionDataRestEntity.class))))
                            })),
            @RouterOperation(
                    path = ROOT_PATH,
                    method = RequestMethod.POST,
                    beanClass = ApiResourceService.class,
                    beanMethod = "createApi",
                    operation = @Operation(
                            operationId = "createApi",
                            summary = "Create a new API.",
                            description = "An API consist of a name, connected with multiple versions. Each version is then connected to the metadata and classifications. " +
                                    "IMPORTANT: Note that upon creation this will return a token. This token is linked " +
                                    "with the collection and must be used as a Bearer token with all following request." +
                                    "It is not possible to recreate this token, keep it in a safe place.",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "API created successfully", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "409", description = "Conflict, a API with the same name already exists", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ApiDataRestEntity.Attributes.class)))
                    )),
            @RouterOperation(
                    path = ITEM_PATH,
                    method = RequestMethod.GET,
                    beanClass = ApiResourceService.class,
                    beanMethod = "getApi",
                    operation = @Operation(
                            operationId = "getApi",
                            summary = "Get API information.",
                            description = "An API consist of a name, connected with multiple versions. Each version is then connected to the metadata and classifications.",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API root object", content = @Content(schema = @Schema(implementation = ApiDataRestEntity.class))),
                                    @ApiResponse(responseCode = "404", description = "API not found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name")
                            }
                    )),
            @RouterOperation(
                    path = ITEM_PATH,
                    method = RequestMethod.PUT,
                    beanClass = ApiResourceService.class,
                    beanMethod = "updateApi",
                    operation = @Operation(
                            operationId = "updateApi",
                            summary = "Update API information.",
                            description = "This does not include metadata, versions or classifications. An API consist of " +
                                    "a name, connected with multiple versions. Each version is then connected to the " +
                                    "metadata and classifications.",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API updated", content = @Content(schema = @Schema(implementation = ApiDataRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ApiDataRestEntity.Attributes.class))),
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name")
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = ITEM_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = ApiResourceService.class,
                    beanMethod = "deleteApi",
                    operation = @Operation(
                            operationId = "deleteApi",
                            summary = "Delete an API (including all versions, metadata and classifications)",
                            description = "This will delete all attached version, metadata and classifications. Thereby completely remove the API from the catalog.",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "API and all related information deleted successfully"),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name")
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = VERSIONED_PATH,
                    method = RequestMethod.GET,
                    beanClass = ApiResourceService.class,
                    beanMethod = "allApiVersions",
                    operation = @Operation(
                            operationId = "allApiVersions",
                            summary = "Get a overview of all API versions.",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API updated", content = @Content(schema = @Schema(implementation = List.class)))
                            }
                    )),
            @RouterOperation(
                    path = VERSIONED_PATH,
                    method = RequestMethod.POST,
                    beanClass = ApiResourceService.class,
                    beanMethod = "createApiVersion",
                    operation = @Operation(
                            operationId = "createApiVersion",
                            summary = "Create a new API version",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "API Version Created Successfully", content = @Content(schema = @Schema(implementation = ApiVersionDataRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ApiVersionDataRestEntity.Attributes.class))),
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = VERSIONED_ITEM_PATH,
                    method = RequestMethod.GET,
                    beanClass = ApiResourceService.class,
                    beanMethod = "getApiVersion",
                    operation = @Operation(
                            operationId = "getApiVersion",
                            summary = "Get a specific API version",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API Version", content = @Content(schema = @Schema(implementation = ApiVersionDataRestEntity.class)))
                            }
                    )),
            @RouterOperation(
                    path = VERSIONED_ITEM_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = ApiResourceService.class,
                    beanMethod = "deleteApiVersion",
                    operation = @Operation(
                            operationId = "deleteApiVersion",
                            summary = "Delete a specific API version (and connected metadata and classifications)",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Delete Successful. All metadata and classifications associated with the version is also deleted.", content = @Content(schema = @Schema(implementation = ApiVersionDataRestEntity.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = METADATA_PATH,
                    method = RequestMethod.POST,
                    beanClass = ApiMetadataService.class,
                    beanMethod = "createMetadata",
                    operation = @Operation(
                            operationId = "createMetadata",
                            summary = "Create a API version metadata",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "API Version Metadata Created Successfully", content = @Content(schema = @Schema(implementation = MetadataDataRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MetadataDataRestEntity.Attributes.class))),
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = METADATA_PATH,
                    method = RequestMethod.PUT,
                    beanClass = ApiMetadataService.class,
                    beanMethod = "updateMetadata",
                    operation = @Operation(
                            operationId = "updateMetadata",
                            summary = "Updated metadata",
                            tags = {"API"},
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MetadataDataRestEntity.Attributes.class))),
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API Version Metadata Updated Successfully", content = @Content(schema = @Schema(implementation = MetadataDataRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = METADATA_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = ApiMetadataService.class,
                    beanMethod = "deleteMetadata",
                    operation = @Operation(
                            operationId = "deleteMetadata",
                            summary = "Delete API version metadata",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "API Version Metadata Deleted Successfully"),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = METADATA_PATH,
                    method = RequestMethod.GET,
                    beanClass = ApiMetadataService.class,
                    beanMethod = "getMetadata",
                    operation = @Operation(
                            operationId = "getMetadata",
                            summary = "Get API version metadata",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API Version Metadata Content"),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            }
                    )),
            @RouterOperation(
                    path = CLASSIFICATION_PATH,
                    method = RequestMethod.DELETE,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "deleteClassification",
                    operation = @Operation(
                            operationId = "deleteClassification",
                            summary = "Delete all classifications by apiName and apiVersion",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "API Version Classification Deleted Successfully"),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = CLASSIFICATION_PATH,
                    method = RequestMethod.GET,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "getClassification",
                    operation = @Operation(
                            operationId = "getClassification",
                            summary = "Get all classifications by apiName and apiVersion",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API Version Classification Deleted Successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClassificationDataRestEntity.class)))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            }
                    )),
            @RouterOperation(
                    path = CLASSIFICATION_PATH,
                    method = RequestMethod.PUT,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "updateClassification",
                    operation = @Operation(
                            operationId = "updateClassification",
                            summary = "Update classifications by apiName and apiVersion",
                            tags = {"API"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "API Version Classification Deleted Successfully", content = @Content(schema = @Schema(implementation = ClassificationRootRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassificationRootRestEntity.class))),
                            security = { @SecurityRequirement(name = "token") }
                    )),
            @RouterOperation(
                    path = CLASSIFICATION_PATH,
                    method = RequestMethod.POST,
                    beanClass = ClassificationResourceService.class,
                    beanMethod = "createClassification",
                    operation = @Operation(
                            operationId = "createClassification",
                            summary = "Add classifications to a API version",
                            tags = {"API"},
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "apiName", description = "URL encoded API name"),
                                    @Parameter(in = ParameterIn.PATH, name = "apiVersion", description = "URL encoded API version identifier")
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassificationRootRestEntity.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "API Version Classification Created Successfully", content = @Content(schema = @Schema(implementation = ClassificationRootRestEntity.class))),
                                    @ApiResponse(responseCode = "400", description = "Unable to parse incoming values", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized, valid bearer token missing og faulty", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class))),
                                    @ApiResponse(responseCode = "404", description = "No existing API or API Version found", content = @Content(schema = @Schema(implementation = JsonApiRestResponseWrapper.class)))
                            },
                            security = { @SecurityRequirement(name = "token") }
                    )),
    })
    RouterFunction<ServerResponse> apiRoutes(ApiResourceService apiService,
                                             ApiMetadataService metadataService,
                                             ApiClassificationService classificationService) {
        return RouterFunctions
                .route(GET(ROOT_PATH).and(accept(APPLICATION_JSON)), apiService::allApis)
                .andRoute(POST(ROOT_PATH).and(contentType(APPLICATION_JSON)), apiService::createApi)
                .andRoute(GET(ITEM_PATH).and(accept(APPLICATION_JSON)), apiService::getApi)
                .andRoute(PUT(ITEM_PATH).and(contentType(APPLICATION_JSON)), apiService::updateApi)
                .andRoute(DELETE(ITEM_PATH), apiService::deleteApi)
                .andRoute(GET(VERSIONED_PATH).and(accept(APPLICATION_JSON)), apiService::allApiVersions)
                .andRoute(POST(VERSIONED_PATH).and(contentType(APPLICATION_JSON)), apiService::createApiVersion)
                .andRoute(GET(VERSIONED_ITEM_PATH).and(accept(APPLICATION_JSON)), apiService::getApiVersion)
                .andRoute(DELETE(VERSIONED_ITEM_PATH), apiService::deleteApiVersion)
                .andRoute(POST(CLASSIFICATION_PATH).and(contentType(APPLICATION_JSON)), classificationService::createClassification)
                .andRoute(DELETE(CLASSIFICATION_PATH), classificationService::deleteClassification)
                .andRoute(GET(CLASSIFICATION_PATH).and(accept(APPLICATION_JSON)), classificationService::getClassification)
                .andRoute(PUT(CLASSIFICATION_PATH).and(accept(APPLICATION_JSON)), classificationService::updateClassification)
                .andRoute(POST(METADATA_PATH).and(contentType(APPLICATION_JSON)), metadataService::createMetadata)
                .andRoute(DELETE(METADATA_PATH), metadataService::deleteMetadata)
                .andRoute(PUT(METADATA_PATH).and(contentType(APPLICATION_JSON)), metadataService::updateMetadata)
                .andRoute(GET(METADATA_PATH).and(accept(APPLICATION_JSON)), metadataService::getMetadata);
    }
}