package io.apimap.api.repository.mongodb.documents;

import io.apimap.api.repository.interfaces.IVote;
import org.springframework.data.annotation.Id;

import java.time.Instant;

public class Vote implements IVote {
    protected String apiId;
    protected String apiVersion;
    protected Integer rating;
    protected Instant created;

    @Id
    private String id;

    public Vote() {
    }

    public Vote(final String apiId,
                final String apiVersion,
                final Integer rating,
                final Instant created) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = created;
        this.id = IVote.createId();
    }

    public Vote(final String apiId,
                final String apiVersion,
                final Integer rating) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = Instant.now();
        this.id = IVote.createId();
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer value) {
        this.rating = value;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
