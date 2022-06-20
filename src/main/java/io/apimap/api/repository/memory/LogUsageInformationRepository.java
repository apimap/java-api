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

package io.apimap.api.repository.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.stereotype.Repository;

@Repository
public class LogUsageInformationRepository extends InMemoryHttpTraceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUsageInformationRepository.class);

    @Override
    public void add(HttpTrace trace) {
        LOGGER.info(getInformationFromHttpTrace(trace));
        super.add(trace);
    }

    private String getInformationFromHttpTrace(HttpTrace trace) {
        //Principal and session seems to be always null
        //Not fetching timestamp since we are adding our one timestamp in the log
        return getRequest(trace.getRequest()) +
                ", " + getResponse(trace.getResponse()) +
                ", Timetaken=" + trace.getTimeTaken() + " milliseconds";
    }

    private String getResponse(HttpTrace.Response response) {
        // Headers MUST NOT be captured
        if (response != null) {
            return "Status=" + response.getStatus();
        } else {
            return null;
        }
    }

    private String getRequest(HttpTrace.Request request) {
        // Remote address seems to be always null
        // Headers MUST NOT be captured
        if (request != null) {
            return "Method=" + request.getMethod() +
                    ", URI=" + request.getUri();
        } else {
            return null;
        }
    }
}