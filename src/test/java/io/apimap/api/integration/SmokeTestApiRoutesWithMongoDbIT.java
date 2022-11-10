package io.apimap.api.integration;

import io.apimap.api.integration.dbconfig.MongoDbTestConfig;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Run API smoke tests with an embedded MongoDB database
 */
@SpringBootTest(properties = {"nitrite.enabled=false", "mongodb.enabled=true"})
@AutoConfigureWebTestClient
@Import({MongoDbTestConfig.class, EmbeddedMongoAutoConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmokeTestApiRoutesWithMongoDbIT extends SmokeTestApiRoutesBase {

    // Runs tests from the SmokeTestApiRoutesBase base class

}