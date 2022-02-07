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

package io.apimap.api.security;

import io.apimap.api.configuration.AccessConfiguration;
import io.apimap.api.repository.nitrite.NitriteApiRepository;
import io.apimap.api.repository.nitrite.NitriteTaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollection;
import io.apimap.api.utils.RequestUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Optional;

/*
 This is not to add security, it is just a minimal effort to make it a little harder
  to update someone else's information
 */
@Component("Authorizer")
public class Authorizer {
    protected NitriteApiRepository nitriteApiRepository;
    protected NitriteTaxonomyRepository nitriteTaxonomyRepository;
    protected AccessConfiguration accessConfiguration;

    public Authorizer(NitriteApiRepository nitriteApiRepository,
                      NitriteTaxonomyRepository nitriteTaxonomyRepository,
                      AccessConfiguration accessConfiguration) {
        this.nitriteApiRepository = nitriteApiRepository;
        this.nitriteTaxonomyRepository = nitriteTaxonomyRepository;
        this.accessConfiguration = accessConfiguration;
    }

    /*
    Is the token valid to put/post/delete taxonomy/* resources
     */
    public boolean isValidTaxonomyToken(ServerRequest request) {
        try {
            if (request == null)
                return false;

            final String taxonomyNid = RequestUtil.taxonomyNidFromRequest(request);
            if (taxonomyNid == null)
                return false;

            final String token = RequestUtil.bearerTokenFromRequest(request);
            if (token == null || token.isEmpty())
                return false;

            Optional<TaxonomyCollection> entity = nitriteTaxonomyRepository.getTaxonomyCollection(taxonomyNid, true);

            return entity.map(collection -> {
                if (collection.getToken() == null) return false;
                return collection.getToken().equals(token);
            }).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /*
    Is the token valid to put/post/delete api/* resources?
     */
    public boolean isValidApiAccessToken(ServerRequest request) {
        try {
            if (request == null)
                return false;

            final String apiName = RequestUtil.apiNameFromRequest(request);
            if (apiName == null)
                return false;

            final String token = RequestUtil.bearerTokenFromRequest(request);
            if (token == null || token.isEmpty())
                return false;

            Optional<Api> entity = nitriteApiRepository.get(apiName, true);

            if(entity.isPresent()
                && entity.get().getToken() != null
                && entity.get().getToken().equals(token)){
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    Is the token valid to get api/* zip resources?
     */
    public boolean isValidAccessToken(ServerRequest request) {
        try {
            if (request == null)
                return false;

            final String token = RequestUtil.bearerTokenFromRequest(request);
            if (token == null || token.isEmpty())
                return false;

            return accessConfiguration.getToken().equals(token);
        } catch (Exception e) {
            return false;
        }
    }
}
