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

package io.apimap.api.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.HashMap;

@ConfigurationProperties(prefix = "apimap")
public class ApimapConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApimapConfiguration.class);

    protected final HashMap<String, String> metadata;
    protected final Enabled hostIdentifier;
    protected final Enabled openapi;
    protected final String version;
    protected final Limits limits;

    @ConstructorBinding
    @SuppressFBWarnings
    public ApimapConfiguration(final HashMap<String, String> metadata,
                               final Enabled hostIdentifier,
                               final Enabled openapi,
                               final String version,
                               final Limits limits) {
        LOGGER.info("Apimap.io application version {}", version);

        this.metadata = metadata;
        this.hostIdentifier = hostIdentifier;
        this.openapi = openapi;
        this.version = version;
        this.limits = limits;
    }

    public String getVersion() {
        return version;
    }

    public boolean enabledOpenapi() {
        if (openapi == null) return false;
        return openapi.isEnabled();
    }

    public boolean enabledHostIdentifier() {
        if (hostIdentifier == null) return false;
        return hostIdentifier.isEnabled();
    }

    public HashMap<String, String> getMetadata() {
        return (HashMap<String, String>) metadata.clone();
    }

    public Limits getLimits() {
        return limits;
    }

    public static class Enabled {
        protected final boolean enabled;

        public Enabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class Limits {
        // Maximum byte size of README and Changelog body
        protected final long maximumMetadataDocumentSize;

        public Limits(final long maximumMetadataDocumentSize) {
            this.maximumMetadataDocumentSize = maximumMetadataDocumentSize;
        }

        public long getMaximumMetadataDocumentSize() {
            return maximumMetadataDocumentSize;
        }
    }
}
