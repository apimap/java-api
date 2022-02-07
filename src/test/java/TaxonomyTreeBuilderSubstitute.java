import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import io.apimap.api.utils.TaxonomyTreeBuilder;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyTreeBuilderSubstitute extends TaxonomyTreeBuilder {

    public TaxonomyTreeBuilderSubstitute() {
        super();
    }

    public static TaxonomyTreeBuilderSubstitute empty() {
        return new TaxonomyTreeBuilderSubstitute();
    }

    public boolean insert(TaxonomyTreeDataRestEntity newEntity) {
        return super.insert(newEntity);
    }

    public void recursiveInsert(ArrayList<TaxonomyTreeDataRestEntity> tree, TaxonomyTreeDataRestEntity node, String url, ArrayList<String> paths) {
        super.recursiveInsert(tree, node, url, paths);
    }

    public List<String> splitURLParts(String url) {
        return super.splitURLParts(url);
    }
}
