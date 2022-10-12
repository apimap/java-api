package io.apimap.api.integration;

import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ClassificationDataRestEntity;
import io.apimap.api.rest.ClassificationRootRestEntity;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static io.apimap.api.integration.IntegrationTestHelper.*;
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
    public void testApiCrud() {
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

    /** test API version lifecycle with POST, GET and DELETE */
    @Test
    public void testApiVersionCrd() {
        var testApiVersion = testData.createApiVersion();

        var api = helper.storeApi(testData.createApiData());

        var createResult = helper.postJsonAuthed(RESPONSE_TYPE_VERSION, new JsonApiRestRequestWrapper<>(testApiVersion), "/api/{name}/version", api.getName());
        var retrieveResult = helper.getJsonPublic(RESPONSE_TYPE_VERSION, "/api/{name}/version/{version}", api.getName(), testApiVersion.getVersion());
        helper.deleteAuthedAndVerifyGone("/api/{name}/version/{version}", api.getName(), testApiVersion.getVersion());

        assertThat(createResult.getData()).as("create result")
                .usingRecursiveComparison()
                .ignoringFields("created", "rating", "id")
                .isEqualTo(testApiVersion);

        assertThat(retrieveResult.getData()).as("get result")
                .usingRecursiveComparison()
                .isEqualTo(createResult.getData());
    }

    @Test
    public void testGetAllApiVersions() {
        var api = helper.storeApi(testData.createApiData());
        helper.storeVersionForCurrentApi(testData.createApiVersion());

        var getResult = helper.getJsonPublic(RESPONSE_TYPE_VERSION_LIST, "/api/{name}/version", api.getName());

        assertThat(getResult.getData().getVersions())
                .hasSize(1);
    }

    @Test
    public void testClassificationCrud() {
        var testClassifications = new ClassificationRootRestEntity(new ArrayList<>(List.of(
                testData.createClassification("taxonomy://what"),
                testData.createClassification("taxonomy://who")
        )));
        var classificationsUpdate = new ClassificationRootRestEntity(new ArrayList<>(List.of(
                testData.createClassification("taxonomy://what"),
                testData.createClassification("taxonomy://where")
        )));

        var api = helper.storeApi(testData.createApiData());
        var version = helper.storeVersionForCurrentApi(testData.createApiVersion());

        var createResult = helper.postJsonAuthed(RESPONSE_TYPE_CLASSIFICATION_LIST, new JsonApiRestRequestWrapper<>(testClassifications), "/api/{name}/version/{version}/classification", api.getName(), version.getVersion());
        var updateResult = helper.putJsonAuthed(RESPONSE_TYPE_CLASSIFICATION_LIST, new JsonApiRestRequestWrapper<>(classificationsUpdate), "/api/{name}/version/{version}/classification", api.getName(), version.getVersion());
        var retrieveResult = helper.getJsonPublic(RESPONSE_TYPE_CLASSIFICATION_LIST, "/api/{name}/version/{version}/classification", api.getName(), version.getVersion());
        helper.deleteAuthed("/api/{name}/version/{version}/classification", api.getName(), version.getVersion());
        // GET after DELETE returns an empty list of classifications
        var retrieveAfterDelete = helper.getJsonPublic(RESPONSE_TYPE_CLASSIFICATION_LIST, "/api/{name}/version/{version}/classification", api.getName(), version.getVersion());

        assertThat(createResult.getData()).as("create result")
                .usingRecursiveComparison()
                .isEqualTo(testClassifications);

        assertThat(updateResult.getData()).as("update result")
                .usingRecursiveComparison()
                .isEqualTo(classificationsUpdate);

        assertThat(retrieveResult.getData()).as("get result")
                .usingRecursiveComparison()
                .isEqualTo(classificationsUpdate);

        assertThat(retrieveAfterDelete.getData().getData()).as("get after delete")
                .isEmpty();
    }

    @Test
    public void testMetadataCrud() {
        var testMetadata = testData.createMetadata();
        var metadataUpdate = new MetadataDataRestEntity(
                testMetadata.getName(),
                "new description",
                "new visibility",
                testMetadata.getApiVersion(),
                "new release status",
                "new interface",
                "new description language",
                "new layer",
                "new unit",
                "new system identifier",
                List.of("https://example.org/new_documentation")
        );

        var api = helper.storeApi(testData.createApiData());
        var version = helper.storeVersionForCurrentApi(testData.createApiVersion());

        var createResult = helper.postJsonAuthed(RESPONSE_TYPE_METADATA, new JsonApiRestRequestWrapper<>(testMetadata), "/api/{name}/version/{version}/metadata", api.getName(), version.getVersion());
        var updateResult = helper.putJsonAuthed(RESPONSE_TYPE_METADATA, new JsonApiRestRequestWrapper<>(metadataUpdate), "/api/{name}/version/{version}/metadata", api.getName(), version.getVersion());
        var retrieveResult = helper.getJsonPublic(RESPONSE_TYPE_METADATA, "/api/{name}/version/{version}/metadata", api.getName(), version.getVersion());
        helper.deleteAuthedAndVerifyGone("/api/{name}/version/{version}/metadata", api.getName(), version.getVersion());

        assertThat(createResult.getData()).as("create result")
                .usingRecursiveComparison()
                .ignoringFields("links", "uri")
                .isEqualTo(testMetadata);

        assertThat(updateResult.getData()).as("update result")
                .usingRecursiveComparison()
                .ignoringFields("links", "uri")
                .isEqualTo(metadataUpdate);

        assertThat(retrieveResult.getData()).as("get result")
                .usingRecursiveComparison()
                .ignoringFields("links", "uri")
                .isEqualTo(metadataUpdate);
    }
}