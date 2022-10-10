package io.apimap.api.integration;

import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static io.apimap.api.integration.IntegrationTestHelper.RESPONSE_TYPE_API;
import static io.apimap.api.integration.IntegrationTestHelper.RESPONSE_TYPE_API_LIST;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class with common test cases that should work both when using NitriteDB and MongoDB
 */
public abstract class SmokeTestAllBase {


    private final TestDataHelper testData = new TestDataHelper();

    @Autowired
    private WebTestClient webClient;

    @Autowired
    IntegrationTestHelper helper;

    @AfterEach
    public void clearDatabases() {
        helper.clearDatabases();
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

        var createResult = helper.postJsonPublic(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(testApi), "/api");
        helper.currentApiToken = createResult.getData().getMeta().getToken();
        var updateResult = helper.putJsonAuthed(RESPONSE_TYPE_API, new JsonApiRestRequestWrapper<>(apiUpdate), "/api/{name}", testApi.getName());
        var retrieveResult = helper.getJsonPublic(RESPONSE_TYPE_API, "/api/{name}", testApi.getName());
        helper.deleteAuthedAndVerifyGone("/api/{name}", testApi.getName());

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
        helper.storeApi(testData.createApiData());
        helper.storeVersionForCurrentApi(testData.createApiVersion());
        helper.storeMetadataForCurrentVersion(testData.createMetadata());

        var getResult = helper.getJsonPublic(RESPONSE_TYPE_API_LIST, "/api");

        assertThat(getResult.getData().getData())
                .hasSize(1);
    }
}