package io.apimap.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.entities.IApi;
import io.apimap.api.repository.entities.IRESTEntityMapper;
import io.apimap.api.repository.generic.StatisticsValue;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.rest.StatisticsCollectionRootRestEntity;
import io.apimap.api.rest.StatisticsDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class StatisticsServiceTest {
    @Mock
    IApiRepository mockAPIRepository;

    @Mock
    ITaxonomyRepository mockTaxonomyRepository;

    @Mock
    IMetadataRepository mockMetadataRepository;

    @Mock
    ApimapConfiguration mockApimapConfiguration;

    @Mock
    IRESTEntityMapper irestEntityMapper;

    @Mock
    ServerRequest serverRequest;

    @Test
    void getApiCreatedStatistics() {
        final String api1Name = "The first API";
        final String api2Name = "The second API";

        final Date apiCreated = new Date();


        IApi api1 = Mockito.mock(IApi.class);
        Mockito.when(api1.getName()).thenReturn(api1Name);
        Mockito.when(api1.getCreated()).thenReturn(apiCreated);

        IApi api2 = Mockito.mock(IApi.class);
        Mockito.when(api2.getName()).thenReturn(api2Name);
        Mockito.when(api2.getCreated()).thenReturn(apiCreated);

        Flux content = Flux.just(api1, api2);

        Mockito.when(mockAPIRepository.all()).thenReturn(content);

        StatisticsService statisticsService = new StatisticsService(
                mockAPIRepository,
                mockTaxonomyRepository,
                mockMetadataRepository,
                mockApimapConfiguration,
                irestEntityMapper);

        Mockito.when(serverRequest.uri()).thenReturn(URI.create("api-catalog-entry"));

        Mockito.when(irestEntityMapper.encodeStatistics(any(), any())).thenAnswer(invocation -> {
            ArrayList<StatisticsDataRestEntity> items = (ArrayList<StatisticsDataRestEntity>) invocation.getArgument(1, List.class)
                    .stream()
                    .map(e -> new StatisticsDataRestEntity(
                            ((StatisticsValue) e).getKey(),
                            ((StatisticsValue) e).getKey(),
                            ((StatisticsValue) e).getValue()
                    ))
                    .collect(Collectors.toCollection(ArrayList::new));

            return Mono.justOrEmpty(
                    new JsonApiRestResponseWrapper<>(new StatisticsCollectionRootRestEntity(items))
            );
        });

        Mono<ServerResponse> serverResponseMono = statisticsService.getApiCreatedStatistics(serverRequest);
        ServerResponse serverResponse = serverResponseMono.block();

        JsonApiRestResponseWrapper<ArrayList<Map<String, Object>>> jsonApiRestResponseWrapper = ServerResponseExtractor.serverResponseAsObject(serverResponse, new ObjectMapper(), JsonApiRestResponseWrapper.class);
        ArrayList<Map<String, Object>> data = jsonApiRestResponseWrapper.getData();

        Assertions.assertThat(data).containsExactly(
                Map.of("id", api1Name, "type", JsonApiRestResponseWrapper.STATISTICS_ENTRY, "attributes", Map.of("key", api1Name, "value", apiCreated.toString())),
                Map.of("id", api2Name, "type", JsonApiRestResponseWrapper.STATISTICS_ENTRY, "attributes", Map.of("key", api2Name, "value", apiCreated.toString()))
        );
    }
}