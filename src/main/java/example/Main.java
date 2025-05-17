package example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private final Trie trie;
    private final TernarySearchTree tst;
    private final Map<String, Integer> wordFrequencies;
    public static String beforeInitMemory;
    public static long trieLoadTime;
    public static long tstLoadTime;
    public static long trieMemoryUsage;
    public static long tstMemoryUsage;

    public static void main(String[] args, String input) {
        try {
            // Track memory before initialization
            System.out.println("===== STARTING MEMORY TRACKING =====");
            Runtime runtime = Runtime.getRuntime();
            System.gc(); // Request garbage collection to get more accurate memory readings
            beforeInitMemory = formatMemorySize(runtime.totalMemory() - runtime.freeMemory());
            // System.out.println("Initial memory usage: " + formatMemorySize(beforeInitMemory));


            Main autocomplete = new Main("filtered_words.csv");

            // Test the autocomplete system
            // String input = "prog";

            // Memory before Trie suggestion
            System.gc();
            long beforeTrieSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();

            // RUNNING TRIE
            long startTime = System.nanoTime();
            List<List<String>> trieOutput = autocomplete.suggestWithTrie(input, 5);
            long endTime = System.nanoTime();
            // System.out.println("Time taken by Trie: " + (endTime - startTime) + " ns");
            String trieTimeSpent = (endTime - startTime) + " ns";

            // Memory after Trie suggestion
            System.gc();
            long afterTrieSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();
            // System.out.println("Memory used during Trie suggestion: " +
            //         formatMemorySize(afterTrieSuggestionMemory - beforeTrieSuggestionMemory));
            String trieMemoryUsed = formatMemorySize(afterTrieSuggestionMemory - beforeTrieSuggestionMemory);

            // Memory before TST suggestion
            System.gc();
            long beforeTSTSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();

            // RUNNING TST
            startTime = System.nanoTime();
            List<List<String>> tstOutput = autocomplete.suggestWithTST(input, 5);
            endTime = System.nanoTime();
            // System.out.println("Time taken by TST: " + (endTime - startTime) + " ns");
            String tstTimeSpent = (endTime - startTime) + " ns";

            // Memory after TST suggestion
            System.gc();
            long afterTSTSuggestionMemory = runtime.totalMemory() - runtime.freeMemory();
            // System.out.println("Memory used during TST suggestion: " +
            //         formatMemorySize(afterTSTSuggestionMemory - beforeTSTSuggestionMemory));
            String tstMemoryUsed = formatMemorySize(afterTSTSuggestionMemory - beforeTSTSuggestionMemory);

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public Main(String dictionaryFile) throws IOException, CsvException {
        // Track memory before creating data structures
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Request garbage collection
        long beforeTrieMemory = runtime.totalMemory() - runtime.freeMemory();
        List<String> allDataMain = new ArrayList<>();

        this.trie = new Trie();
        // Load only Trie first to measure its memory usage
        long startTimeTrie = System.nanoTime();
        loadTrieDictionary(dictionaryFile);
        long endTimeTrie = System.nanoTime();

        System.gc(); // Request garbage collection
        long afterTrieMemory = runtime.totalMemory() - runtime.freeMemory();
        trieMemoryUsage = afterTrieMemory - beforeTrieMemory;

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
        // System.out.println("Trie vs TST memory difference: " + formatMemorySize(Math.abs(trieMemoryUsage - tstMemoryUsage)));
        allDataMain.add(formatMemorySize(Math.abs(trieMemoryUsage - tstMemoryUsage)));
        if (trieMemoryUsage > tstMemoryUsage) {
            // System.out.println("Trie uses more memory by " +
            //         String.format("%.2f%%", (double)(trieMemoryUsage - tstMemoryUsage) * 100 / tstMemoryUsage));
            allDataMain.add("Trie uses more memory by " + String.format("%.2f%%", (double)(trieMemoryUsage - tstMemoryUsage) * 100 / tstMemoryUsage));
        } else if (tstMemoryUsage > trieMemoryUsage) {
            // System.out.println("TST uses more memory by " +
            //         String.format("%.2f%%", (double)(tstMemoryUsage - trieMemoryUsage) * 100 / trieMemoryUsage));
            allDataMain.add("TST uses more memory by " + String.format("%.2f%%", (double)(tstMemoryUsage - trieMemoryUsage) * 100 / trieMemoryUsage));
        } else {
            // System.out.println("Both data structures use the same amount of memory.");
            allDataMain.add("Both data structures use the same amount of memory.");
        }

        // return allData;
    }


    public List<String> getStatData() {
        List<String> allData = new ArrayList<>();
        allData.add("Trie Memory Usage: " + formatMemorySize(trieMemoryUsage));
        allData.add("TST Memory Usage: " + formatMemorySize(tstMemoryUsage));
        allData.add("Trie Word Loading Time: " + trieLoadTime);
        allData.add("TST Word Loading Time: " + tstLoadTime);
        return allData;
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
        // System.out.println("Loaded " + wordCount + " words into Trie");
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
        // System.out.println("Loaded " + wordCount + " words into TST");
    }

    public List<List<String>> suggestWithTrie(String prefix, int limit) {
        // Track memory and time for Trie suggestion operation
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        List<String> suggestions = trie.getSuggestions(prefix.toLowerCase(), limit);

        long endTime = System.nanoTime();
        System.gc();
        long endMemoryLong = runtime.totalMemory() - runtime.freeMemory();
        String endMemory = String.valueOf(runtime.totalMemory() - runtime.freeMemory());
        String totalMemory = formatMemorySize(endMemoryLong - startMemory);
        String operationTime = (endTime - startTime) + " ns";

        List<List<String>> allData = new ArrayList<>();
        allData.add(suggestions);
        allData.add(List.of(String.valueOf(endMemory)));
        allData.add(List.of(totalMemory));
        allData.add(List.of(operationTime));
        return allData;
    }

    public List<List<String>> suggestWithTST(String prefix, int limit) {
        // Track memory and time for TST suggestion operation
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        List<String> suggestions = tst.getSuggestions(prefix.toLowerCase(), limit);

        long endTime = System.nanoTime();
        System.gc();
        long endMemoryLong = runtime.totalMemory() - runtime.freeMemory();
        String endMemory = String.valueOf(runtime.totalMemory() - runtime.freeMemory());
        String totalMemory = formatMemorySize(endMemoryLong - startMemory);
        String operationTime = (endTime - startTime) + " ns";

        List<List<String>> allData = new ArrayList<>();
        allData.add(suggestions);
        allData.add(List.of(String.valueOf(endMemory)));
        allData.add(List.of(totalMemory));
        allData.add(List.of(operationTime));
        return allData;
    }
}