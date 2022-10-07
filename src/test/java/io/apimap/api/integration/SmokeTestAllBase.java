package io.apimap.api.integration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IApiVersion;
import io.apimap.api.repository.repository.*;
import io.apimap.api.rest.*;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class with common test cases that should work both when using NitriteDB and MongoDB
 */
public abstract class SmokeTestAllBase {

    private static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiDataRestEntity>> RESPONSE_TYPE_API = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiCollectionRootRestEntity>> RESPONSE_TYPE_API_LIST = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<JsonApiRestResponseWrapper<ApiVersionDataRestEntity>> RESPONSE_TYPE_VERSION = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<JsonApiRestResponseWrapper<MetadataDataRestEntity>> RESPONSE_TYPE_METADATA = new ParameterizedTypeReference<>() {};

    private final TestDataHelper testData = new TestDataHelper();

    private String currentApiToken;
    private ApiDataRestEntity currentApi;
    private ApiVersionDataRestEntity currentVersion;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private IApiRepository apiRepository;
    @Autowired
    private IClassificationRepository classificationRepository;
    @Autowired
    private IMetadataRepository metadataRepository;
    @Autowired
    private ITaxonomyRepository taxonomyRepository;
    @Autowired
    private IVoteRepository voteRepository;

    @SuppressWarnings({"ConstantConditions"})
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @AfterEach
    public void clearDatabases() {
        var apis = (List<IApi>) apiRepository.all().collectList().block();
        for (var api: apis) {
            var versions = (List<IApiVersion>)apiRepository.allApiVersions(api.getId()).collectList().block();
            for (var version: versions) {
                apiRepository.deleteApiVersion(version.getApiId(), version.getVersion()).block();
            }
            apiRepository.delete(api.getName()).block();
        }
    }

    @Test
    public void rootShouldReturnOk() throws Exception {
        webClient.get().uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    /** test API lifecycle with POST, PUT, GET and DELETE */
    @Test
    public void testApiCRUD() throws Exception {
        var testApi = testData.createApiData();
        var apiUpdate = new ApiDataRestEntity(testApi.getName(), "http://example.org/new_repo");

        var createResult = postJsonPublic(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(testApi), "/api");
        currentApiToken = createResult.getData().getMeta().getToken();
        var updateResult = putJsonAuthed(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(apiUpdate), "/api/{name}", testApi.getName());
        var retrieveResult = getJsonPublic(RESPONSE_TYPE_API, "/api/{name}", testApi.getName());
        deleteAuthedAndVerifyGone("/api/{name}", testApi.getName());

        assertThat(createResult.getData()).as("create result")
                .usingRecursiveComparison()
                .ignoringFields("meta", "relationships")
                .isEqualTo(testApi);

        assertThat(createResult.getData().getMeta().getToken()).as("token")
                .isNotEmpty();

        assertThat(updateResult.getData()).as("update result")
                .usingRecursiveComparison()
                .comparingOnlyFields("codeRepository")
                .isEqualTo(apiUpdate);

        assertThat(retrieveResult.getData()).as("get result")
                .usingRecursiveComparison()
                .ignoringFields("relationships", "codeRepository")
                .isEqualTo(testApi);

        assertThat(retrieveResult.getData()).as("get result")
                .usingRecursiveComparison()
                .comparingOnlyFields("codeRepository")
                .isEqualTo(apiUpdate);
    }

    @Test
    public void testGetAllApis() {
        // APIs need a version with metadata to be listed
        storeApi(testData.createApiData());
        storeVersionForCurrentApi(testData.createApiVersion());
        storeMetadataForCurrentVersion(testData.createMetadata());

        var getResult = getJsonPublic(RESPONSE_TYPE_API_LIST, "/api");

        assertThat(getResult.getData().getData())
                .hasSize(1);
    }


    /** Helper method to store an API and keep the token and API data for further update */
    private ApiDataRestEntity storeApi(ApiDataRestEntity testApi) {
        var postResult = postJsonPublic(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(testApi), "/api");
        currentApi = postResult.getData();
        currentApiToken = currentApi.getMeta().getToken();
        return currentApi;
    }

    /** Helper method to add an API version to the last added API */
    private ApiVersionDataRestEntity storeVersionForCurrentApi(ApiVersionDataRestEntity testVersion) {
        var postResult = postJsonAuthed(RESPONSE_TYPE_VERSION, new JsonApiRestRequestWrapper<>(testVersion), "/api/{name}/version", currentApi.getName());
        currentVersion = postResult.getData();
        return currentVersion;
    }

    /** Helper method to add metadata */
    private MetadataDataRestEntity storeMetadataForCurrentVersion(MetadataDataRestEntity testMetadata) {
        return postJsonAuthed(
                RESPONSE_TYPE_METADATA,
                new JsonApiRestRequestWrapper<>(testMetadata),
                "/api/{name}/version/{version}/metadata", currentApi.getName(), currentVersion.getVersion()
        ).getData();
    }

    /** Helper for sending a GET request with no auth and receiving JSON in response */
    private <T> T getJsonPublic(ParameterizedTypeReference<T> responseType, String uri, Object... uriVariables) {
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
    private <T> T postJsonPublic(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
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
    private <T> T postJsonAuthed(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
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
    private <T> T putJsonAuthed(ParameterizedTypeReference<T> responseType, Object requestBody, String uri, Object... uriVariables) {
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
    private void deleteAuthedAndVerifyGone(String uri, Object... uriVariables) {
        webClient.delete().uri(uri, uriVariables)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus().isNoContent();

        // Verify that deleted resource returns 404 now
        webClient.get().uri(uri, uriVariables)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus().isNotFound();
    }
}