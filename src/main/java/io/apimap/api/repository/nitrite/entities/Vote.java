package io.apimap.api.repository.nitrite.entities;


import io.apimap.api.repository.interfaces.IVote;
import org.dizitart.no2.objects.Id;

import java.util.Date;

public class Vote implements IVote {
    protected String apiId;
    protected String apiVersion;
    protected Integer rating;
    protected Date created;

    @Id
    private String id;

    public Vote() {
    }

    public Vote(String apiId, String apiVersion, Integer rating, Date created) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = created;
        this.id = IVote.createId();
    }

    public Vote(String apiId, String apiVersion, Integer rating) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.rating = rating;
        this.created = new Date();
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
