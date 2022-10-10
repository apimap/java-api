package io.apimap.api.integration.dbconfig;

import io.apimap.api.configuration.NitriteConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Nitrite configuration for tests - simply returns an empty filename to set up an in-memory NitriteDB instance
 */
@TestConfiguration
public class NitriteTestConfig {
    @Bean
    @Primary
    public NitriteConfiguration getConfig() {
        return new NitriteConfiguration();
    }
}
