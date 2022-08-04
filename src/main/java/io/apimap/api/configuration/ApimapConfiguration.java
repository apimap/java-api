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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "apimap")
public class ApimapConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApimapConfiguration.class);
    protected HashMap<String, String> metadata = new HashMap<>();
    protected Enabled hostIdentifier;
    protected Enabled openapi;
    protected String version;
    protected Limits limits;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        LOGGER.info("Apimap.io application version {}", version);
        this.version = version;
    }

    public void setOpenapi(Enabled openapi) {
        this.openapi = openapi;
    }

    public void setHostIdentifier(Enabled hostIdentifier) {
        this.hostIdentifier = hostIdentifier;
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
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    private static class Enabled {
        boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Limits {
        // Maximum byte size of README and Changelog body
        protected long maximumMetadataDocumentSize;

        public Limits() {
        }

        public Limits(long maximumMetadataDocumentSize) {
            this.maximumMetadataDocumentSize = maximumMetadataDocumentSize;
        }

        public long getMaximumMetadataDocumentSize() {
            return maximumMetadataDocumentSize;
        }

        public void setMaximumMetadataDocumentSize(long maximumMetadataDocumentSize) {
            this.maximumMetadataDocumentSize = maximumMetadataDocumentSize;
        }
    }
}
