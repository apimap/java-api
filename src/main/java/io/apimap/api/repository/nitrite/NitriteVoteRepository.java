package io.apimap.api.repository.nitrite;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.configuration.NitriteConfiguration;
import io.apimap.api.repository.nitrite.entities.Vote;
import io.apimap.api.repository.repository.IVoteRepository;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Repository
@ConditionalOnBean(io.apimap.api.configuration.NitriteConfiguration.class)
public class NitriteVoteRepository extends NitriteRepository implements IVoteRepository<Vote> {

    @SuppressFBWarnings
    public NitriteVoteRepository(final NitriteConfiguration nitriteConfiguration) {
        super(nitriteConfiguration, "vote");
    }

    @Override
    public Flux<Vote> all(final String apiId, final String apiVersion) {
        final ObjectRepository<Vote> repository = database.getRepository(Vote.class);
        Cursor<Vote> cursor = repository.find(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        );
        return Flux.fromIterable(cursor);
    }

    @Override
    public Mono<Vote> add(final Vote entity) {
        entity.setCreated(Instant.now());

        final ObjectRepository<Vote> repository = database.getRepository(Vote.class);
        return Mono.justOrEmpty(repository.getById(repository.insert(entity).iterator().next()));
    }

    @Override
    public Mono<Integer> rating(String apiId, String apiVersion) {
        final ObjectRepository<Vote> repository = database.getRepository(Vote.class);
        Cursor<Vote> cursor = repository.find(
                and(eq("apiId", apiId), eq("apiVersion", apiVersion))
        );

        double average = cursor
                .toList()
                .stream()
                .mapToDouble(Vote::getRating)
                .average()
                .orElse(-1);

        return Mono.just(Double.valueOf(average).intValue());
    }
}
