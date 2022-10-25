package io.apimap.api.security;

import io.apimap.api.configuration.AccessConfiguration;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.ITaxonomyCollection;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.ITaxonomyRepository;
import io.apimap.api.router.ApiRouter;
import io.apimap.api.router.TaxonomyRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthorizerTest {

    private static final String API_NAME = "MyApi";
    private static final String API_TOKEN = UUID.randomUUID().toString();
    private static final String TAXONOMY_NID = "MyTaxonomy";
    private static final String TAXONOMY_TOKEN = UUID.randomUUID().toString();

    @Mock
    IApi api;
    @Mock
    private IApiRepository apiRepository;
    @Mock
    ITaxonomyCollection taxonomy;
    @Mock
    private ITaxonomyRepository taxonomyRepository;
    private final AccessConfiguration accessConfiguration = new AccessConfiguration();

    private Authorizer authorizer;

    @BeforeEach
    public void setUp() {
        authorizer = new Authorizer(apiRepository, taxonomyRepository, accessConfiguration);

        lenient().when(api.getToken()).thenReturn(API_TOKEN);
        lenient().when(apiRepository.get(API_NAME)).thenReturn(Mono.just(api));
        lenient().when(taxonomy.getToken()).thenReturn(TAXONOMY_TOKEN);
        lenient().when(taxonomyRepository.getTaxonomyCollection(TAXONOMY_NID)).thenReturn(Mono.just(taxonomy));
    }

    @Test
    public void shouldAcceptValidApiToken() {
        var request = MockServerRequest.builder()
                .pathVariable(ApiRouter.API_NAME_KEY, API_NAME)
                .header("Authorization", "Bearer " + API_TOKEN)
                .build();
        assertThat(authorizer.isValidApiAccessToken(request))
                .as("isValidApiAccessToken result")
                .isTrue();
    }

    @Test
    public void shouldRejectMissingApiToken() {
        var request = MockServerRequest.builder()
                .pathVariable(ApiRouter.API_NAME_KEY, API_NAME)
                .build();
        assertThat(authorizer.isValidApiAccessToken(request))
                .as("isValidApiAccessToken result")
                .isFalse();
    }


    @ParameterizedTest
    @MethodSource("invalidAuthorizations")
    public void shouldRejectInvalidApiTokens(String authorization) {
        var request = MockServerRequest.builder()
                .pathVariable(ApiRouter.API_NAME_KEY, API_NAME)
                .header("Authorization", authorization)
                .build();
        assertThat(authorizer.isValidApiAccessToken(request))
                .as("isValidApiAccessToken result")
                .isFalse();
    }

    @Test
    public void shouldAcceptValidTaxonomyToken() {
        var request = MockServerRequest.builder()
                .pathVariable(TaxonomyRouter.TAXONOMY_NID_KEY, TAXONOMY_NID)
                .header("Authorization", "Bearer " + TAXONOMY_TOKEN)
                .build();
        assertThat(authorizer.isValidTaxonomyToken(request))
                .as("isValidTaxonomyToken result")
                .isTrue();
    }


    @ParameterizedTest
    @MethodSource("invalidAuthorizations")
    public void shouldRejectInvalidTaxonomyTokens(String authorization) {
        var request = MockServerRequest.builder()
                .pathVariable(TaxonomyRouter.TAXONOMY_NID_KEY, TAXONOMY_NID)
                .header("Authorization", authorization)
                .build();
        assertThat(authorizer.isValidTaxonomyToken(request))
                .as("isValidTaxonomyToken result")
                .isFalse();
    }

    protected static Stream<String> invalidAuthorizations() {
        return Stream.of(
                "",
                "Bearer",
                "Bearer ",
                "Bearer abcde",
                "Bearer " + UUID.randomUUID()
        );
    }
}