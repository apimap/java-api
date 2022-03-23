package io.apimap.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;

/**
 * Copied from https://stackoverflow.com/questions/60740572/how-to-get-body-as-string-from-serverresponse-for-test
 */
public class ServerResponseExtractor {

    public static <T> T serverResponseAsObject(ServerResponse serverResponse,
                                               ObjectMapper mapper, Class<T> type) {
        String response = serverResponseAsString(serverResponse);
        try {
            return mapper.readValue(response, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serverResponseAsString(ServerResponse serverResponse) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/foo/foo"));

        DebugServerContext debugServerContext = new DebugServerContext();
        serverResponse.writeTo(exchange, debugServerContext).block();

        MockServerHttpResponse response = exchange.getResponse();
        return response.getBodyAsString().block();

    }

    private static class DebugServerContext implements ServerResponse.Context {
        @NotNull
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return HandlerStrategies.withDefaults().messageWriters();
        }

        @NotNull
        @Override
        public List<ViewResolver> viewResolvers() {
            return Collections.emptyList();
        }
    }
}