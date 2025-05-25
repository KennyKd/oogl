package example;

import java.util.*;

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word, int frequency) {
        TrieNode current = root; // Starts at the root node of Trie

        for (char c : word.toCharArray()) {
            current = current.getChildren().computeIfAbsent(c, ch -> new TrieNode()); // Creates a new branch if "c" does not exist
        }
        current.setEndOfWord(true);
        current.setWordFrequency(frequency);
    }

    public List<String> getSuggestions(String prefix, int limit) {
        List<String> suggestions = new ArrayList<>();  // Gets a list of suggestions
        TrieNode prefixNode = findNode(prefix); // Goes to the node with a trace of the appropriate prefix

        /// Goes to each branch sourced from the prefix node.
        /// It traverses each branch until it reaches the end.
        /// Returns the word that has reached the end with their frequency.
        if (prefixNode != null) {
            PriorityQueue<WordFrequency> pq = new PriorityQueue<>(
                    Comparator.comparingInt(WordFrequency::getFrequency).reversed()
            );

            collectWordsWithFrequency(prefixNode, prefix, pq);

            int count = 0;
            while (!pq.isEmpty() && count < limit) {
                suggestions.add(pq.poll().getWord());
                count++;
            }
        }
        System.out.println(suggestions);

        return suggestions;
    }

    /// Searches for the node with a trace for the appropriate prefix.
    private TrieNode findNode(String prefix) {
        TrieNode current = root;

        for (char c : prefix.toCharArray()) {
            TrieNode node = current.getChildren().get(c);
            if (node == null) {
                return null;
            }
            current = node;
        }
        return current;
    }

    /// From the getSuggestions function, the input to this function is the series of nodes.
    /// Check each item and check whether they are ends of a word. If not, recursively keep going down, while appending each letter to the prefix.
    /// This slowly builds the word.
    /// If the end of the word is found, return its frequency.
    private void collectWordsWithFrequency(TrieNode node, String prefix, PriorityQueue<WordFrequency> pq) {
        if (node.isEndOfWord()) {
            pq.offer(new WordFrequency(prefix, node.getWordFrequency()));
        }

        for (char c : node.getChildren().keySet()) {
            collectWordsWithFrequency(node.getChildren().get(c), prefix + c, pq);
        }
    }

    /// Represents a node in the Trie...
    static class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>(); // Stores child nodes, keyed by characters.
        private boolean isEndOfWord; // Indicates whether this node marks the end of a word.
        private int wordFrequency = 0; // Stores the frequency of the word ending at this node.

        public Map<Character, TrieNode> getChildren() {
            return children;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public void setEndOfWord(boolean endOfWord) {
            isEndOfWord = endOfWord;
        }

        public void setWordFrequency(int frequency) {
            this.wordFrequency = frequency;
        }

        public int getWordFrequency() {
            return wordFrequency;
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