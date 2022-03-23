package io.apimap.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class AccessConfiguration {
    public static final String TOKEN = UUID.randomUUID().toString();
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessConfiguration.class);

    public AccessConfiguration() {
        LOGGER.info("Node access token {}", TOKEN);
    }

    public String getToken(){
        return TOKEN;
    }
}
