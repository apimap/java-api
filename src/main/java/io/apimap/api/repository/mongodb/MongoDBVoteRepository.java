package io.apimap.api.repository.mongodb;

import io.apimap.api.repository.mongodb.documents.Vote;
import io.apimap.api.repository.repository.IVoteRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.LinkedHashMap;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.MongoConfiguration.class)
public class MongoDBVoteRepository extends MongoDBRepository implements IVoteRepository<Vote> {

    public MongoDBVoteRepository(final ReactiveMongoTemplate template) {
        super(template);
    }

    @Override
    public Flux<Vote> all(String apiId, String apiVersion){
        final Query query = new Query().addCriteria(Criteria.where("apiId").is(apiId));
        query.addCriteria(Criteria.where("apiVersion").is(apiVersion));

        return template
                .find(query, Vote.class);
    }

    @Override
    public Mono<Vote> add(Vote entity){
        entity.setCreated(new Date());
        return template.insert(entity);
    }

    @Override
    public Mono<Integer> rating(String apiId, String apiVersion) {
        final String FIELD_NAME = "averageRating";

         final TypedAggregation<Vote> aggregation = Aggregation.newAggregation(Vote.class,
                 Aggregation.match(Criteria.where("apiVersion").is(apiVersion)),
                 Aggregation.match(Criteria.where("apiId").is(apiId)),
                 Aggregation.group("apiId").avg("rating").as(FIELD_NAME)
        );

        return template
                .aggregate(aggregation, Vote.class, LinkedHashMap.class)
                .flatMap(e -> Mono.just(Double.valueOf((Double) e.get(FIELD_NAME)).intValue()))
                .take(1)
                .single(-1);
    }
}
