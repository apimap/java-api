package io.apimap.api.repository.interfaces;

import java.time.Instant;
import java.util.UUID;

public interface IVote {
    static String createId() {
        return UUID.randomUUID().toString();
    }

    public String getApiId();
    public void setApiId(String apiId);
    public String getApiVersion();
    public void setApiVersion(String apiVersion);
    public Integer getRating();
    public void setRating(Integer value);
    public Instant getCreated();
    public void setCreated(Instant created);
    public String getId();
    public void setId(String id);
}
