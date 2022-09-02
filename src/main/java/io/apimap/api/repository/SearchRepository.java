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

package io.apimap.api.repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IApiClassification;
import io.apimap.api.repository.interfaces.IApiVersion;
import io.apimap.api.repository.interfaces.IMetadata;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IClassificationRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.service.query.ClassificationFilter;
import io.apimap.api.service.query.Filter;
import io.apimap.api.service.query.MetadataFilter;
import io.apimap.api.service.query.QueryFilter;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;

@Repository
public class SearchRepository {

    final protected IClassificationRepository classificationRepository;
    final protected IApiRepository apiRepository;
    final protected IMetadataRepository metadataRepository;

    @SuppressFBWarnings
    public SearchRepository(final IClassificationRepository classificationRepository,
                            final IApiRepository apiRepository,
                            final IMetadataRepository metadataRepository,
                            final ITaxonomyRepository taxonomyRepository) {
        this.classificationRepository = classificationRepository;
        this.apiRepository = apiRepository;
        this.metadataRepository = metadataRepository;
    }

    public Flux<Tuple3<IApi, IMetadata, IApiVersion>> find(List<Filter> filters, QueryFilter queryFilter) {

        // Only ClassificationFilters used
        if(queryFilter == null && filters.stream().noneMatch(filter -> filter instanceof MetadataFilter)){
            return classificationRepository
                .allByFilters(classificationRepository.queryFilters(filters))
                .flatMap(classification -> metadataRepository
                    .get(((IApiClassification) classification).getApiId(), ((IApiClassification) classification).getApiVersion())
                )
                .flatMap(metadata -> apiRepository.getById(((IMetadata) metadata).getApiId())
                    .flatMap(api -> Mono.just(Tuples.of(api, metadata)))
                    .flatMap(result -> apiRepository.getApiVersion(((IApi) ((Tuple2) result).getT1()).getId(), ((IMetadata) ((Tuple2) result).getT2()).getApiVersion())
                        .flatMap(apiVersion -> Mono.just(Tuples.of(((Tuple2<?, ?>) result).getT1(), ((Tuple2<?, ?>) result).getT2(), apiVersion)))
                    )
                );
        }

        // Only MetadataFilter and/or QueryFilters used
        if(filters.stream().noneMatch(filter -> filter instanceof ClassificationFilter)) {
            return metadataRepository
                .allByFilters(metadataRepository.queryFilters(filters, queryFilter))
                .flatMap(metadata -> apiRepository.getById(((IMetadata) metadata).getApiId())
                    .flatMap(api -> Mono.just(Tuples.of(api, metadata)))
                    .flatMap(result -> apiRepository.getApiVersion(((IApi) ((Tuple2) result).getT1()).getId(), ((IMetadata) ((Tuple2) result).getT2()).getApiVersion())
                        .flatMap(apiVersion -> Mono.just(Tuples.of(((Tuple2<?, ?>) result).getT1(), ((Tuple2<?, ?>) result).getT2(), apiVersion)))
                    )
                );
        }

        // MetadataFilter and ClassificationFilters
        return classificationRepository
            .allByFilters(classificationRepository.queryFilters(filters))
            .flatMap(classification -> metadataRepository
                .get(((IApiClassification) classification).getApiId(), ((IApiClassification) classification).getApiVersion())
            )
            .filter(metadata -> IMetadata.compliesWithFilters((IMetadata)metadata, filters))
            .flatMap(metadata -> apiRepository.getById(((IMetadata) metadata).getApiId())
                .flatMap(api -> Mono.just(Tuples.of(api, metadata)))
                .flatMap(result -> apiRepository.getApiVersion(((IApi) ((Tuple2) result).getT1()).getId(), ((IMetadata) ((Tuple2) result).getT2()).getApiVersion())
                    .flatMap(apiVersion -> Mono.just(Tuples.of(((Tuple2<?, ?>) result).getT1(), ((Tuple2<?, ?>) result).getT2(), apiVersion)))
                )
            );
    }
}