package autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Runs the shared suite on {@link Solution1}, plus solution-specific tests. */
class Solution1Test extends DictionaryTest {

    // Tell the shared suite which dictionary to build for each test
    @Override
    protected Dictionary createDictionary() { return new Solution1(); }

    /** Solution-1-specific: the internal hash table must resize correctly. */
    @Test
    void manyInsertsStillWorkAfterResizing() {
        // 5000 inserts blow way past the initial 101 buckets, so the map resizes a bunch
        Solution1 s = new Solution1();
        for (int i = 0; i < 5_000; i++) s.add("k" + i, i);
        // Nothing should have been lost during all those resizes
        assertEquals(5_000, s.size());
        // Every single key should still map to its original count
        for (int i = 0; i < 5_000; i++) assertEquals(i, s.frequency("k" + i));
    }

    /** Solution-1-specific: complete() still returns the correct top-k after removals. */
    @Test
    void completeAfterManyRemovals() {
        // Add a0..a199 with counts 0..199, so a199 has the highest count
        Solution1 s = new Solution1();
        for (int i = 0; i < 200; i++) s.add("a" + i, i);
        // Remove every other word (a0, a2, a4, ...), leaving only the odd ones
        for (int i = 0; i < 200; i += 2) s.remove("a" + i);
        // The top 3 should be the highest-count survivors: 199, 197, 195
        assertEquals(List.of("a199", "a197", "a195"), s.complete("a", 3));
    }
}
