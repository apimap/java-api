package io.apimap.api.integration.dbconfig;

import com.mongodb.reactivestreams.client.MongoClient;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import io.apimap.api.configuration.MongoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.io.IOException;

/**
 * MongoDB configuration for tests - provides config for the EmbeddedMongoAutoConfiguration
 * to start an in-memory MongoDB server for the test, and overrides the beans from our MongoConfiguration class.
 */
@TestConfiguration
public class MongoDbTestConfig {

    // Keep our own copy of MongoConfiguration so we can set the connection URI based on the test mongodb instance
    private final MongoConfiguration substituteConfig = new MongoConfiguration();

    /**
     * Configuration for the MongoDB embedded server
     *
     * @see org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
     */
    @Bean
    public MongodConfig mongodConfig() {
        try {
            var netConfig = new Net();
            var database = "apimap";
            var uri = String.format("mongodb://%s:%d/%s", netConfig.getServerAddress().getHostAddress(), netConfig.getPort(), database);

            substituteConfig.setUri(uri);
            substituteConfig.setDatabaseName(database);

            return MongodConfig.builder()
                    .version(Version.V3_6_22)
                    .net(netConfig)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Override MongoConfiguration.reactiveMongoClient */
    @Bean
    @Primary
    @DependsOn("mongodConfig")
    public MongoClient reactiveMongoClientForTest() {
        return substituteConfig.reactiveMongoClient();
    }

    /** Override MongoConfiguration.reactiveMongoTemplate */
    @Bean
    @Primary
    @DependsOn("mongodConfig")
    public ReactiveMongoTemplate reactiveMongoTemplateForTest() {
        return substituteConfig.reactiveMongoTemplate();
    }
}
