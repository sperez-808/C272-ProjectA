
package autocomplete;

import java.util.List;
import java.util.ArrayList;

/**
 * Solution 2 for the dictionary service.
 *
 * uses a self-balancing AVL binary search tree,
 * adapted from the COMP 272 BinarySearchTree and
 * AVLTree course codes.

 * expected time complexities:
 * add(): O(log n)
 * remove(): O(log n)
 * contains(): O(log n)
 * frequency(): O(log n)
 * complete(): O(log n + m * L)
 * size(): O(1)
 *
 * m is the number of matching words examined,
 * limited to k, and L is the cost of string checks.
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
        Node left;
        Node right;
        Node parent;
        int height = 0;

        Node(String word, long count, Node parent) {
            this.word = word;
            this.count = count;
            this.parent = parent;
        }
    }

    // Constructor for an empty dictionary
    public Solution2() {
        root = null;
        size = 0;
    }

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
        Node current = root;
        Node parent = null;
        // searching until an empty position is found
        while (current != null) {
            // save the current node as the parent
            parent = current;
            // Ccompare the new word alphabetically
            // with the word stored in the current node
            int comparison = word.compareTo(current.word);
            // check if the word already exists
            if (comparison == 0) {
                // update the existing word's frequency
                current.count = count;
                return;
            }
            // if the new word comes alphabetically before
            // the current word then move to the left child
            else if (comparison < 0) {
                current = current.left;

            // otherwise move to the right child
            } else {
                current = current.right;
            }
        }
        // creating a new node with the word, count,
        // and parent found during the search
        Node inserted = new Node(word, count, parent);
        // Determine whether the new node belongs left or right of parent
        if (word.compareTo(parent.word) < 0) {
            parent.left = inserted;
        } else {
            parent.right = inserted;
        }
        // increase size of stored wordes
        size++;
        // restore AVL balance starting from the inserted node's parent.
        rebalanceUpward(parent);
    }
    
    //Search for a word using BST ordering
    private Node findNode(String word) {
        // begin search from the root
        Node current = root;
        // search until the word is found or the search reaches an empty node
        while (current != null) {
            // compare requested word with word stored in the current node
            int comparison = word.compareTo(current.word);
            //if the words match return the node
            if (comparison == 0) {
                return current;
            // if the requested word is smaller, continue searching the left subtree
            } else if (comparison < 0) {
                current = current.left;
            //else, search the right
            } else {
                current = current.right;
            }
        }
        // return null if the word isn't fount
        return null;
    }

    @Override
    public boolean contains(String word) {
        // search for the word and return true ifa matching node exists
        return findNode(word) != null;
    }

    @Override
    public long frequency(String word) {
        //search for the node containing the word
        Node found = findNode(word);
        //if the word does not exist, return zero
        if (found == null) {
            return 0;
        }
        //Return the frequency stored in the node.
        return found.count;
    }

    @Override
    public boolean remove(String word) {
        //search for the node containing the word
        // If the word does not exist, return false
        Node node = findNode(word);
        if (node == null) {
            return false;
        }
        // if the node has two children case
        if (node.left != null && node.right != null) {
            Node successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.word = successor.word;
            node.count = successor.count;
            node = successor;
        }
        //reference for the remaining child
        Node child;
        //check whether the node has a left child
        if (node.left != null) {
            //use left child as the replacement
            child = node.left;
        } else {
            //Otherwise use the right child
            child = node.right;
        }
        //save parent of the node being removed
        Node parent = node.parent;
        //if replacement child exists connect it to the deleted node's parent
        if (child != null) {
            child.parent = parent;
        }
        //if the deleted node is the root replace ti with its child
        if (parent == null) {
            root = child;
        //If node is parent's left child, replace that left reference
        } else if (parent.left == node) {
            parent.left = child;
        // else replace the parent's right child.\
        } else {
            parent.right = child;
        }
        // decrease num of stored words.
        size--;
        //restore AVL balance starting at parent of removed node
        rebalanceUpward(parent);
        return true;
    }

    @Override
    public List<String> complete(String prefix, int k) {
        // create an mpty List for matching words.
        List<String> matches = new ArrayList<>();
        //If requested number of suggestions is zero or negative return an empty list.
        if (k <= 0) {
            return matches;
        }
        //search the AVL tree for words beginning with the prefix
        completeInOrder(root, prefix, k, matches);
        //return matching words
        return matches;
    }
    //use inorder traversal to find matching words
    //in alphabetical order
    private void completeInOrder(
            Node node,
            String prefix,
            int k,
            List<String> matches) {
        // Stop if the node does not exist or
        //enough matching words have been found
        if (node == null || matches.size() >= k) {
            return;
        }
        //compare the current word with the prefix
        int comparison = node.word.compareTo(prefix);
        // if current is alphabetically greater than or equal to the prefix,
        // search the left subtree for possible matches
        if (comparison >= 0) {
            completeInOrder(node.left, prefix, k, matches);
        }
        //Stop if the requested number of matching words has been collected
        if (matches.size() >= k) {
            return;
        }
        //check whether the current word begins with the requested prefix
        if (node.word.startsWith(prefix)) {
            //add the matching word to the list
            matches.add(node.word);
        }
        //search the right subtree for additional words that begin with the prefix
        completeInOrder(node.right, prefix, k, matches);
    }

    @Override
    public int size() {
        //Return the number of stored words
        return size;
    }

    //adapted from BinarySearchTree.
    private int height(Node node) {
        //return -1 for an empty subtree, otherwise return the stored node height
        return node == null ? -1 : node.height;
    }

    // Ddapted from BinarySearchTree.
    private void updateHeight(Node node) {
        // Find the greater height of the two children,add one for the current node
        node.height = 1 + Math.max(
            height(node.left),
            height(node.right)
        );
    }

    //Adapted from AVLTree
    private int balanceFactor(Node node) {
        //subtract the right subtree height from the left subtree height.
        return height(node.left) - height(node.right);
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
            int balance = balanceFactor(node);
            // check if the left subtree is more than one level taller
            if (balance > 1) {
                // perform a left rotation
                if (balanceFactor(node.left) < 0) {
                    rotateLeft(node.left);
                }
                // rotate right to restore balance.
                subtreeRoot = rotateRight(node);
            // check wehther the right subtree is more than one level taller
            } else if (balance < -1) {
                //right rotation first
                if (balanceFactor(node.right) > 0) {
                    rotateRight(node.right);
                }
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
        newRoot.left = oldRoot;
        //update the old root's parent ref
        oldRoot.parent = newRoot;
        //Move middle subtree to the right side of the old root
        oldRoot.right = middle;
        //if the middle subtree exists update its parent reference
        if (middle != null) {
            middle.parent = oldRoot;
        }
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
        newRoot.right = oldRoot;
        //update the old root's parent reference
        oldRoot.parent = newRoot;
        //move the middle subtree to left side of the old root
        oldRoot.left = middle;
        //if middle subtree exists then update its parent reference
        if (middle != null) {
            middle.parent = oldRoot;
        }
        //update the old root's height
        updateHeight(oldRoot);
        //update the new root's height
        updateHeight(newRoot);
        //return the new root of the subtree
        return newRoot;
    }

    //adapted from AVLTree
    private void replaceAtParent(
            Node oldRoot, Node newRoot) {
        //save the parent of the original subtree root
        Node parent = oldRoot.parent;
        //assign the original parent to the new root
        newRoot.parent = parent;
        //if the original subtree was the entire tree thenupdate the tree's root reference.
        if (parent == null) {
            root = newRoot;
        //if old root was a left child,
        //connect the new root as the left child
        } else if (parent.left == oldRoot) {
            parent.left = newRoot;
        //else connect the new root as the parent's right child
        } else {
            parent.right = newRoot;
        }
    }
}
