package io.apimap.api.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IVote;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IVoteRepository;
import io.apimap.api.rest.VoteDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
public class ApiVoteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiVoteService.class);

    final protected IVoteRepository voteRepository;
    final protected IApiRepository apiRepository;
    final protected ApimapConfiguration apimapConfiguration;
    final protected IRESTConverter entityMapper;

    public ApiVoteService(final IVoteRepository voteRepository,
                          final IApiRepository apiRepository,
                          final ApimapConfiguration apimapConfiguration,
                          final IRESTConverter entityMapper) {
        this.voteRepository = voteRepository;
        this.apiRepository = apiRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }

    @NotNull
    public Mono<ServerResponse> createVote(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);
        final JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(JsonApiRestRequestWrapper.class, VoteDataRestEntity.class);
        final URI uri = request.uri();

        return request
                .bodyToMono(ParameterizedTypeReference.forType(type))
                .flatMap(vote -> apiRepository
                                    .get(context.getApiName())
                                    .flatMap(api -> entityMapper.decodeVote(context.withApiId(((IApi) api).getId()), (JsonApiRestRequestWrapper<VoteDataRestEntity>) vote))
                )
                .doOnNext(vote -> {
                    if(((IVote) vote).getRating() > 5 || ((IVote) vote).getRating() < 1 ){
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating only allowed with values 1 to 5");
                    }
                })
                .flatMap(vote -> voteRepository.add((IVote) vote))
                .flatMap(vote -> entityMapper.encodeVote(uri, (IVote) vote))
                .flatMap(version -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<VoteDataRestEntity>) version)
                        .created(false)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    public Mono<ServerResponse> allVotes(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMapMany(api -> voteRepository.all(((IApi) api).getId(), context.getApiVersion()))
                .collectList()
                .flatMap(votes -> entityMapper.encodeVotes(uri, (List) votes))
                .flatMap(metadata -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .withResourceURI(uri)
                        .withBody((JsonApiRestResponseWrapper<?>) metadata)
                        .okResource()
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }
}
