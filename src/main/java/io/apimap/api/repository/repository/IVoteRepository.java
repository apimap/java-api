package io.apimap.api.repository.repository;

import io.apimap.api.repository.interfaces.IVote;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IVoteRepository <TIVote extends IVote> {

    Flux<TIVote> all(String apiId, String apiVersion);
    Mono<TIVote> add(TIVote entity);
    Mono<Integer> rating(String apiId, String apiVersion);
}
