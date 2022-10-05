package io.apimap.api.integration;

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
    @AfterEach
    public void clearDatabases() {
        var apis = (List<IApi>) apiRepository.all().collectList().block();
        for (var api: apis) {
            var versions = (List<IApiVersion>)apiRepository.allApiVersions(api.getId()).collectList().block();
            for (var version: versions) {
                apiRepository.deleteApiVersion(version.getApiId(), version.getVersion()).block();
            }
            apiRepository.delete(api.getName()).block();
            System.err.println("deleted " + api);
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

    @Test
    public void postAndRetrieveAPI() throws Exception {
        var testApi = testData.createApiData();

        var postResult = webClient.post().uri("/api")
                .bodyValue(new JsonApiRestRequestWrapper<>(testApi))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_API)
                .returnResult()
                .getResponseBody();

        var createdApi = postResult.getData();
        assertThat(createdApi)
                .usingRecursiveComparison()
                .ignoringFields("meta", "relationships")
                .isEqualTo(testApi);

        assertThat(createdApi.getMeta().getToken()).as("token")
                .isNotEmpty();

        var getResult = webClient.get().uri("/api/{name}", testApi.getName())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_API)
                .returnResult()
                .getResponseBody();

        assertThat(getResult.getData())
                .usingRecursiveComparison()
                .ignoringFields("relationships")
                .isEqualTo(testApi);
    }

    @Test
    public void testGetAllApis() {
        // APIs need a version with metadata to be listed
        storeApi(testData.createApiData());
        storeVersionForCurrentApi(testData.createApiVersion());
        storeMetadataForCurrentVersion(testData.createMetadata());

        var getResult = webClient.get().uri("/api")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_API_LIST)
                .returnResult()
                .getResponseBody();

        assertThat(getResult.getData().getData())
                .hasSize(1);
    }


    /** Helper method to store an API and keep the token and API data for further update */
    private ApiDataRestEntity storeApi(ApiDataRestEntity testApi) {
        var postResult = webClient.post().uri("/api")
                .bodyValue(new JsonApiRestRequestWrapper<>(testApi))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_API)
                .returnResult()
                .getResponseBody();

        currentApi = postResult.getData();
        currentApiToken = currentApi.getMeta().getToken();
        return currentApi;
    }

    /** Helper method to add an API version to the last added API */
    private ApiVersionDataRestEntity storeVersionForCurrentApi(ApiVersionDataRestEntity testVersion) {
        var postResult = webClient.post().uri("/api/{name}/version", currentApi.getName())
                .bodyValue(new JsonApiRestRequestWrapper<>(testVersion))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_VERSION)
                .returnResult()
                .getResponseBody();

        currentVersion = postResult.getData();
        return currentVersion;
    }

    /** Helper method to add metadata */
    private MetadataDataRestEntity storeMetadataForCurrentVersion(MetadataDataRestEntity testMetadata) {
        var postResult = webClient.post().uri("/api/{name}/version/{version}/metadata", currentApi.getName(), currentVersion.getVersion())
                .bodyValue(new JsonApiRestRequestWrapper<>(testMetadata))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + currentApiToken)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RESPONSE_TYPE_METADATA)
                .returnResult()
                .getResponseBody();

        return postResult.getData();
    }
}