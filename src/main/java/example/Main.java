package example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main {
    private final Trie trie;
    private final TernarySearchTree tst;
    private final Map<String, Integer> wordFrequencies;

    public static void main(String[] args) {
        try {
            Main autocomplete = new Main("filtered_words.csv");

            // Test the autocomplete system
            String input = "prog";

            System.out.println("Suggestions for " + input + " using Trie: ");
            long startTime = System.nanoTime();
            List<String> trieSuggestions = autocomplete.suggestWithTrie(input, 5);
            long endTime = System.nanoTime();
            trieSuggestions.forEach(System.out::println);
            System.out.println("Time taken by Trie: " + (endTime - startTime) + " ns");

            System.out.println("\nSuggestions for " + input + " using TST: ");
            startTime = System.nanoTime();
            List<String> tstSuggestions = autocomplete.suggestWithTST(input, 5);
            endTime = System.nanoTime();
            tstSuggestions.forEach(System.out::println);
            System.out.println("Time taken by TST: " + (endTime - startTime) + " ns");

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public Main(String dictionaryFile) throws IOException, CsvException {
        this.trie = new Trie();
        this.tst = new TernarySearchTree();
        this.wordFrequencies = new HashMap<>();

        long startTime = System.nanoTime();
        loadDictionary(dictionaryFile);
        long endTime = System.nanoTime();
        System.out.println("Time taken to load dictionary: " + (endTime - startTime) + " ns");
    }

    private void loadDictionary(String dictionaryFile) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(dictionaryFile))) {
            List<String[]> rows = reader.readAll();

            for (int i = 2; i < rows.size(); i++) { // Skip the header row (word/count)
                String[] columns = rows.get(i);
                if (columns.length < 2) {
                    continue; // Skip malformed rows
                }

                String word = columns[0].trim().toLowerCase();
                int count = Integer.parseInt(columns[1].trim());

                if (!word.isEmpty()) {
                    // Insert each word and their frequency into Trie and TST
                    trie.insert(word, count);
                    tst.insert(word, count);

                    // Add to wordFrequencies
                    wordFrequencies.put(word, count);
                }
            }
        }
    }

    public List<String> suggestWithTrie(String prefix, int limit) {
        return trie.getSuggestions(prefix.toLowerCase(), limit);
    }

    public List<String> suggestWithTST(String prefix, int limit) {
        return tst.getSuggestions(prefix.toLowerCase(), limit);
    }
}