package io.apimap.api.repository.mongodb.documents;

import io.apimap.api.repository.interfaces.IDocument;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Date;

public class Document implements IDocument {
    protected String apiId;
    protected String apiVersion;
    protected String body;
    protected Instant created;
    protected DocumentType type;

    @Id
    private String id;

    public Document() {
    }

    public Document(final String apiId,
                    final String apiVersion,
                    final String body,
                    final Object created,
                    final DocumentType type) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.body = body;
        this.type = type;

        if(created instanceof Date){
            this.created = ((Date) created).toInstant();
        }else if(created instanceof Instant){
            this.created = (Instant) created;
        }else{
            this.created = Instant.now();
        }

        this.id = IDocument.createId(apiId, apiVersion, type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
        this.id = IDocument.createId(apiId, apiVersion, type);

    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        this.id = IDocument.createId(apiId, apiVersion, type);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public void setCreated(Date created) {
        if(created != null){
            this.created = created.toInstant();
        }else{
            this.created = null;
        }
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
        this.id = IDocument.createId(apiId, apiVersion, type);
    }

    @Override
    public String toString() {
        return "MetadataDocument{" +
                "apiId='" + apiId + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", body='" + body + '\'' +
                ", created=" + created +
                ", type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}
