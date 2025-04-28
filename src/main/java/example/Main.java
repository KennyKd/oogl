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
            // Track memory before initialization
            System.out.println("===== STARTING MEMORY TRACKING =====");
            Runtime runtime = Runtime.getRuntime();
            System.gc(); // Request garbage collection to get more accurate memory readings
            long beforeInitMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Initial memory usage: " + formatMemorySize(beforeInitMemory));

            Main autocomplete = new Main("filtered_words.csv");

            // Print detailed memory stats after initialization
            autocomplete.printDetailedMemoryStats();

            // Test the autocomplete system
            String input = "prog";

            // Memory before Trie suggestion
            System.gc();
            long beforeTrieSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();

            System.out.println("\nSuggestions for " + input + " using Trie: ");
            long startTime = System.nanoTime();
            List<String> trieSuggestions = autocomplete.suggestWithTrie(input, 5);
            long endTime = System.nanoTime();
            trieSuggestions.forEach(System.out::println);
            System.out.println("Time taken by Trie: " + (endTime - startTime) + " ns");

            // Memory after Trie suggestion
            System.gc();
            long afterTrieSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Memory used during Trie suggestion: " +
                    formatMemorySize(afterTrieSuggestionMemory - beforeTrieSuggestionMemory));

            // Memory before TST suggestion
            System.gc();
            long beforeTSTSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();

            System.out.println("\nSuggestions for " + input + " using TST: ");
            startTime = System.nanoTime();
            List<String> tstSuggestions = autocomplete.suggestWithTST(input, 5);
            endTime = System.nanoTime();
            tstSuggestions.forEach(System.out::println);
            System.out.println("Time taken by TST: " + (endTime - startTime) + " ns");

            // Memory after TST suggestion
            System.gc();
            long afterTSTSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Memory used during TST suggestion: " +
                    formatMemorySize(afterTSTSuggestionMemory - beforeTSTSuggestionMemory));

            // Print final memory stats
            autocomplete.printDetailedMemoryStats();

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public Main(String dictionaryFile) throws IOException, CsvException {
        // Track memory before creating data structures
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Request garbage collection
        long beforeTrieMemory = runtime.totalMemory() - runtime.freeMemory();

        this.trie = new Trie();
        this.wordFrequencies = new HashMap<>();

        // Load only Trie first to measure its memory usage
        System.out.println("\n===== LOADING TRIE =====");
        long startTimeTrie = System.nanoTime();
        loadTrieDictionary(dictionaryFile);
        long endTimeTrie = System.nanoTime();

        System.gc(); // Request garbage collection
        long afterTrieMemory = runtime.totalMemory() - runtime.freeMemory();
        long trieMemoryUsage = afterTrieMemory - beforeTrieMemory;

        System.out.println("Time taken to load Trie: " + (endTimeTrie - startTimeTrie) + " ns");
        System.out.println("Trie Memory Usage: " + formatMemorySize(trieMemoryUsage));

        // Now create and load TST
        System.out.println("\n===== LOADING TST =====");
        System.gc(); // Request garbage collection
        long beforeTSTMemory = runtime.totalMemory() - runtime.freeMemory();

        this.tst = new TernarySearchTree();
        long startTimeTST = System.nanoTime();
        loadTSTDictionary(dictionaryFile);
        long endTimeTST = System.nanoTime();

        System.gc(); // Request garbage collection
        long afterTSTMemory = runtime.totalMemory() - runtime.freeMemory();
        long tstMemoryUsage = afterTSTMemory - beforeTSTMemory;

        System.out.println("Time taken to load TST: " + (endTimeTST - startTimeTST) + " ns");
        System.out.println("TST Memory Usage: " + formatMemorySize(tstMemoryUsage));

        // Compare memory usage
        System.out.println("\n===== MEMORY COMPARISON =====");
        System.out.println("Trie vs TST memory difference: " +
                formatMemorySize(Math.abs(trieMemoryUsage - tstMemoryUsage)));
        if (trieMemoryUsage > tstMemoryUsage) {
            System.out.println("Trie uses more memory by " +
                    String.format("%.2f%%", (double)(trieMemoryUsage - tstMemoryUsage) * 100 / tstMemoryUsage));
        } else if (tstMemoryUsage > trieMemoryUsage) {
            System.out.println("TST uses more memory by " +
                    String.format("%.2f%%", (double)(tstMemoryUsage - trieMemoryUsage) * 100 / trieMemoryUsage));
        } else {
            System.out.println("Both data structures use the same amount of memory.");
        }
    }

    private static String formatMemorySize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), pre);
    }

    private void loadTrieDictionary(String dictionaryFile) throws IOException, CsvException {
        int wordCount = 0;
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
                    // Insert each word and their frequency into Trie
                    trie.insert(word, count);

                    // Add to wordFrequencies
                    wordFrequencies.put(word, count);
                    wordCount++;
                }
            }
        }
        System.out.println("Loaded " + wordCount + " words into Trie");
    }

    private void loadTSTDictionary(String dictionaryFile) throws IOException, CsvException {
        int wordCount = 0;
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
                    // Insert each word and their frequency into TST
                    tst.insert(word, count);
                    wordCount++;
                }
            }
        }
        System.out.println("Loaded " + wordCount + " words into TST");
    }

    public List<String> suggestWithTrie(String prefix, int limit) {
        // Track memory and time for Trie suggestion operation
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        List<String> suggestions = trie.getSuggestions(prefix.toLowerCase(), limit);

        long endTime = System.nanoTime();
        System.gc();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Trie suggestion operation memory usage: " +
                formatMemorySize(endMemory - startMemory));
        System.out.println("Trie suggestion operation time: " + (endTime - startTime) + " ns");

        return suggestions;
    }

    public List<String> suggestWithTST(String prefix, int limit) {
        // Track memory and time for TST suggestion operation
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        List<String> suggestions = tst.getSuggestions(prefix.toLowerCase(), limit);

        long endTime = System.nanoTime();
        System.gc();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("TST suggestion operation memory usage: " +
                formatMemorySize(endMemory - startMemory));
        System.out.println("TST suggestion operation time: " + (endTime - startTime) + " ns");

        return suggestions;
    }

    public void printDetailedMemoryStats() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        System.out.println("\n===== DETAILED MEMORY STATISTICS =====");
        System.out.println("Total Memory: " + formatMemorySize(totalMemory));
        System.out.println("Used Memory: " + formatMemorySize(usedMemory));
        System.out.println("Free Memory: " + formatMemorySize(freeMemory));
        System.out.println("Max Memory: " + formatMemorySize(maxMemory));
        System.out.println("Memory Usage: " + String.format("%.2f%%", (double)usedMemory * 100 / totalMemory));
        System.out.println("=====================================");
    }

    public void runMemoryBenchmark(String prefix, int iterations) {
        System.out.println("\n===== MEMORY BENCHMARK =====");
        System.out.println("Running " + iterations + " iterations for prefix: " + prefix);

        // Warm-up JVM
        for (int i = 0; i < 1000; i++) {
            suggestWithTrie(prefix, 5);
            suggestWithTST(prefix, 5);
        }

        // Benchmark Trie
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long startTrieMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTrieTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            suggestWithTrie(prefix, 5);
        }

        long endTrieTime = System.nanoTime();
        System.gc();
        long endTrieMemory = runtime.totalMemory() - runtime.freeMemory();

        long trieMemoryUsage = endTrieMemory - startTrieMemory;
        long trieTimeUsage = endTrieTime - startTrieTime;

        // Benchmark TST
        System.gc();
        long startTSTMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTSTTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            suggestWithTST(prefix, 5);
        }

        long endTSTTime = System.nanoTime();
        System.gc();
        long endTSTMemory = runtime.totalMemory() - runtime.freeMemory();

        long tstMemoryUsage = endTSTMemory - startTSTMemory;
        long tstTimeUsage = endTSTTime - startTSTTime;

        // Print results
        System.out.println("\nTrie Benchmark Results:");
        System.out.println("Memory Usage: " + formatMemorySize(trieMemoryUsage));
        System.out.println("Time Usage: " + trieTimeUsage + " ns");
        System.out.println("Average Time per Operation: " + (trieTimeUsage / iterations) + " ns");

        System.out.println("\nTST Benchmark Results:");
        System.out.println("Memory Usage: " + formatMemorySize(tstMemoryUsage));
        System.out.println("Time Usage: " + tstTimeUsage + " ns");
        System.out.println("Average Time per Operation: " + (tstTimeUsage / iterations) + " ns");

        System.out.println("\nComparison:");
        System.out.println("Memory Difference: " + formatMemorySize(Math.abs(trieMemoryUsage - tstMemoryUsage)));
        System.out.println("Time Difference: " + Math.abs(trieTimeUsage - tstTimeUsage) + " ns");

        if (trieTimeUsage < tstTimeUsage) {
            System.out.println("Trie is faster by " +
                    String.format("%.2f%%", (double)(tstTimeUsage - trieTimeUsage) * 100 / tstTimeUsage));
        } else {
            System.out.println("TST is faster by " +
                    String.format("%.2f%%", (double)(trieTimeUsage - tstTimeUsage) * 100 / trieTimeUsage));
        }

        if (trieMemoryUsage < tstMemoryUsage) {
            System.out.println("Trie uses less memory by " +
                    String.format("%.2f%%", (double)(tstMemoryUsage - trieMemoryUsage) * 100 / tstMemoryUsage));
        } else {
            System.out.println("TST uses less memory by " +
                    String.format("%.2f%%", (double)(trieMemoryUsage - tstMemoryUsage) * 100 / trieMemoryUsage));
        }

        System.out.println("===== BENCHMARK COMPLETE =====");
    }
}