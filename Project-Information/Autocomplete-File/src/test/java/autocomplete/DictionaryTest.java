package autocomplete;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Shared test suite. All original tests are kept verbatim (the {@code @Disabled}
 * on the dataset test is removed, per the starter instructions). New tests
 * cover every item on the TODO list and every workload in the application sheet.
 *
 * <p>Dataset tests require {@code data/count_1w.txt} (and, for the book test,
 * {@code data/book.txt}). The original dataset test is <b>strict</b> as the
 * spec requires: it fails if the file is missing. Our <i>added</i> dataset
 * tests use {@link org.junit.jupiter.api.Assumptions#assumeTrue} so they skip
 * cleanly when the data files are absent, without cluttering the output.
 */
abstract class DictionaryTest {

    // each concrete test class says which solution to build
    protected abstract Dictionary createDictionary();

    // the dictionary under test, rebuilt fresh before every test
    private Dictionary dict;

    // Full word list loaded once per test class.
    // Static, so it is shared by all tests and only read from disk once
    private static List<DatasetReader.WordCount> DATASET;
    // path to the book, or null if the file isn't there
    private static Path BOOK;

    @BeforeAll
    static void loadDataset() throws IOException {
        // Load the word list once so it doesn't re-read the file for every test
        Path wordFile = Path.of("data", "count_1w.txt");
        if (Files.exists(wordFile)) {
            DATASET = DatasetReader.readWordCounts(wordFile, Integer.MAX_VALUE);
        }
        // Only keep the book path if the file is actually there
        Path bookFile = Path.of("data", "book.txt");
        if (Files.exists(bookFile)) BOOK = bookFile;
    }

    // Setting up an empty dictionary before every test
    @BeforeEach
    void setUp() { dict = createDictionary(); }

    // ------------------------------------------------------------------
    // add, contains, frequency, size
    // ------------------------------------------------------------------

    @Test void newDictionaryIsEmpty() {
        assertEquals(0, dict.size());
        assertFalse(dict.contains("the"));
    }
    @Test void addedWordIsContained() {
        dict.add("cat", 5);
        assertTrue(dict.contains("cat"));
        assertEquals(1, dict.size());
    }
    @Test void missingWordIsNotContainedAndHasFrequencyZero() {
        dict.add("cat", 5);
        assertFalse(dict.contains("dog"));
        assertEquals(0, dict.frequency("dog"));
    }
    @Test void frequencyReturnsTheCount() {
        dict.add("cat", 5); dict.add("dog", 12);
        assertEquals(5, dict.frequency("cat"));
        assertEquals(12, dict.frequency("dog"));
    }
    @Test void addingAnExistingWordReplacesItsCount() {
        dict.add("cat", 5); dict.add("cat", 40);
        assertEquals(40, dict.frequency("cat"));
        assertEquals(1, dict.size());
    }
    @Test void wordsAreCaseSensitive() {
        dict.add("Cat", 5);
        assertTrue(dict.contains("Cat"));
        assertFalse(dict.contains("cat"));
    }
    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------
    
    @Test void removingAPresentWordReturnsTrueAndRemovesIt() {
        dict.add("cat", 5); dict.add("dog", 12);
        assertTrue(dict.remove("cat"));
        assertFalse(dict.contains("cat"));
        assertEquals(0, dict.frequency("cat"));
        assertEquals(1, dict.size());
        assertTrue(dict.contains("dog"));
    }
    @Test void removingAMissingWordReturnsFalse() {
        dict.add("cat", 5);
        assertFalse(dict.remove("dog"));
        assertEquals(1, dict.size());
    }
    @Test void removingTheLastWordEmptiesTheDictionary() {
        dict.add("cat", 5); dict.remove("cat");
        assertEquals(0, dict.size());
        assertEquals(List.of(), dict.complete("", 10));
    }

    // ------------------------------------------------------------------
    // complete
    // ------------------------------------------------------------------

    @Test void completeReturnsTheMostFrequentWordsFirst() {
        dict.add("the", 100); dict.add("they", 30); dict.add("then", 50);
        dict.add("there", 70); dict.add("cat", 1000);
        assertEquals(List.of("the", "there", "then"), dict.complete("th", 3));
    }

    @Test void completeBreaksTiesAlphabetically() {
        dict.add("tea", 10); dict.add("ten", 10);
        dict.add("tab", 10); dict.add("toe", 20);
        assertEquals(List.of("toe", "tab", "tea", "ten"), dict.complete("t", 10));
    }

    @Test void completeIncludesThePrefixItselfWhenItIsAWord() {
        dict.add("car", 10); dict.add("cart", 5);
        assertEquals(List.of("car", "cart"), dict.complete("car", 5));
    }
    
    @Test void emptyPrefixMatchesEveryWord() {
        dict.add("b", 1); dict.add("a", 2); dict.add("c", 3);
        assertEquals(List.of("c", "a", "b"), dict.complete("", 10));
    }

    @Test void completeReturnsAllMatchesWhenFewerThanK() {
        dict.add("dog", 3); dict.add("door", 7);
        assertEquals(List.of("door", "dog"), dict.complete("do", 100));
    }

    @Test void completeWithNoMatchReturnsAnEmptyList() {
        dict.add("dog", 3);
        assertEquals(List.of(), dict.complete("x", 5));
    }

    @Test void completeWithKZeroReturnsAnEmptyList() {
        dict.add("dog", 3);
        assertEquals(List.of(), dict.complete("d", 0));
    }

    @Test void completeDoesNotReturnRemovedWords() {
        dict.add("sun", 9); dict.add("sum", 8); dict.remove("sun");
        assertEquals(List.of("sum"), dict.complete("su", 5));
    }
    
    // ------------------------------------------------------------------
    // Larger inputs
    // ------------------------------------------------------------------

    @Test void manyWordsAddedInSortedOrderAreAllFound() {
        int n = 20_000;
        for (int i = 0; i < n; i++) dict.add(String.format("w%06d", i), i);
        assertEquals(n, dict.size());
        for (int i = 0; i < n; i++)
            assertEquals(i, dict.frequency(String.format("w%06d", i)));
    }

    @Test void randomOperationsGiveTheSameResultsAsTreeMap() {
        TreeMap<String, Long> ref = new TreeMap<>();
        Random rnd = new Random(42);
        for (int step = 0; step < 50_000; step++) {
            String w = "w" + rnd.nextInt(2_000);
            switch (rnd.nextInt(3)) {
                // pick an op at random: add, remove, or frequency
                case 0 -> { long c = rnd.nextInt(1_000); dict.add(w, c); ref.put(w, c); }
                case 1 -> assertEquals(ref.remove(w) != null, dict.remove(w), "remove " + w);
                default -> assertEquals(ref.getOrDefault(w, 0L), dict.frequency(w), "freq " + w);
            }
            // Sizes must stay in sync after every single step
            assertEquals(ref.size(), dict.size());
        }
    }

    // ------------------------------------------------------------------
    // Tests from the dataset
    // ------------------------------------------------------------------
    @Test
    void everyWordOfTheDatasetIsFoundWithItsCount() throws IOException {
        // Throws FileNotFoundException if data/count_1w.txt is missing
            // Which is the correct signal that the data folder isn't ready
        // Only read the first 10k lines so this test stays fast
        List<DatasetReader.WordCount> words =
                DatasetReader.readWordCounts(Path.of("data", "count_1w.txt"), 10_000);
        for (var wc : words) dict.add(wc.word(), wc.count());
        // every line should have produced exactly one entry
        assertEquals(words.size(), dict.size());
        for (var wc : words) {
            assertTrue(dict.contains(wc.word()), wc.word());
            assertEquals(wc.count(), dict.frequency(wc.word()), wc.word());
        }
    }

    // ------------------------------------------------------------------
    // TODO items
    // ------------------------------------------------------------------

    // Top-5 words starting with "pro" — expected values read from the dataset
    @Test
    void topFiveWordsStartingWithProFromDataset() {
        // No data file, no test - skip instead of failing
        assumeTrue(DATASET != null, "data/count_1w.txt missing — skipping");
        // Work out the right answer the slow way first, then compare
        List<String> expected = bruteForce(DATASET, "pro", 5);
        for (var wc : DATASET) dict.add(wc.word(), wc.count());
        assertEquals(expected, dict.complete("pro", 5));
    }

    // The complete() on the full dataset matches a brute-force oracle for prefixes of length 1, 2, 3 and 5, and several values of k
    @Test
    void completeOnFullDatasetMatchesBruteForce() {
        assumeTrue(DATASET != null, "data/count_1w.txt missing — skipping");
        for (var wc : DATASET) dict.add(wc.word(), wc.count());
        String[] prefixes = { "a", "th", "pro", "the", "compu" };   // lengths 1, 2, 3, 3, 5
        int[] ks = { 1, 5, 10, 25 };
        // Try every prefix against every k, and check against brute force
        for (String p : prefixes) for (int k : ks) {
            assertEquals(bruteForce(DATASET, p, k), dict.complete(p, k),
                    "prefix='" + p + "', k=" + k);
        }
    }

    // Words from the Gutenberg book that aren't in the word list
    @Test
    void bookWordsNotInWordList() throws IOException {
        assumeTrue(DATASET != null && BOOK != null, "data files missing — skipping");
        for (var wc : DATASET) dict.add(wc.word(), wc.count());
        List<String> bookWords = DatasetReader.readBookWords(BOOK);

        // Grab the first five book words the word list hasn't heard of
        List<String> notInList = new ArrayList<>();
        for (String w : bookWords) {
            if (!dict.contains(w)) { notInList.add(w); if (notInList.size() == 5) break; }
        }
        // Needs at least one, or the book/list are both empty
        assertFalse(notInList.isEmpty(), "expected some book-only words");
        for (String w : notInList) assertFalse(dict.contains(w), w);

        // The same words must be findable in the dictionary 
        Dictionary bookDict = createDictionary();
        for (String w : bookWords) bookDict.add(w, 1);
        for (String w : notInList) assertTrue(bookDict.contains(w), w);
    }

    // Randomized add/remove/frequency/complete() vs a TreeMap oracle.
    @Test
    void randomizedCompleteMatchesReference() {
        TreeMap<String, Long> ref = new TreeMap<>();
        Random rnd = new Random(7);
        // Build 3000 short p words with random counts
        for (int i = 0; i < 3_000; i++) {
            String w = "p" + (char) ('a' + rnd.nextInt(6)) + (char) ('a' + rnd.nextInt(6));
            long c = rnd.nextInt(100);
            dict.add(w, c); ref.put(w, c);
        }
        // Check complete() against the TreeMap oracle 50 times
        for (int t = 0; t < 50; t++) {
            String prefix = "p" + (char) ('a' + rnd.nextInt(6));
            int k = rnd.nextInt(10) + 1;
            assertEquals(bruteForceFromMap(ref, prefix, k), dict.complete(prefix, k),
                    "prefix='" + prefix + "', k=" + k);
        }
        // Interleave removes and re-check
        List<String> keys = new ArrayList<>(ref.keySet());
        for (int t = 0; t < 200; t++) {
            String w = keys.get(rnd.nextInt(keys.size()));
            // It was inserted above, so removing it will return true
            assertTrue(dict.remove(w));
            ref.remove(w);
            // After each remove, complete() should still agree with the oracle
            String prefix = w.substring(0, 1);
            assertEquals(bruteForceFromMap(ref, prefix, 5), dict.complete(prefix, 5),
                    "after removing " + w);
        }
    }

    // ------------------------------------------------------------------
    // Workloads
    // ------------------------------------------------------------------

    /** Workload "Misspellings": one-letter substitutions of real words. */
    @Test
    void misspellingsAreRejected() {
        assumeTrue(DATASET != null, "data/count_1w.txt missing — skipping");
        for (var wc : DATASET) dict.add(wc.word(), wc.count());
        Random rnd = new Random(2024);
        int tested = 0, oov = 0;
        // Keep going until tried 1000 real misspellings
        while (tested < 1_000) {
            String w = DATASET.get(rnd.nextInt(DATASET.size())).word();
            // Skip short words, there's nothing safe to mutate in the middle
            if (w.length() < 4) continue;
            int pos = 1 + rnd.nextInt(w.length() - 2);
            char orig = w.charAt(pos), repl;
            // Keep picking a random letter until it's actually different
            do { repl = (char) ('a' + rnd.nextInt(26)); } while (repl == orig);
            String m = w.substring(0, pos) + repl + w.substring(pos + 1);
            tested++;
            // If the typo happens to be a real word that's fine, only check the rest
            if (!dict.contains(m)) {
                assertEquals(0L, dict.frequency(m));
                oov++;
            }
        }
        // Over 90% of typos should be unknown words
        assertTrue(oov > 900, "expected >90% of misspellings to be OOV, got " + oov);
    }

    // Workload "Sizes": 10³, 10⁴, 10⁵ and (if loaded) full.
    @Test
    void dictionaryWorksAtTargetSizes() {
        assumeTrue(DATASET != null, "data/count_1w.txt missing — skipping");
        // Build a fresh dictionary at each target size and spot-check it
        for (int n : new int[] { 1_000, 10_000, 100_000 }) {
            Dictionary d = createDictionary();
            int limit = Math.min(n, DATASET.size());
            // The count_1w.txt is sorted by frequency, so the first n lines are the top n
            for (int i = 0; i < limit; i++) d.add(DATASET.get(i).word(), DATASET.get(i).count());
            assertEquals(limit, d.size());
            // Only check the first 100
            for (int i = 0; i < Math.min(100, limit); i++) {
                assertTrue(d.contains(DATASET.get(i).word()));
                assertEquals(DATASET.get(i).count(), d.frequency(DATASET.get(i).word()));
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    // Get the top-k - filter, sort, take k
    private static List<String> bruteForce(List<DatasetReader.WordCount> data, String prefix, int k) {
        List<DatasetReader.WordCount> m = new ArrayList<>();
        // Keep only the words with the right prefix
        for (var wc : data) if (wc.word().startsWith(prefix)) m.add(wc);
        // Rank by count descending, then word ascending
        m.sort((a, b) -> a.count() != b.count()
                ? Long.compare(b.count(), a.count())
                : a.word().compareTo(b.word()));
        List<String> out = new ArrayList<>();
        // Take at most k of them, in case there are fewer
        for (int i = 0; i < k && i < m.size(); i++) out.add(m.get(i).word());
        return out;
    }

    // Like bruteForce, but reading counts out of a TreeMap
    private static List<String> bruteForceFromMap(TreeMap<String, Long> map, String prefix, int k) {
        List<String> m = new ArrayList<>();
        for (var e : map.entrySet()) if (e.getKey().startsWith(prefix)) m.add(e.getKey());
        m.sort((a, b) -> {
            long ca = map.get(a), cb = map.get(b);
            return ca != cb ? Long.compare(cb, ca) : a.compareTo(b);
        });
        return new ArrayList<>(m.subList(0, Math.min(k, m.size())));
    }
}
