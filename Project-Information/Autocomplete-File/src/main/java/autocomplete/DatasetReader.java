package autocomplete;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the datasets of Application A.
 *
 * <p>Word list: {@code count_1w.txt} from https://norvig.com/ngrams/
 * (one line per word: {@code word<TAB>count}).
 *
 * <p>Books: Project Gutenberg "Plain Text UTF-8" downloads.
 *
 * <h2>Punctuation policy (documented for the report)</h2>
 * <ul>
 *   <li><b>Apostrophes</b> become a space: {@code "don't"} -> {@code ["don", "t"]}</li>
 *   <li><b>Hyphens</b> become a space: {@code "self-evident"} -> {@code ["self", "evident"]}</li>
 *   <li><b>Digits</b> become a space: {@code "Chapter 1"} -> {@code ["chapter"]}</li>
 *   <li>Any character outside {@code [a-z]} after lowercasing becomes a space</li>
 * </ul>
 * Rationale: {@code count_1w.txt} contains only lowercase, already-tokenised
 * words. Producing tokens that match that vocabulary keeps the two datasets
 * directly comparable. Splitting contractions rather than merging them avoids
 * conflating {@code "don't"} with the (different) word {@code "dont"}.
 */
public final class DatasetReader {

    /** One line of the word list. */
    public record WordCount(String word, long count) { }

    private DatasetReader() { }

    /**
     * Reads at most {@code maxLines} lines of the word-count file, in file order.
     * Malformed lines (missing TAB, non-numeric count) are skipped rather than
     * crashing the whole load. Pass {@code Integer.MAX_VALUE} to read everything.
     */
    public static List<WordCount> readWordCounts(Path file, int maxLines) throws IOException {
        //create "words" arraylist to store words and their frequencies
        List<WordCount> words = new ArrayList<>();
        //open the file
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int linesRead = 0;
            boolean first = true;
            //read until the maxLines are reached or the file ends
            while (linesRead < maxLines && (line = reader.readLine()) != null) {
                linesRead++;
                // strip UTF-8 BOM if present
                if (first) {
                    //the BOM is invisible but it would corrupt the first word, so chop it off
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    first = false;
                }
                //Split each line into the word and its appearance frequency
                String[] parts = line.split("\t");
                //skip the lines that doen't have both
                // malformed line
                if (parts.length != 2) continue;
                try {
                    //get the word and its frequency
                    String word = parts[0];
                    long count = Long.parseLong(parts[1]);
                    //Add the word and its freq count to the "words" arraylist
                    words.add(new WordCount(word, count));
                } catch (NumberFormatException ignored) {
                    // non-numeric count — skip
                }
            }
        }
        return words;
    }

    /**
     * Reads a Gutenberg book and returns its words, in order, lowercased and
     * with punctuation removed. Skips the license text before the line starting
     * with "*** START OF" and after the line starting with "*** END OF".
     *
     * <p>See the class javadoc for how apostrophes, hyphens and digits are handled.
     */
    public static List<String> readBookWords(Path file) throws IOException {
        //Create an arraylist to store the words
        List<String> words = new ArrayList<>();
        //opening the file
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            boolean readingBook = false;
            boolean first = true;
            //keep reading as long as there are more lines to read (of the story)
            while ((line = reader.readLine()) != null) {
                if (first) {
                    //the BOM is invisible but it would break the "*** START OF" check, so chop it off
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    first = false;
                }
                if (line.startsWith("*** START OF")) { readingBook = true; continue; }
                //stop reading when the story ends
                if (line.startsWith("*** END OF"))   { break; }
                //skip anything that isn't part of the actual story
                if (!readingBook) continue;

                // Convert letters to lowercase
                line = line.toLowerCase();
                //replace punctuationa nd digits with spaces
                // apostrophe/hyphen/digit policy
                line = line.replaceAll("[^a-z]+", " ");
                //splitting the line into words
                String[] parts = line.trim().split("\\s+");
                //add nonempty words to the list
                for (String w : parts) {
                    if (!w.isEmpty()) words.add(w);
                }
            }
        }
        return words;
    }
}
