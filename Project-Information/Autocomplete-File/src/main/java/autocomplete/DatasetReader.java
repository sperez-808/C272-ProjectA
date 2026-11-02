package autocomplete;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.ArrayList;
/**
 * Reads the datasets of Application A. You must write the two methods below.
 *
 * <p>Datasets (plain text files):
 * <ul>
 *   <li>count_1w.txt from https://norvig.com/ngrams/ — one line per word:
 *       the word, a TAB character, and its count. Example line: {@code the<TAB>23135851162}</li>
 *   <li>Books from https://www.gutenberg.org/ — download the "Plain Text UTF-8" version.</li>
 * </ul>
 *
 * <p>Hints:
 * <ul>
 *   <li>{@code java.nio.file.Files.newBufferedReader(file)} gives a reader;
 *       call {@code readLine()} until it returns null.</li>
 *   <li>{@code line.split("\t")} splits a line at the TAB;
 *       {@code Long.parseLong(text)} converts the count.</li>
 * </ul>
 */
public final class DatasetReader {

    /** One line of the word list. (A "record" is a small class that only holds data.) */
    public record WordCount(String word, long count) { }

    private DatasetReader() { }   // no objects: only static methods

    /**
     * Reads at most maxLines lines of the word-count file, in file order.
     * Use maxLines = Integer.MAX_VALUE to read the whole file.
     */
    public static List<WordCount> readWordCounts(Path file, int maxLines) throws IOException {
        //create "words" arraylist to store words and their frequencies
        List<WordCount> words = new ArrayList<>();
        //open the file
        try (BufferedReader reader = Files.newBufferedReader(file)){
            String line;
            int linesRead = 0;

            //read until the maxLines are reached or the file ends
            while (linesRead < maxLines && (line = reader.readLine()) != null) {
                linesRead++;
                //Split each line into the word and its appearance frequency
                String[] parts = line.split("\t");
                //skip the lines that doen't have both
                if (parts.length != 2 ) {
                    continue;
                }
                //get the word and its frequency
                String word = parts[0];
                long count = Long.parseLong(parts[1]);
                //Add the word and its freq count to the "words" arraylist
                words.add(new WordCount(word, count));
            }
        }
        return words;
        
    }

    /**
     * Reads a Gutenberg book and returns its words, in order, converted to lowercase,
     * with punctuation removed. Skip the license text before the line starting with
     * "*** START OF" and after the line starting with "*** END OF".
     * Document in your report how you handle apostrophes, hyphens and digits.
     */
    public static List<String> readBookWords(Path file) throws IOException {
        //Create an arraylist to store the words
        List<String> words = new ArrayList<>();
        //opening the file
        try (BufferedReader reader = Files.newBufferedReader(file)){
            String line;
            boolean readingBook = false;
            //keep reading as long as there are more lines to read (of the story)
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("*** START OF")) {
                    readingBook = true;
                    continue;
                }
                //stop reading when the story ends
                if (line.startsWith("*** END OF")){
                    break;
                }
                //skip anything that isn't part of the actual story
                if (! readingBook){
                    continue;
                }
                // Convert letters to lowercase
                line = line.toLowerCase();
                //replace punctuationa nd digits with spaces
                line = line.replaceAll("[^a-z]+", " ");
                //splitting the line into words
                String[] parts = line.trim().split("\\s+");
                //add nonempty words to the list
                for (String word : parts) {
                    if (!word.isEmpty()) {
                        words.add(word);
                    }
                }
            
            }
        }
        return words;
    }
}
