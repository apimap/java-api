package io.apimap.api.integration;

import io.apimap.api.integration.dbconfig.NitriteTestConfig;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Run API smoke tests with the Nitrite database implementations
 */
@SpringBootTest
@AutoConfigureWebTestClient
@Import(NitriteTestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmokeTestApiRoutesWithNitriteIT extends SmokeTestApiRoutesBase {

    // Runs tests from the SmokeTestApiRoutesBase base class

}