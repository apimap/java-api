import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassificationSearchFilterTest {
    @Test
    public void recursiveInsert_didSucceed(){
        TaxonomyTreeBuilderSubstitute util = new TaxonomyTreeBuilderSubstitute();

        TaxonomyTreeDataRestEntity node = new TaxonomyTreeDataRestEntity(
                "urn:apimap:1",
                "Test",
                "taxonomy://One/Two/Three",
                "Description",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        ArrayList<String> paths = new ArrayList<>(util.splitURLParts(node.getUrl()));
        util.recursiveInsert(util.getTree(), node, "taxonomy://" + util.splitURLParts(node.getUrl()).get(0), paths);

        assertEquals(1, util.getTree().size());
        assertEquals(1, util.getTree().get(0).getEntities().size());
    }

    @Test
    public void extendTree_didSucceed(){
        TaxonomyTreeBuilderSubstitute util = TaxonomyTreeBuilderSubstitute.empty();

        TaxonomyTreeDataRestEntity node1 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:1",
                "Test",
                "taxonomy://One/Two/Three",
                "Description",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node1);

        TaxonomyTreeDataRestEntity node2 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:2",
                "Test 2",
                "taxonomy://One",
                "Description 2",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node2);

        TaxonomyTreeDataRestEntity node3 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:3",
                "Test 3",
                "taxonomy://One/Two",
                "Description 3",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node3);

        assertEquals("urn:apimap:2", util.getTree().get(0).getUrn());
        assertEquals("urn:apimap:1", util.getTree().get(0).getEntities().get(0).getEntities().get(0).getUrn());
    }

    @Test
    public void extendTreeMultiRoot_didSucceed(){
        TaxonomyTreeBuilderSubstitute util = new TaxonomyTreeBuilderSubstitute();

        TaxonomyTreeDataRestEntity node1 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:1",
                "Test",
                "taxonomy://One/Two/Three",
                "Description",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node1);

        TaxonomyTreeDataRestEntity node2 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:2",
                "Test 2",
                "taxonomy://One",
                "Description 2",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node2);

        TaxonomyTreeDataRestEntity node3 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:3",
                "Test 3",
                "taxonomy://Two",
                "Description 3",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node3);

        TaxonomyTreeDataRestEntity node4 = new TaxonomyTreeDataRestEntity(
                "urn:apimap:4",
                "Test 4",
                "taxonomy://Two/One",
                "Description 4",
                "apimap",
                "1",
                TaxonomyDataRestEntity.ReferenceType.CLASSIFICATION,
                new ArrayList()
        );

        util.insert(node4);

        assertEquals("urn:apimap:2", util.getTree().get(0).getUrn());
        assertEquals("urn:apimap:3", util.getTree().get(1).getUrn());
        assertEquals("urn:apimap:1", util.getTree().get(0).getEntities().get(0).getEntities().get(0).getUrn());
        assertEquals("urn:apimap:4", util.getTree().get(1).getEntities().get(0).getUrn());
    }
}
