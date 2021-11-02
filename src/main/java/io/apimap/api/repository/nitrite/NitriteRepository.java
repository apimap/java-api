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

package io.apimap.api.repository.nitrite;

import io.apimap.api.configuration.NitriteConfiguration;
import org.dizitart.no2.Nitrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NitriteRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitriteRepository.class);

    protected final NitriteConfiguration nitriteConfiguration;

    protected Nitrite database;

    public NitriteRepository(NitriteConfiguration nitriteConfiguration, String identifier) {
        this.nitriteConfiguration = nitriteConfiguration;

        String dbFileName = null;

        if (nitriteConfiguration.hasFilesystem()
                && this.nitriteConfiguration.getFilePath() != null
                && !this.nitriteConfiguration.getFilePath().isEmpty()) {
            dbFileName = this.nitriteConfiguration.getFilePath() + "/" + identifier.toLowerCase() + ".nitrite";
        }

        if (dbFileName != null) {
            this.database = Nitrite.builder()
                    .filePath(dbFileName)
                    .disableAutoCompact()
                    .openOrCreate();
        } else {
            this.database = Nitrite.builder()
                    .disableAutoCompact()
                    .openOrCreate();
        }

        LOGGER.info("Created db @ {}", dbFileName);
    }

    public Nitrite getDatabase() {
        return database;
    }
}
