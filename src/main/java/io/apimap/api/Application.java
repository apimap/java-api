/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package io.apimap.api;

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.configuration.MongoConfiguration;
import io.apimap.api.configuration.NitriteConfiguration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@OpenAPIDefinition(
        info = @Info(
                title = "Apimap.io",
                description = "Apimap.io is a centralized registry of our APIs. This API is built to comply with the JSON:API standard version 1.1 (https://jsonapi.org/)",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        tags = {
                @Tag(name = "API", description = "Endpoints managing API related resources"),
                @Tag(name = "CLASSIFICATION", description = "Endpoints managing CLASSIFICATION related resources"),
                @Tag(name = "STATISTICS", description = "Endpoints managing STATISTICS related resources"),
                @Tag(name = "TAXONOMY", description = "Endpoints managing TAXONOMY related resources"),
                @Tag(name = "START", description = "The HATEOAS start point for exploring the API")
        }
)
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoReactiveAutoConfiguration.class,
        EmbeddedMongoAutoConfiguration.class,
})
@EnableConfigurationProperties({ApimapConfiguration.class, NitriteConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}