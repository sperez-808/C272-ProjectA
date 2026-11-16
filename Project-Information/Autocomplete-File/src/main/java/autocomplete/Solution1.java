package autocomplete;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Solution 1 — chained hash map (separate chaining).</b>
 *
 * <p>Adapted from the COMP 272 {@code ChainHashMap} course code.
 * Words are stored in a {@link ChainHashMap} keyed by the word; the value is
 * the word's frequency count.
 *
 * <p><b>Time complexities:</b>
 * <ul>
 *   <li>{@code add} / {@code remove} / {@code contains} / {@code frequency} — O(1) average</li>
 *   <li>{@code complete(prefix, k)} — O(n + m log m): scan all n keys, sort the
 *       m matches by (count desc, word asc)</li>
 *   <li>{@code size} — O(1)</li>
 * </ul>
 *
 * <p><b>Trade-off:</b> fast exact lookup but <em>no key order</em>, so
 * {@code complete} cannot prune the search — every key is examined.
 */
public class Solution1 implements Dictionary {

    // to store words nd their frequency counts
    // the reference never changes after the constructor, so mark it final
    private final ChainHashMap<String, Long> dictionary;

    public Solution1() {
        // initialize your data structure.
        // 101 is prime - the map resizes automatically as it fills
        dictionary = new ChainHashMap<>(101);
    }

    @Override public void add(String word, long count) {
        //add a word or replace a word's existing frequency
        dictionary.put(word, count);
    }

    @Override public boolean remove(String word) {
        //removes word and return whether it existed
        return dictionary.remove(word) != null;
    }

    @Override public boolean contains(String word) {
        //check if word exists
        return dictionary.get(word) != null;
    }

    @Override public long frequency(String word) {
        //get the word's frequency - return 0 if word does not exist
        Long c = dictionary.get(word);
        return c == null ? 0 : c;
    }

    @Override
    public List<String> complete(String prefix, int k) {
        //create list for matching words
        List<String> matches = new ArrayList<>();
        // Return empty list if k is 0 or less
        if (k <= 0) return matches;

        //find all the words starting with a prefix
        // A hash map has no key order, so need to check every key
        for (String word : dictionary.keySet()) {
            if (word.startsWith(prefix)) matches.add(word);
        }
        //sort by frequency (highest first)
        //break ties by listing words alphabetically
        matches.sort((a, b) -> {
            long ca = dictionary.get(a);
            long cb = dictionary.get(b);
            return ca != cb ? Long.compare(cb, ca) : a.compareTo(b);
        });
        //return at most k matching words
        return new ArrayList<>(matches.subList(0, Math.min(k, matches.size())));
    }

    @Override public int size() {
        //return number of stored words
        return dictionary.size();
    }
}
