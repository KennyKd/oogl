package example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TernarySearchTree {
    private TSTNode root;

    public void insert(String word, int frequency) {
        root = insert(root, word, 0, frequency); // Index 0 is a default value.
    }

    /// Recursive helper for insertion...
    private TSTNode insert(TSTNode node, String word, int index, int frequency) {
        if (word.isEmpty()) return node; // Skips over empty words, if any.

        char c = word.charAt(index); // Retrieves the letter from the word at a specified index.

        if (node == null) { // If the current node is not populated, then add the retrieved letter.
            node = new TSTNode(c);
        }

        if (c < node.data) { // If the current letter is smaller than the node's letter.
            node.left = insert(node.left, word, index, frequency);
        } else if (c > node.data) { // If the current letter is bigger than the node's letter.
            node.right = insert(node.right, word, index, frequency);
        } else { // If the letters are the same.
            /// This part usually occurs when:
            /// 1. The word is new, populating the middle branch.
            /// 2. A new word has the same prefix as another word already inserted.
            if (index < word.length() - 1) {
                node.middle = insert(node.middle, word, index + 1, frequency); // If the word is not finished.
            } else {
                node.isEndOfWord = true;
                node.wordFrequency = frequency;
            }
        }
        return node;
    }

    public List<String> getSuggestions(String prefix, int limit) {
        List<String> suggestions = new ArrayList<>();

        if (prefix.isEmpty()) { // Error handling for empty prefixes.
            collectWords(root, "", suggestions, limit);
            return suggestions;
        }

        TSTNode lastNode = searchPrefix(root, prefix, 0);

        if (lastNode == null) {
            return suggestions;
        }

        if (lastNode.isEndOfWord) {
            suggestions.add(prefix);
        }

        collectWords(lastNode.middle, prefix, suggestions, limit);

        return suggestions;
    }


    /// Searches for the node with a trace for the appropriate prefix.
    private TSTNode searchPrefix(TSTNode node, String prefix, int index) {
        if (node == null || prefix.isEmpty()) {
            return null;
        }

        char c = prefix.charAt(index);

        if (c < node.data) {
            return searchPrefix(node.left, prefix, index);
        } else if (c > node.data) {
            return searchPrefix(node.right, prefix, index);
        } else {
            if (index == prefix.length() - 1) {
                return node;
            }
            return searchPrefix(node.middle, prefix, index + 1);
        }
    }

    private void collectWords(TSTNode node, String prefix, List<String> suggestions, int limit) {
        if (node == null || suggestions.size() >= limit) {
            return;
        }

        PriorityQueue<WordFrequency> pq = new PriorityQueue<>(Comparator.comparingInt(WordFrequency::getFrequency).reversed());

        collectWordsWithFrequency(node, prefix, pq, limit);

        while (!pq.isEmpty() && suggestions.size() < limit) {
            suggestions.add(pq.poll().getWord());
        }
    }

    /// Helper recursive function.
    private void collectWordsWithFrequency(TSTNode node, String prefix, PriorityQueue<WordFrequency> pq, int limit) {
        if (node == null || pq.size() >= limit) {
            return;
        }

        collectWordsWithFrequency(node.left, prefix, pq, limit);

        String word = prefix + node.data;
        if (node.isEndOfWord) {
            pq.offer(new WordFrequency(word, node.wordFrequency));
        }

        collectWordsWithFrequency(node.middle, word, pq, limit);
        collectWordsWithFrequency(node.right, prefix, pq, limit);
    }

    /// Represents a node in the TST...
    static class TSTNode {
        char data; // Stores single characters.
        boolean isEndOfWord; // Determines whether the node marks the end of a word.
        TSTNode left, middle, right; // Every node has 3 children.
        int wordFrequency = 0;

        TSTNode(char data) {
            this.data = data;
            this.isEndOfWord = false;
            this.left = this.middle = this.right = null;
        }
    }

    /// Represents a word's frequency...
    static class WordFrequency {
        private final String word;
        private final int frequency;

        public WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        public String getWord() {
            return word;
        }

        public int getFrequency() {
            return frequency;
        }
    }
}
