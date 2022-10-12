package io.apimap.api.integration;

import io.apimap.api.rest.*;

import java.util.List;

/** Helper to generate dummy test data */
public class TestDataHelper {
    public ApiDataRestEntity createApiData() {
        return new ApiDataRestEntity(
                "test-api",
                "https://example.org/code-repository"
        );
    }

    public ApiVersionDataRestEntity createApiVersion() {
        return new ApiVersionDataRestEntity("v1.1");
    }

    public MetadataDataRestEntity createMetadata() {
        return new MetadataDataRestEntity(
                "test-api",
                "A test API for doing tests.",
                "EXTREMELY VISIBLE",
                "v1.1",
                "eternal beta",
                "JSON:API v1.1",
                "napkin scribbles",
                "middle-end",
                "postal department",
                "S19999",
                List.of("https://example.org/README.txt"),
                "https://example.org/api"
        );
    }

    public ClassificationDataRestEntity createClassification(String urn) {
        return new ClassificationDataRestEntity(
                urn,
                "1"
        );
    }

    public String createMarkdownDoc() {
        return "Title\n=====\nLorem ipsum *dolor* sit amet.";
    }
}
