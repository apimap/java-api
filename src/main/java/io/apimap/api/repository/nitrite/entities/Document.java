package io.apimap.api.repository.nitrite.entities;

import io.apimap.api.repository.interfaces.IDocument;
import org.dizitart.no2.objects.Id;

import java.util.Date;

public class Document implements IDocument {
    protected String apiId;
    protected String apiVersion;
    protected String body;
    protected Date created;
    protected DocumentType type;

    @Id
    private String id;

    public Document() {
    }

    public Document(String apiId, String apiVersion, String body, Date created, DocumentType type) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.body = body;
        this.created = created;
        this.type = type;
        this.id = IDocument.createId(apiId, apiVersion, type);
    }

    @Override
    public String getApiId() {
        return apiId;
    }

    @Override
    public void setApiId(String apiId) {
        this.apiId = apiId;
        this.id = IDocument.createId(this.apiId, this.apiVersion, this.type);
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        this.id = IDocument.createId(this.apiId, this.apiVersion, this.type);
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public DocumentType getType() {
        return type;
    }

    @Override
    public void setType(DocumentType type) {
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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
