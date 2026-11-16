package autocomplete;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Solution 2 — self-balancing AVL binary search tree.</b>
 *
 * <p>Adapted from the COMP 272 {@code BinarySearchTree} and {@code AVLTree}
 * course code. Each node stores a word, its count, parent/left/right links and
 * its subtree height. All four rotation cases are implemented; rebalancing is
 * performed upward from the modified node on every {@code add}/{@code remove}.
 *
 * <p><b>Time complexities:</b>
 * <ul>
 *   <li>{@code add} / {@code remove} / {@code contains} / {@code frequency} — O(log n)</li>
 *   <li>{@code complete(prefix, k)} — O(log n + m + m log m): O(log n) to
 *       locate the first prefix match, then m matching nodes are collected
 *       (subtrees outside the prefix range are pruned), then m log m to sort
 *       by (count desc, word asc)</li>
 *   <li>{@code size} — O(1)</li>
 * </ul>
 *
 * <p><b>Trade-off:</b> slower writes than a hash table, but the BST stores the
 * keys in order, so {@code complete} can prune entire subtrees that cannot
 * contain a prefix match. This is what makes short-prefix queries fast.
 */
public class Solution2 implements Dictionary {

    // Store the root node of the AVL tree
    private Node root;
    // stores the number of words in the dictionary
    private int size;

    // adapted from BinarySearchTree.Node
    // create a class to represent ecah tree node
    private static class Node {
        // word associated with node
        String word;
        // frequency count of the word.
        long count;
        Node left, right, parent;
        int height = 0;
        Node(String word, long count, Node parent) {
            // Store the word, the count, and a link to the parent
            this.word = word; this.count = count; this.parent = parent;
        }
    }

    // Constructor for an empty dictionary
    public Solution2() { root = null; size = 0; }

    // ------------------------------------------------------------------ add
    @Override
    public void add(String word, long count) {
        // check whether the tree is empty
        if (root == null) {
            // create the first node as the root
            root = new Node(word, count, null);
            size++;
            return;
        }
        // start searching for the insertion position at the root
        Node cur = root, parent = null;
        // searching until an empty position is found
        while (cur != null) {
            // save the current node as the parent
            parent = cur;
            // Ccompare the new word alphabetically
            // with the word stored in the current node
            int cmp = word.compareTo(cur.word);
            // check if the word already exists
            if (cmp == 0) {
                // update the existing word's frequency
                cur.count = count;
                return;
            }
            // if the new word comes alphabetically before
            // the current word then move to the left child,
            // otherwise move to the right child
            cur = cmp < 0 ? cur.left : cur.right;
        }
        // creating a new node with the word, count,
        // and parent found during the search
        Node inserted = new Node(word, count, parent);
        // Determine whether the new node belongs left or right of parent
        if (word.compareTo(parent.word) < 0) parent.left = inserted;
        else parent.right = inserted;
        // increase size of stored wordes
        size++;
        // restore AVL balance starting from the inserted node's parent.
        rebalanceUpward(parent);
    }

    // ------------------------------------------------------------------ lookup
    //Search for a word using BST ordering
    private Node findNode(String word) {
        // begin search from the root
        Node cur = root;
        // search until the word is found or the search reaches an empty node
        while (cur != null) {
            // compare requested word with word stored in the current node
            int cmp = word.compareTo(cur.word);
            //if the words match return the node
            if (cmp == 0) return cur;
            // if the requested word is smaller, continue searching the left subtree
            //else, search the right
            cur = cmp < 0 ? cur.left : cur.right;
        }
        // return null if the word isn't fount
        return null;
    }

    @Override public boolean contains(String word) {
        // search for the word and return true ifa matching node exists
        return findNode(word) != null;
    }

    @Override public long frequency(String word) {
        //search for the node containing the word
        Node n = findNode(word);
        //if the word does not exist, return zero,
        //otherwise return the frequency stored in the node.
        return n == null ? 0 : n.count;
    }

    // ------------------------------------------------------------------ remove
    @Override
    public boolean remove(String word) {
        //search for the node containing the word
        // If the word does not exist, return false
        Node node = findNode(word);
        if (node == null) return false;
        // if the node has two children case
        if (node.left != null && node.right != null) {
            // Find the leftmost node in the right subtree - the successor
            Node succ = node.right;
            while (succ.left != null) succ = succ.left;
            // copy the successor's data here, then delete the successor instead
            node.word = succ.word;
            node.count = succ.count;
            node = succ;
        }
        //reference for the remaining child
        Node child = (node.left != null) ? node.left : node.right;
        //save parent of the node being removed
        Node parent = node.parent;
        //if replacement child exists connect it to the deleted node's parent
        if (child != null) child.parent = parent;
        //if the deleted node is the root replace ti with its child
        if (parent == null) root = child;
        //If node is parent's left child, replace that left reference
        else if (parent.left == node) parent.left = child;
        // else replace the parent's right child.
        else parent.right = child;
        // decrease num of stored words.
        size--;
        //restore AVL balance starting at parent of removed node
        rebalanceUpward(parent);
        return true;
    }

    // ------------------------------------------------------------------ complete
    @Override
    public List<String> complete(String prefix, int k) {
        //If requested number of suggestions is zero or negative return an empty list.
        if (k <= 0) return new ArrayList<>();
        // create an mpty List for matching words.
        List<Node> matches = new ArrayList<>();
        // Upper bound is one past every string with the prefix
        String upper = upperBound(prefix);
        // Collect every match
            // Pruning subtrees that can't contain one
        collect(root, prefix, upper, matches);
        // Sort by frequency (highest first) breaking alphabetic organization
        matches.sort((a, b) ->
                a.count != b.count
                        ? Long.compare(b.count, a.count)
                        : a.word.compareTo(b.word));
        // Keep only the first k words
        List<String> out = new ArrayList<>(Math.min(k, matches.size()));
        for (int i = 0; i < k && i < matches.size(); i++) out.add(matches.get(i).word);
        //return matching words
        return out;
    }

    /**
     * Returns the smallest string that is greater than every string starting
     * with {@code prefix}; {@code null} means "no upper bound" (prefix is all
     * {@link Character#MAX_VALUE} characters, which cannot occur for real words).
     */
    private static String upperBound(String prefix) {
        // Empty prefix means no upper bound
        if (prefix.isEmpty()) return null;
        char last = prefix.charAt(prefix.length() - 1);
        // If the last character is already maxed out, drop it and try again
        if (last == Character.MAX_VALUE)
            return upperBound(prefix.substring(0, prefix.length() - 1));
        // Bump the last character up by one to get just past the prefix
        return prefix.substring(0, prefix.length() - 1) + (char) (last + 1);
    }

    //use inorder traversal to find matching words
    private void collect(Node node, String prefix, String upper, List<Node> out) {
        // Stop if the node does not exist
        if (node == null) return;
        // if this node sorts at or after the prefix, the left subtree may hold matches
        if (node.word.compareTo(prefix) > 0) collect(node.left, prefix, upper, out);
        //check whether the current word begins with the requested prefix
        if (node.word.startsWith(prefix)) out.add(node);
        // Only search right while below the upper bound
        if (upper == null || node.word.compareTo(upper) < 0)
            collect(node.right, prefix, upper, out);
    }

    @Override public int size() {
        //Return the number of stored words
        return size;
    }

    // ------------------------------------------------------------------ AVL
    //adapted from BinarySearchTree.
    private int height(Node n) {
        //return -1 for an empty subtree, otherwise return the stored node height
        return n == null ? -1 : n.height;
    }

    // Ddapted from BinarySearchTree.
    private void updateHeight(Node n) {
        // Find the greater height of the two children,add one for the current node
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    //Adapted from AVLTree
    private int balanceFactor(Node n) {
        //subtract the right subtree height from the left subtree height.
        return height(n.left) - height(n.right);
    }

    //adapted from AVLTree
    private void rebalanceUpward(Node node) {
        //continue checking nodes moving upward toward the root
        while (node != null) {
            //calculate the current node's height.
            updateHeight(node);
            //Store the curent subtree root
            Node subtreeRoot = node;
            //Calculate the node's balance factor
            int bf = balanceFactor(node);
            // check if the left subtree is more than one level taller
            if (bf > 1) {
                // perform a left rotation
                if (balanceFactor(node.left) < 0) rotateLeft(node.left);
                // rotate right to restore balance.
                subtreeRoot = rotateRight(node);
            // check whether the right subtree is more than one level taller
            } else if (bf < -1) {
                //right rotation first
                if (balanceFactor(node.right) > 0) rotateRight(node.right);
                // Rotate left to restore balance
                subtreeRoot = rotateLeft(node);
            }
            //continue checking the parent
            node = subtreeRoot.parent;
        }
    }

    //adapted from AVLTree
    private Node rotateLeft(Node oldRoot) {
        //save right child as the new subtree root
        Node newRoot = oldRoot.right;
        //Save the new root's left subtree
        Node middle = newRoot.left;
        //connect new root to the original parent
        replaceAtParent(oldRoot, newRoot);
        // make old root the left child of the new root
        newRoot.left = oldRoot; oldRoot.parent = newRoot;
        //update the old root's parent ref
        oldRoot.parent = newRoot;
        //Move middle subtree to the right side of the old root
        oldRoot.right = middle;
        //if the middle subtree exists update its parent reference
        if (middle != null) middle.parent = oldRoot;
        //update the oldd root's height
        updateHeight(oldRoot);
        //update the new root's height second
        updateHeight(newRoot);
        //return the new root of the subtree
        return newRoot;
    }

    // Adapted from AVLTree
    private Node rotateRight(Node oldRoot) {
        //save the left child as the new subtree root
        Node newRoot = oldRoot.left;
        //save the new root's right subtree
        Node middle = newRoot.right;
        // connect the new root to the og parent
        replaceAtParent(oldRoot, newRoot);
        // Make the old root the right child of new root
        newRoot.right = oldRoot; oldRoot.parent = newRoot;
        //update the old root's parent reference
        oldRoot.parent = newRoot;
        //move the middle subtree to left side of the old root
        oldRoot.left = middle;
        //if middle subtree exists then update its parent reference
        if (middle != null) middle.parent = oldRoot;
        //update the old root's height
        updateHeight(oldRoot);
        //update the new root's height
        updateHeight(newRoot);
        //return the new root of the subtree
        return newRoot;
    }

    //adapted from AVLTree
    private void replaceAtParent(Node oldRoot, Node newRoot) {
        //save the parent of the original subtree root
        Node p = oldRoot.parent;
        //assign the original parent to the new root
        newRoot.parent = p;
        //if the original subtree was the entire tree thenupdate the tree's root reference.
        if (p == null) root = newRoot;
        //if old root was a left child,
        //connect the new root as the left child
        else if (p.left == oldRoot) p.left = newRoot;
        //else connect the new root as the parent's right child
        else p.right = newRoot;
    }
}
