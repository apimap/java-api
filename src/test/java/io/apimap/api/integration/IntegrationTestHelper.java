package io.apimap.api.integration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IApiVersion;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.rest.*;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

/**
 * Helper class for making HTTP calls and setting up and clearing the database
 */
@Component
class IntegrationTestHelper {
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiDataRestEntity>> RESPONSE_TYPE_API = new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>> RESPONSE_TYPE_API_LIST = new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiVersionDataRestEntity>> RESPONSE_TYPE_VERSION = new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiVersionCollectionRootRestEntity>> RESPONSE_TYPE_VERSION_LIST = new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ClassificationRootRestEntity>> RESPONSE_TYPE_CLASSIFICATION_LIST = new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<JsonApiRestResponseWrapper<MetadataDataRestEntity>> RESPONSE_TYPE_METADATA = new ParameterizedTypeReference<>() {};

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private IApiRepository apiRepository;
    @Autowired
    private IMetadataRepository metadataRepository;
    @Autowired
    private IClassificationRepository classificationRepository;

    String currentApiToken;
    ApiDataRestEntity currentApi;
    ApiVersionDataRestEntity currentVersion;

    @SuppressWarnings({"ConstantConditions"})
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void clearDatabases() {
        var apis = (List<IApi>) apiRepository.all().collectList().block();
        for (var api: apis) {
            var versions = (List<IApiVersion>)apiRepository.allApiVersions(api.getId()).collectList().block();
            for (var version: versions) {
                try {
                    classificationRepository.delete(api.getId(), currentVersion.getVersion()).block();
                } catch (Exception e) {
                    System.err.println("Could not delete test classification: " + e);
                }
                try {
                    metadataRepository.delete(api.getId(), currentVersion.getVersion()).block();
                } catch (Exception e) {
                    System.err.println("Could not delete test metadata: " + e);
                }
                try {
                    apiRepository.deleteApiVersion(version.getApiId(), version.getVersion()).block();
                } catch (Exception e) {
                    System.err.println("Could not delete test API version: " + e);
                }
            }
            apiRepository.delete(api.getName()).block();
        }
    }

    /** Helper method to store an API and keep the token and API data for further update */
    public ApiDataRestEntity storeApi(ApiDataRestEntity testApi) {
        var postResult = postJsonPublic(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(testApi), "/api");
        currentApi = postResult.getData();
        currentApiToken = currentApi.getMeta().getToken();
        return currentApi;
    }

    /** Helper method to add an API version to the last added API */
    public ApiVersionDataRestEntity storeVersionForCurrentApi(ApiVersionDataRestEntity testVersion) {
        var postResult = postJsonAuthed(RESPONSE_TYPE_VERSION, new JsonApiRestRequestWrapper<>(testVersion), "/api/{name}/version", currentApi.getName());
        currentVersion = postResult.getData();
        return currentVersion;
    }

    /** Helper method to add metadata */
    public MetadataDataRestEntity storeMetadataForCurrentVersion(MetadataDataRestEntity testMetadata) {
        return postJsonAuthed(
                RESPONSE_TYPE_METADATA,
                new JsonApiRestRequestWrapper<>(testMetadata),
                "/api/{name}/version/{version}/metadata", currentApi.getName(), currentVersion.getVersion()
        ).getData();
    }

    /** Helper for sending a GET request with no auth and receiving JSON in response */
    public <T> T getJsonPublic(ParameterizedTypeReference<T> responseType, String uri, Object... uriVariables) {
        return webClient.get().uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(responseType)
                .returnResult()
                .getResponseBody();
    }

    /** Helper for sending a POST request with no auth and receiving JSON in response */
    public <T> T postJsonPublic(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
        return webClient.post().uri(uri, uriVariables)
                .bodyValue(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(responseType)
                .returnResult()
                .getResponseBody();
    }

    /** Helper for sending a POST request with auth for the last added API and receiving JSON in response */
    public <T> T postJsonAuthed(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
        return webClient.post().uri(uri, uriVariables)
                .bodyValue(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(responseType)
                .returnResult()
                .getResponseBody();
    }

    /** Helper for sending a PUT request with auth for the last added API and receiving JSON in response */
    public <T> T putJsonAuthed(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
        return webClient.put().uri(uri, uriVariables)
                .bodyValue(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(responseType)
                .returnResult()
                .getResponseBody();
    }

    /** Helper for sending a DELETE request with auth for the last added API */
    public void deleteAuthed(String uri, Object... uriVariables) {
        webClient.delete().uri(uri, uriVariables)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    /** Helper for sending a DELETE request with auth for the last added API
     * Also verifies that the resource returns 404 after deletion */
    public void deleteAuthedAndVerifyGone(String uri, Object... uriVariables) {
        deleteAuthed(uri, uriVariables);

        // Verify that deleted resource returns 404 now
        webClient.get().uri(uri, uriVariables)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus().isNotFound();
    }
}
