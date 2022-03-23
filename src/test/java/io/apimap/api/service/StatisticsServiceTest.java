package io.apimap.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.ITaxonomyRepository;
import io.apimap.api.repository.nitrite.entity.db.Api;
import io.apimap.api.repository.nitrite.entity.db.ApiVersion;
import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

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
    ServerRequest serverRequest;

    @Test
    void getApiCreatedStatistics() {
        //Add two APIs to the catalogue
        List<ApiCollection.Item> items = new ArrayList<>(2);
        List<String> parents = new ArrayList<>(0);
        String theFirstApiName = "The first API";
        Date theFirstApiCreatedDate = new Date();
        ApiCollection.Item firstApi = getAnAPICollectionEntry(theFirstApiName, theFirstApiCreatedDate);
        items.add(firstApi);
        String theSecondApiName = "The second API";
        Date theSecondApiCreatedDate = new Date();
        ApiCollection.Item secondApi = getAnAPICollectionEntry(theSecondApiName, theSecondApiCreatedDate);
        items.add(secondApi);
        ApiCollection apiCollection = new ApiCollection(items, parents );

        Mockito.when(mockAPIRepository.all()).thenReturn(apiCollection);

        StatisticsService statisticsService = new StatisticsService( mockAPIRepository, mockTaxonomyRepository, mockMetadataRepository, mockApimapConfiguration  );

        //Mock the request uri
        Mockito.when(serverRequest.uri()).thenReturn(URI.create("api-catalog-entry"));

        //Execute the query
        Mono<ServerResponse> serverResponseMono = statisticsService.getApiCreatedStatistics( serverRequest );
        ServerResponse serverResponse = serverResponseMono.block();
        //Not sure how to avoid this unchecked assignment. Suggestions?
        JsonApiRestResponseWrapper<ArrayList<Map<String,Object>>> jsonApiRestResponseWrapper = ServerResponseExtractor.serverResponseAsObject( serverResponse, new ObjectMapper(), JsonApiRestResponseWrapper.class );
        ArrayList<Map<String,Object>> data = jsonApiRestResponseWrapper.getData();

        //Assert the response
        Assertions.assertThat(data).containsExactly(
                Map.of("id", theFirstApiName, "type", JsonApiRestResponseWrapper.STATISTICS_ENTRY, "attributes", Map.of("key", theFirstApiName, "value", theFirstApiCreatedDate.toString())),
                Map.of("id", theSecondApiName, "type", JsonApiRestResponseWrapper.STATISTICS_ENTRY, "attributes", Map.of("key", theSecondApiName, "value", theSecondApiCreatedDate.toString()))
        );

    }

    @NotNull
    private ApiCollection.Item getAnAPICollectionEntry(String name, Date apiCreatedDate) {
        Api anApi = new Api();
        anApi.setName(name);
        ApiVersion anApiVersion = new ApiVersion("1.0", apiCreatedDate, "apiId");

        Metadata metadata = new Metadata(null, name, null, null, null, null, null, anApiVersion.getVersion(), null, null, null, null, Collections.emptyList(), apiCreatedDate);

        return new ApiCollection.Item( anApi, Optional.of(anApiVersion), Optional.of(metadata));
    }
}