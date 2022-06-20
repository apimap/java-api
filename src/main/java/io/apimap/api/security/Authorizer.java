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
import io.apimap.api.repository.entities.IApi;
import io.apimap.api.repository.entities.ITaxonomyCollection;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.context.AuthorizationContext;
import io.apimap.api.service.context.TaxonomyContext;
import io.apimap.api.utils.RequestUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/*
 This is not to add security, it is just a minimal effort to make it a little harder
  to update someone else's information
 */
@Component("Authorizer")
public class Authorizer {
    protected IApiRepository apiRepository;
    protected ITaxonomyRepository taxonomyRepository;
    protected AccessConfiguration accessConfiguration;

    public Authorizer(IApiRepository apiRepository,
                      ITaxonomyRepository taxonomyRepository,
                      AccessConfiguration accessConfiguration) {
        this.apiRepository = apiRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.accessConfiguration = accessConfiguration;
    }

    /*
    Is the token valid to put/post/delete taxonomy/* resources
     */
    public Boolean isValidTaxonomyToken(ServerRequest request){
        final AuthorizationContext authorizationContext = RequestUtil.authorizationContextFromRequest(request);
        if (authorizationContext.isEmpty()) return Boolean.FALSE;

        final TaxonomyContext taxonomyContext = RequestUtil.taxonomyContextFromRequest(request);
        if (taxonomyContext.getNid() == null) return Boolean.FALSE;

        try {
            return (Boolean) taxonomyRepository
                    .getTaxonomyCollection(taxonomyContext.getNid())
                    .subscribeOn(Schedulers.boundedElastic())
                    .filter(collection -> ((ITaxonomyCollection) collection).getToken() != null)
                    .filter(collection -> ((ITaxonomyCollection) collection).getToken().equals(authorizationContext.getToken()))
                    .flatMap(collection -> Mono.just(Boolean.TRUE))
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Boolean.FALSE)))
                    .toFuture()
                    .get(); // Fix: Spring 5.8.x
        } catch (Exception ignored) {
            return false;
        }
    }

    /*
    Is the token valid to put/post/delete api/* resources?
     */
    public boolean isValidApiAccessToken(ServerRequest request){
        final AuthorizationContext authorizationContext = RequestUtil.authorizationContextFromRequest(request);
        if (authorizationContext.isEmpty()) return Boolean.FALSE;

        final ApiContext apiContext = RequestUtil.apiContextFromRequest(request);
        if (apiContext.getApiName() == null) return Boolean.FALSE;

        try {
            return (Boolean) apiRepository
                    .get(apiContext.getApiName())
                    .subscribeOn(Schedulers.boundedElastic())
                    .filter(api -> ((IApi) api).getToken() != null)
                    .filter(api -> ((IApi) api).getToken().equals(authorizationContext.getToken()))
                    .flatMap(api -> Mono.just(Boolean.TRUE))
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Boolean.FALSE)))
                    .toFuture()
                    .get(); // Fix: Spring 5.8.x
        } catch (Exception ignored) {
            return false;
        }
    }

    /*
    Is the token valid to get api/* zip resources?
     */
    public boolean isValidAccessToken(ServerRequest request) {
        final AuthorizationContext authorizationContext = RequestUtil.authorizationContextFromRequest(request);
        if (authorizationContext.isEmpty()) return Boolean.FALSE;

        return accessConfiguration.getToken().equals(authorizationContext.getToken());
    }
}
