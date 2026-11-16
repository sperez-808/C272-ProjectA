package autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Runs the shared suite on {@link Solution2}, plus solution-specific tests. */
class Solution2Test extends DictionaryTest {

    // Tell the shared suite which dictionary to build for each test
    @Override
    protected Dictionary createDictionary() { return new Solution2(); }

    // Sorted insertion must keep the tree balanced
    @Test
    void sortedInsertKeepsHeightLogarithmic() {
        Solution2 s = new Solution2();
        int n = 20_000;
        // Sorted keys are the classic BST worst case, but the AVL rotations save the case
        for (int i = 0; i < n; i++) s.add(String.format("w%06d", i), i);
        // A balanced tree of 20k nodes has height around 15, so 25 is a good upper bound
        assertTrue(avlHeight(s) <= 25, "AVL height too large: " + avlHeight(s));
        // Every key needs to be findable after all the rotating
        for (int i = 0; i < n; i++)
            assertEquals(i, s.frequency(String.format("w%06d", i)));
    }

    // Rebalancing after remove
    @Test
    void rebalancesAfterRemovingAllInOrder() {
        Solution2 s = new Solution2();
        // Fill the tree in sorted order, so it starts nice and balanced
        for (int i = 0; i < 1_000; i++) s.add(String.format("w%04d", i), i);
        // Delete everything one by one, in the same order, and rebalancing has to keep up
        for (int i = 0; i < 1_000; i++) assertTrue(s.remove(String.format("w%04d", i)));
        // Assure empty tree after removing
        assertEquals(0, s.size());
        // The empty tree has height -1, not 0
        assertEquals(-1, avlHeight(s));
    }

    // The complete() on a tree built from sorted input
    @Test
    void completeFromSortedInput() {
        Solution2 s = new Solution2();
        // Add the "pro" words out of alphabetical order, so the tree has to sort them
        s.add("probe", 5); s.add("process", 100); s.add("problem", 50);
        s.add("pro", 10); s.add("profit", 20);
        // Only three get returned, ranked by count: 100, 50, 20
            // Probe (5) and pro (10) miss out
        assertEquals(List.of("process", "problem", "profit"), s.complete("pro", 3));
    }

    // Reflection helper
    // The fields are private, so use reflection for the tests
    private static int avlHeight(Solution2 s) {
        try {
            // Grab the private "root" field and make it readable
            var rootField = Solution2.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Object root = rootField.get(s);
            // Null root means an empty tree, report as height -1
            if (root == null) return -1;
            // Grab the private "height" field on the node itself
            var h = root.getClass().getDeclaredField("height");
            h.setAccessible(true);
            return (int) h.get(root);
        } catch (ReflectiveOperationException e) {
            // If reflection breaks, fail loudly instead of silently returning a wrong height
            throw new AssertionError(e);
        }
    }
}
