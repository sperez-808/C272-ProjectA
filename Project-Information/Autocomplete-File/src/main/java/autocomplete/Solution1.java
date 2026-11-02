package autocomplete;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Solution 1 for the dictionary service.
 * 
 *  Solution 1 uses a chianed hash map with seperate chaining
 *  to store words and their frequency counts.
 *  it uses adapted code from the course ChainHashMap code.
 *   
 *  
 *   add() = O(1)
 *   remove() = O(1)
 *   contains() = O(1)
 *   frequency() = O(1)
 *   complete() = O(n + m log m)
 *   size() = O(1)
 *   keySet() = O(C + n) where C is the table capacity and n is number of entries
 *  

 */
public class Solution1 implements Dictionary {
    // to store words nd their frequency counts
    private ChainHashMap<String, Long> dictionary;

    public Solution1() {
        // initialize your data structure.
       dictionary = new ChainHashMap<>(101);
    }

    @Override
    public void add(String word, long count) {
        //add a word or replace a word's existing frequency
        dictionary.put(word, count);
    }

    @Override
    public boolean remove(String word) {
        // removes word and return whether it existed
        return dictionary.remove(word) != null;
    }

    @Override
    public boolean contains(String word) {
       //check if word exists
       return dictionary.get(word) != null;
    }

    @Override
    public long frequency(String word) {
        //get the word's frequency
        Long count = dictionary.get(word);
        //return 0 if word does not exist
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public List<String> complete(String prefix, int k) {
        //create list for matching words
        List<String> matches = new ArrayList<>();
        // Return empty list if k is 0 or less
        if (k <= 0) {
            return matches;
        }
        //find all the words starting with a prefix
        for (String word : dictionary.keySet()) {
            if (word.startsWith(prefix)){
                matches.add(word);
            }
        }
        //sort by frequency (highest first)
        //break ties by listing words alphabetically
        matches.sort((a,b) -> {
            long countA = dictionary.get(a);
            long countB = dictionary.get(b);

            if (countA != countB) {
               return Long.compare(countB, countA); 
            }
            return a.compareTo(b);
        });
        //return at most k matching words
        return new ArrayList<>(matches.subList(0, Math.min(k, matches.size())));
    }

    @Override
    public int size() {
        //return number of stored words
        return dictionary.size();
    }

/* Returns a list of all keys stored in the hash map.
* Used to implement complete().
* adapted from the COMP 272 ChainHashMap .
*/

    @Override 
    public List <K> keySet () {
        //empty list to store all the keys
        List <K> keys = new ArrayLst<>();
        //loop through tthe hash table
        for (LinkedList<Entry<K, V>> bucket : table) {
            // add the keys of nonempty buckets to the list
            if (bucket != null) {
                for (Entry<K, V> e : bucket) {
                    keys.add(e.key);
                }
            }
        }
        return keys;
    }

}
