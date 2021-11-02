package io.apimap.api.repository.nitrite.entity.support;

import io.apimap.api.repository.nitrite.entity.db.Metadata;
import io.apimap.api.repository.nitrite.entity.db.TaxonomyCollectionVersionURN;

import java.util.ArrayList;
import java.util.List;

public class ClassificationTreeCollection {
    TaxonomyCollectionVersionURN taxonomy;
    private List<Metadata> items = new ArrayList<>();

    public ClassificationTreeCollection() {
    }

    public ClassificationTreeCollection(TaxonomyCollectionVersionURN taxonomy, List<Metadata> items) {
        this.taxonomy = taxonomy;
        this.items = items;
    }

    public TaxonomyCollectionVersionURN getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(TaxonomyCollectionVersionURN taxonomy) {
        this.taxonomy = taxonomy;
    }

    public List<Metadata> getItems() {
        return items;
    }

    public void setItems(List<Metadata> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ClassificationTreeCollection{" +
                ", taxonomy=" + taxonomy +
                ", items=" + items +
                '}';
    }
}