package io.apimap.api.repository.interfaces;

import java.time.Instant;

public interface IDocument {
    enum DocumentType {
        README,
        CHANGELOG
    }

    static String createId(String apiId, String apiVersion, DocumentType type) {
        return apiId + "#" + apiVersion + "#" + type;
    }

    public String getId();

    public void setId(String id);

    public String getApiId();

    public void setApiId(String apiId);

    public String getApiVersion();

    public void setApiVersion(String apiVersion);

    public String getBody();

    public void setBody(String body);

    public Instant getCreated();

    public void setCreated(Instant created);

    public DocumentType getType();

    public void setType(DocumentType type);
}
