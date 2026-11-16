package autocomplete;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Chained hash map with separate chaining, adapted from the COMP 272
 * ChainHashMap course code. Automatically resizes at a 0.75 load factor.
 *
 * <p>Complexities: {@code put/get/remove} O(1) average, O(n) worst case;
 * {@code keySet} O(capacity + size); {@code size} O(1).
 */
public class ChainHashMap<K, V> {

    // Little holder class - one key, one value, nothing else
    private static class Entry<K, V> {
        final K key;
        V value;
        // Make new entry from a key and a value
        Entry(K key, V value) { this.key = key; this.value = value; }
    }

    // How many buckets to start with
    private static final int DEFAULT_CAPACITY = 16;
    // When the map gets 75% full, double the buckets
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    // The actual table: one LinkedList (bucket) per slot
    private LinkedList<Entry<K, V>>[] table;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public ChainHashMap(int initialCapacity) {
        // Never let the capacity be 0 or negative
        capacity = Math.max(1, initialCapacity);
        // Make the bucket array (the cast is just to satisfy generics)
        table = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
        size = 0;
    }

    // No capacity given, so use default one
    public ChainHashMap() { this(DEFAULT_CAPACITY); }

    // Turn a key into a bucket index - keeps it in [0, capacity)
    private int hash(K key) { return Math.floorMod(key.hashCode(), capacity); }

    public V put(K key, V value) {
        // Find the bucket for this key
        int i = hash(key);
        // Make the bucket list if it doesn't exist yet
        if (table[i] == null) table[i] = new LinkedList<>();
        // Walk the bucket looking for the same key
        for (Entry<K, V> e : table[i]) {
            // Case 1: Found, so overwrite the value and hand back the old one
            if (e.key.equals(key)) { V old = e.value; e.value = value; return old; }
        }
        // Case 2: Not found, so add a brand new entry and bump the size
        table[i].add(new Entry<>(key, value));
        size++;
        // If too full, double the buckets
        if ((double) size / capacity > LOAD_FACTOR) resize();
        // If nothing was overwritten return null
        return null;
    }

    public V get(K key) {
        // Find the bucket for this key
        int i = hash(key);
        // Empty bucket means the key isn't here
        if (table[i] == null) return null;
        // Walk the bucket until key is found
        for (Entry<K, V> e : table[i]) if (e.key.equals(key)) return e.value;
        // If not found, return null
        return null;
    }

    public V remove(K key) {
        // Find the bucket for this key
        int i = hash(key);
        // Empty bucket means there's nothing to remove
        if (table[i] == null) return null;
        // Use an iterator to be able to remove while walking
        var it = table[i].iterator();
        while (it.hasNext()) {
            Entry<K, V> e = it.next();
            // Found it, so remove it, shrink the size, and return the old value
            if (e.key.equals(key)) { V old = e.value; it.remove(); size--; return old; }
        }
        // If never found, null
        return null;
    }

    // How many key-value pairs are in the map
    public int size() { return size; }

    public List<K> keySet() {
        // Make a list big enough for every key
        List<K> keys = new ArrayList<>(size);
        // Walk every bucket and copy out the keys
        for (var bucket : table) {
            if (bucket != null) for (Entry<K, V> e : bucket) keys.add(e.key);
        }
        return keys;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        // Hold on to the old table to be able to rehash everything
        var old = table;
        // Double the number of buckets
        capacity *= 2;
        // Make a fresh empty table
        table = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
        // Reset size because put() will count everything again
        size = 0;
        // Re-put every entry so it lands in its new bucket!
        for (var bucket : old) {
            if (bucket != null) for (Entry<K, V> e : bucket) put(e.key, e.value);
        }
    }
}
