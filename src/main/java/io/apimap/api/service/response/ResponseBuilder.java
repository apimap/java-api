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
import io.apimap.api.configuration.NodeConfiguration;
import io.apimap.api.rest.jsonapi.JsonApiError;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.rest.jsonapi.JsonApiViews;
import io.apimap.api.utils.URIUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class ResponseBuilder {

    protected long responseMetricsStartTime;

    protected URI resourceURI;

    protected JsonApiRestResponseWrapper body;

    protected ApimapConfiguration apimapConfiguration;

    protected ArrayList<HashMap> relatedReferences = new ArrayList<>();

    public ResponseBuilder(long startTime, ApimapConfiguration apimapConfiguration) {
        this.responseMetricsStartTime = startTime;
        this.apimapConfiguration = apimapConfiguration;
    }

    public static ResponseBuilder builder(long startTime, ApimapConfiguration apimapConfiguration) {
        return new ResponseBuilder(startTime, apimapConfiguration);
    }

    public ResponseBuilder withBody(JsonApiRestResponseWrapper<?> content) {
        this.body = content;
        return this;
    }

    public ResponseBuilder withoutBody() {
        this.body = new JsonApiRestResponseWrapper<Object>(new HashMap<>());
        return this;
    }

    public Mono<ServerResponse> text(String body, MediaType contentType){
        return ServerResponse.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(contentType)
                .body(Mono.just(body), String.class);
    }
    public Mono<ServerResponse> badRequest() {
        JsonApiRestResponseWrapper wrapper = new JsonApiRestResponseWrapper();
        wrapper.addErorr(new JsonApiError(
                HttpStatus.BAD_REQUEST.toString(),
                "There is something wrong with the content of the request"
        ));

        return ServerResponse.badRequest()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> methodNotAllowed() {
        JsonApiRestResponseWrapper wrapper = new JsonApiRestResponseWrapper();
        wrapper.addErorr(new JsonApiError(
                HttpStatus.METHOD_NOT_ALLOWED.toString(),
                "Missing token"
        ));

        return ServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> conflict() {
        JsonApiRestResponseWrapper wrapper = new JsonApiRestResponseWrapper();
        wrapper.addErorr(new JsonApiError(
                HttpStatus.CONFLICT.toString(),
                "The resource exists already"
        ));

        return ServerResponse.status(HttpStatus.CONFLICT)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> noContent() {

        return ServerResponse.noContent()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .build();
    }

    public Mono<ServerResponse> notFound() {
        JsonApiRestResponseWrapper wrapper = new JsonApiRestResponseWrapper();
        wrapper.addErorr(new JsonApiError(
                HttpStatus.NOT_FOUND.toString(),
                "The requested resource or collection could not be found"
        ));

        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> notImplemented() {
        JsonApiRestResponseWrapper wrapper = new JsonApiRestResponseWrapper();
        wrapper.addErorr(new JsonApiError(
                HttpStatus.NOT_IMPLEMENTED.toString(),
                "This method has not been implemeted"
        ));

        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(wrapper), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> okCollection() {
        JsonApiRestResponseWrapper body = bodyWithMetadata(this.body);
        body.setSelf(resourceURI);
        this.relatedReferences.forEach(rel -> body.addRelatedRef((String) rel.get("rel"), (URI) rel.get("href")));
        body.appendDuration(responseMetricsStartTime, System.currentTimeMillis());

        return ServerResponse.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .hint(Jackson2CodecSupport.JSON_VIEW_HINT, JsonApiViews.Collection.class)
                .body(Mono.just(body), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> okResource() {
        JsonApiRestResponseWrapper body = bodyWithMetadata(this.body);
        body.setSelf(resourceURI);
        this.relatedReferences.forEach(rel -> body.addRelatedRef((String) rel.get("rel"), (URI) rel.get("href")));
        body.appendDuration(responseMetricsStartTime, System.currentTimeMillis());

        return ServerResponse.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .hint(Jackson2CodecSupport.JSON_VIEW_HINT, JsonApiViews.Default.class)
                .body(Mono.just(body), JsonApiRestResponseWrapper.class);
    }

    public Mono<ServerResponse> created(Boolean includeToken) {
        JsonApiRestResponseWrapper body = bodyWithMetadata(this.body);

        if (body != null) {
            body.setSelf(resourceURI);
            this.relatedReferences.forEach(rel -> body.addRelatedRef((String) rel.get("rel"), (URI) rel.get("href")));
            body.appendDuration(responseMetricsStartTime, System.currentTimeMillis());
        }

        Class hintClass = (Boolean.TRUE.equals(includeToken)) ? JsonApiViews.Extended.class : JsonApiViews.Default.class;

        return ServerResponse.created(resourceURI)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Request-Method", "GET,POST,DELETE")
                .contentType(MediaType.APPLICATION_JSON)
                .hint(Jackson2CodecSupport.JSON_VIEW_HINT, hintClass)
                .body(Mono.justOrEmpty(body), JsonApiRestResponseWrapper.class);
    }

    public ResponseBuilder withStartTime(long time) {
        this.responseMetricsStartTime = time;
        return this;
    }

    public ResponseBuilder withEmptyBody() {
        this.body = new JsonApiRestResponseWrapper<>();
        return this;
    }

    public ResponseBuilder withResourceURI(URI uri) {
        this.resourceURI = uri;
        return this;
    }

    public ResponseBuilder addRelatedRef(String rel, java.net.URI uri) {
        HashMap<String, Object> item = new HashMap<String, Object>();
        item.put("href", uri);
        item.put("rel", rel);
        relatedReferences.add(item);
        return this;
    }

    protected JsonApiRestResponseWrapper bodyWithMetadata(JsonApiRestResponseWrapper<?> body) {
        if (body == null) {
            return body;
        }

        apimapConfiguration.getMetadata().forEach((key, value) -> body.addMetadata(key.toLowerCase(), value));

        if (apimapConfiguration.enabledOpenapi()) {
            String openApiUrl = URIUtil.rootLevelFromURI(resourceURI).append("documentation").append("openapi3").stringValue();
            body.addMetadata("openapi", openApiUrl);
        }

        if (apimapConfiguration.enabledHostIdentifier()) {
            body.addMetadata("host identifier", NodeConfiguration.ID);
        }

        return body;
    }
}
