package com.adaptivehash;

import com.adaptivehash.adaptive.AdaptiveDecisionEngine;
import com.adaptivehash.controller.HashController;
import com.adaptivehash.core.HashFunction;
import com.adaptivehash.core.HashTable;
import com.adaptivehash.probing.DoubleHashing;
import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.probing.QuadraticProbing;
import com.adaptivehash.statistics.OperationMetrics;
import com.adaptivehash.statistics.Statistics;
import com.adaptivehash.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main application entry point for the Adaptive Collision Resolution Framework.
 * Supports interactive CLI menu, guided DSA demonstration, strategy comparative benchmark,
 * and optional Java Swing desktop GUI.
 */
public class Main {

    public static void main(String[] args) {
        // Check if explicit GUI argument is provided
        boolean launchGuiArg = Arrays.asList(args).contains("--gui");

        if (launchGuiArg) {
            launchGui();
            return;
        }

        // Run interactive CLI menu by default
        runInteractiveCli();
    }

    /**
     * Launches the Java Swing Desktop GUI on the Event Dispatch Thread (EDT).
     */
    public static void launchGui() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("GraphicsEnvironment is headless; Swing GUI cannot be displayed in this terminal.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    /**
     * Runs the interactive console menu for the framework.
     */
    public static void runInteractiveCli() {
        HashController controller = new HashController(7, new LinearProbing());
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("========================================================================");
        System.out.println("      WELCOME TO THE ADAPTIVE COLLISION RESOLUTION FRAMEWORK (DSA)");
        System.out.println("========================================================================");

        while (running) {
            printMenuHeader(controller);
            printMenuOptions();

            System.out.print("Select an option [0-10]: ");
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (input) {
                case "1" -> handleChooseStrategy(controller, scanner);
                case "2" -> handleInsertKey(controller, scanner);
                case "3" -> handleSearchKey(controller, scanner);
                case "4" -> handleDeleteKey(controller, scanner);
                case "5" -> handleDisplayTable(controller);
                case "6" -> handleDisplayTableMetadata(controller);
                case "7" -> handleDisplayMetrics(controller);
                case "8" -> handleCompareStrategies();
                case "9" -> runEducationalDemo();
                case "10" -> {
                    System.out.println("Launching Swing Desktop GUI...");
                    launchGui();
                }
                case "0" -> {
                    System.out.println("Exiting Adaptive Collision Resolution Framework. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter a number between 0 and 10.");
            }
            System.out.println();
        }
    }

    private static void printMenuHeader(HashController controller) {
        HashTable<String, String> ht = controller.getHashTable();
        AdaptiveDecisionEngine engine = controller.getDecisionEngine();
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("  Active Strategy: %-18s | Adaptive Mode: %s%n",
                ht.getProbingStrategy().getName(),
                engine.isAutoAdaptEnabled() ? "ENABLED (Auto)" : "DISABLED (Manual)");
        System.out.printf("  Capacity: %-6d | Size: %-6d | Deleted: %-6d | Load Factor: %.2f%n",
                ht.capacity(), ht.size(), ht.getDeletedCount(), ht.loadFactor());
        System.out.println("------------------------------------------------------------------------");
    }

    private static void printMenuOptions() {
        System.out.println(" 1. Choose a probing strategy, or toggle adaptive strategy engine");
        System.out.println(" 2. Insert key");
        System.out.println(" 3. Search key");
        System.out.println(" 4. Delete key (lazy deletion / tombstone)");
        System.out.println(" 5. Display complete table (slots, keys, and states)");
        System.out.println(" 6. Display current size, capacity, load factor, and deleted-slot count");
        System.out.println(" 7. Display operation metrics and telemetry history");
        System.out.println(" 8. Compare the three strategies using the same input dataset");
        System.out.println(" 9. Run guided educational demonstration (10 & 17 collision, resize, etc.)");
        System.out.println("10. Launch Desktop GUI (Swing)");
        System.out.println(" 0. Exit");
    }

    private static void handleChooseStrategy(HashController controller, Scanner scanner) {
        System.out.println("\nSelect Probing Strategy:");
        System.out.println("  1. Linear Probing   : h(k, i) = (h'(k) + i) mod m");
        System.out.println("  2. Quadratic Probing: h(k, i) = (h'(k) + i + i^2) mod m");
        System.out.println("  3. Double Hashing   : h(k, i) = (h1(k) + i * h2(k)) mod m");
        System.out.printf("  4. Toggle Adaptive Mode (Currently: %s)%n",
                controller.getDecisionEngine().isAutoAdaptEnabled() ? "ON" : "OFF");
        System.out.print("Choice: ");

        String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        switch (choice) {
            case "1" -> {
                controller.switchProbingStrategy(new LinearProbing());
                System.out.println("Switched to Linear Probing and rehashed table.");
            }
            case "2" -> {
                controller.switchProbingStrategy(new QuadraticProbing());
                System.out.println("Switched to Quadratic Probing and rehashed table.");
            }
            case "3" -> {
                controller.switchProbingStrategy(new DoubleHashing());
                System.out.println("Switched to Double Hashing and rehashed table.");
            }
            case "4" -> {
                boolean current = controller.getDecisionEngine().isAutoAdaptEnabled();
                controller.getDecisionEngine().setAutoAdaptEnabled(!current);
                System.out.println("Adaptive mode is now " + (!current ? "ENABLED" : "DISABLED") + ".");
            }
            default -> System.out.println("No changes made.");
        }
    }

    private static void handleInsertKey(HashController controller, Scanner scanner) {
        System.out.print("\nEnter key to insert (integer or text): ");
        String key = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (key.isEmpty()) {
            System.out.println("Error: Key cannot be empty.");
            return;
        }

        System.out.print("Enter associated value (press Enter to use key as value): ");
        String value = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (value.isEmpty()) {
            value = key;
        }

        OperationMetrics m = controller.insert(key, value);
        if (m.isSuccessful()) {
            System.out.printf("SUCCESS: Inserted key '%s' into slot [%d]. Initial Hash: %d, Probes: %d, Collisions: %d, Time: %d ns.%n",
                    m.getKey(), m.getFinalSlotIndex(), m.getInitialHash(), m.getProbeCount(), m.getCollisionCount(), m.getExecutionTimeNanos());
        } else {
            System.out.printf("REJECTED: Key '%s' could not be inserted (duplicate key or table capacity limit reached). Probes: %d, Collisions: %d.%n",
                    m.getKey(), m.getProbeCount(), m.getCollisionCount());
        }
    }

    private static void handleSearchKey(HashController controller, Scanner scanner) {
        System.out.print("\nEnter key to search: ");
        String key = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (key.isEmpty()) {
            System.out.println("Error: Key cannot be empty.");
            return;
        }

        Optional<String> res = controller.search(key);
        OperationMetrics m = controller.getHashTable().getLastOperationMetrics();
        if (res.isPresent()) {
            System.out.printf("FOUND: Key '%s' => Value = '%s' at slot [%d]. Probes: %d, Collisions: %d, Time: %d ns.%n",
                    key, res.get(), m.getFinalSlotIndex(), m.getProbeCount(), m.getCollisionCount(), m.getExecutionTimeNanos());
        } else {
            System.out.printf("NOT FOUND: Key '%s' is absent. Probes: %d, Collisions: %d, Time: %d ns.%n",
                    key, m.getProbeCount(), m.getCollisionCount(), m.getExecutionTimeNanos());
        }
    }

    private static void handleDeleteKey(HashController controller, Scanner scanner) {
        System.out.print("\nEnter key to delete: ");
        String key = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (key.isEmpty()) {
            System.out.println("Error: Key cannot be empty.");
            return;
        }

        boolean deleted = controller.delete(key);
        OperationMetrics m = controller.getHashTable().getLastOperationMetrics();
        if (deleted) {
            System.out.printf("DELETED: Key '%s' deleted from slot [%d] (marked as tombstone). Probes: %d, Collisions: %d, Time: %d ns.%n",
                    key, m.getFinalSlotIndex(), m.getProbeCount(), m.getCollisionCount(), m.getExecutionTimeNanos());
        } else {
            System.out.printf("DELETE FAILED: Key '%s' does not exist in the table. Probes: %d, Collisions: %d.%n",
                    key, m.getProbeCount(), m.getCollisionCount());
        }
    }

    private static void handleDisplayTable(HashController controller) {
        HashTable<String, String> ht = controller.getHashTable();
        System.out.println("\n--- Current Hash Table State ---");
        System.out.printf("%-6s | %-12s | %-15s | %-15s%n", "Slot", "State", "Key", "Value");
        System.out.println("---------------------------------------------------------");

        HashTable.Entry<String, String>[] table = ht.getTable();
        for (int i = 0; i < ht.capacity(); i++) {
            HashTable.Entry<String, String> entry = table[i];
            if (entry == null) {
                System.out.printf("[%4d] | %-12s | %-15s | %-15s%n", i, "EMPTY", "-", "-");
            } else if (entry.isDeleted()) {
                System.out.printf("[%4d] | %-12s | %-15s | %-15s%n", i, "DELETED", entry.getKey(), entry.getValue());
            } else {
                System.out.printf("[%4d] | %-12s | %-15s | %-15s%n", i, "OCCUPIED", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("---------------------------------------------------------");
    }

    private static void handleDisplayTableMetadata(HashController controller) {
        HashTable<String, String> ht = controller.getHashTable();
        System.out.println("\n--- Hash Table Metrics Summary ---");
        System.out.printf("  Active Elements (size) : %d%n", ht.size());
        System.out.printf("  Table Capacity (m)     : %d%n", ht.capacity());
        System.out.printf("  Deleted Slots (tomb)   : %d%n", ht.getDeletedCount());
        System.out.printf("  Active Load Factor (α) : %.4f (Threshold: %.2f)%n", ht.loadFactor(), ht.getMaxLoadFactor());
        System.out.printf("  Total Density (Active+Del): %.4f%n", ht.totalLoadFactor());
        System.out.printf("  Active Probing Strategy: %s%n", ht.getProbingStrategy().getName());
    }

    private static void handleDisplayMetrics(HashController controller) {
        Statistics stats = controller.getStatistics();
        System.out.println("\n--- Real-Time Operation Telemetry ---");
        System.out.printf("  Total Operations       : %d%n", stats.getTotalOperations());
        System.out.printf("  Total Insertions       : %d%n", stats.getTotalInsertions());
        System.out.printf("  Total Searches         : %d%n", stats.getTotalSearches());
        System.out.printf("  Total Deletions        : %d%n", stats.getTotalDeletions());
        System.out.printf("  Total Collisions       : %d%n", stats.getTotalCollisions());
        System.out.printf("  Total Probes           : %d%n", stats.getTotalProbes());
        System.out.printf("  Average Probes / Op    : %.2f%n", stats.getAverageProbes());
        System.out.printf("  Collision Rate         : %.2f%%%n", stats.getCollisionRate() * 100);
        System.out.printf("  Max Probes Encountered : %d%n", stats.getMaxProbesEncountered());

        List<OperationMetrics> recent = controller.getHistory().getRecent(5);
        if (!recent.isEmpty()) {
            System.out.println("\n  Recent Operations:");
            for (OperationMetrics m : recent) {
                System.out.printf("    - %-6s key='%s' -> slot=%d, probes=%d, collisions=%d, success=%b, time=%d ns%n",
                        m.getOperationType(), m.getKey(), m.getFinalSlotIndex(), m.getProbeCount(), m.getCollisionCount(), m.isSuccessful(), m.getExecutionTimeNanos());
            }
        }
    }

    /**
     * Requirement F: Compare the three strategies using the same input dataset.
     */
    public static void handleCompareStrategies() {
        System.out.println("\n========================================================================");
        System.out.println("   BENCHMARK: Comparing Probing Strategies on Identical Collision Dataset");
        System.out.println("========================================================================");

        // Dataset chosen to cause heavy collisions in capacity 11 (all keys congruent mod 11)
        int initialCapacity = 11;
        List<Integer> dataset = List.of(12, 23, 34, 45, 56, 67, 78);
        System.out.printf("Initial Capacity : %d%n", initialCapacity);
        System.out.printf("Input Keys       : %s (all keys map initially to index 1)%n", dataset);
        System.out.println("------------------------------------------------------------------------");

        ProbingStrategy[] strategies = new ProbingStrategy[]{
                new LinearProbing(),
                new QuadraticProbing(1, 1),
                new DoubleHashing()
        };

        System.out.printf("%-20s | %-12s | %-14s | %-15s | %-12s%n",
                "Strategy", "Total Keys", "Total Probes", "Avg Probes/Ins", "Collisions");
        System.out.println("------------------------------------------------------------------------");

        for (ProbingStrategy strategy : strategies) {
            HashTable<Integer, Integer> table = new HashTable<>(initialCapacity, strategy, HashFunction.DEFAULT_INTEGER_HASH);
            table.setMaxLoadFactor(0.90); // allow table to fill up to see clustering behavior

            int totalCollisions = 0;
            int totalProbes = 0;

            for (Integer key : dataset) {
                OperationMetrics m = table.insert(key, key);
                totalCollisions += m.getCollisionCount();
                totalProbes += m.getProbeCount();
            }

            double avgProbes = (double) totalProbes / dataset.size();
            System.out.printf("%-20s | %-12d | %-14d | %-15.2f | %-12d%n",
                    strategy.getName(), dataset.size(), totalProbes, avgProbes, totalCollisions);
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.println("DSA Analysis: Linear Probing suffers from Primary Clustering as clusters merge.");
        System.out.println("Quadratic Probing eliminates primary clustering, while Double Hashing scatters");
        System.out.println("keys across the table using key-dependent step sizes, minimizing cluster formation.");
    }

    /**
     * Requirement F: Guided Educational Demonstration showing:
     * - Inserting 10 and 17 into a table with capacity 7 (initial hash collision at index 3).
     * - Searching for existing and missing keys.
     * - Duplicate key rejection.
     * - Negative key handling.
     * - Lazy deletion / tombstone creation.
     * - Reusing deleted slots.
     * - Table resizing.
     * - Operation metrics.
     */
    public static void runEducationalDemo() {
        System.out.println("\n========================================================================");
        System.out.println("   GUIDED EDUCATIONAL DEMONSTRATION: Collision Resolution & Adaptation");
        System.out.println("========================================================================");

        // 1. Initialize table of capacity 7 with Linear Probing
        HashTable<Integer, Integer> table = new HashTable<>(7, new LinearProbing(), HashFunction.DEFAULT_INTEGER_HASH);
        System.out.printf("[Step 1] Initialized HashTable: Capacity = %d, Strategy = %s%n%n",
                table.capacity(), table.getProbingStrategy().getName());

        // 2. Insert 10: 10 mod 7 = 3
        System.out.println("[Step 2] Inserting key = 10:");
        System.out.println("         Formula: 10 mod 7 = 3. Slot 3 is empty.");
        OperationMetrics m1 = table.insert(10, 100);
        System.out.printf("         => Result: Slot [%d], Probes = %d, Collisions = %d, Success = %b%n%n",
                m1.getFinalSlotIndex(), m1.getProbeCount(), m1.getCollisionCount(), m1.isSuccessful());

        // 3. Insert 17: 17 mod 7 = 3 (COLLISION!)
        System.out.println("[Step 3] Inserting key = 17:");
        System.out.println("         Formula: 17 mod 7 = 3. Slot 3 is OCCUPIED by key 10 -> COLLISION!");
        System.out.println("         Linear Probing attempts slot (3 + 1) mod 7 = 4. Slot 4 is empty.");
        OperationMetrics m2 = table.insert(17, 170);
        System.out.printf("         => Result: Slot [%d], Probes = %d, Collisions = %d, Success = %b%n%n",
                m2.getFinalSlotIndex(), m2.getProbeCount(), m2.getCollisionCount(), m2.isSuccessful());

        // 4. Duplicate key rejection
        System.out.println("[Step 4] Attempting to insert duplicate key = 10:");
        OperationMetrics mDup = table.insert(10, 999);
        System.out.printf("         => Result: Success = %b (Duplicate rejected), Probes = %d, Collisions = %d%n%n",
                mDup.isSuccessful(), mDup.getProbeCount(), mDup.getCollisionCount());

        // 5. Negative key handling
        System.out.println("[Step 5] Handling negative integer key = -4:");
        System.out.println("         Math.floorMod(-4, 7) = 3 (since -4 = -1*7 + 3).");
        System.out.println("         Slot 3 occupied (10), Slot 4 occupied (17) -> Probes to slot 5.");
        OperationMetrics mNeg = table.insert(-4, -400);
        System.out.printf("         => Result: Slot [%d], Probes = %d, Collisions = %d, Success = %b%n%n",
                mNeg.getFinalSlotIndex(), mNeg.getProbeCount(), mNeg.getCollisionCount(), mNeg.isSuccessful());

        // 6. Searching existing and missing keys
        System.out.println("[Step 6] Searching for keys:");
        Optional<Integer> s17 = table.search(17);
        OperationMetrics mSearch17 = table.getLastOperationMetrics();
        System.out.printf("         - Search(17): Present = %b, Value = %s, Probes = %d, Collisions = %d%n",
                s17.isPresent(), s17.orElse(null), mSearch17.getProbeCount(), mSearch17.getCollisionCount());

        Optional<Integer> s99 = table.search(99);
        OperationMetrics mSearch99 = table.getLastOperationMetrics();
        System.out.printf("         - Search(99): Present = %b (Key missing), Probes = %d, Collisions = %d%n%n",
                s99.isPresent(), mSearch99.getProbeCount(), mSearch99.getCollisionCount());

        // 7. Lazy Deletion (Tombstone)
        System.out.println("[Step 7] Lazy deletion of key = 10:");
        boolean del10 = table.delete(10);
        System.out.printf("         - Delete(10): Success = %b. Slot 3 is marked as DELETED (tombstone).%n", del10);
        System.out.printf("         - Active Size = %d, Deleted Count = %d%n", table.size(), table.getDeletedCount());
        // Verify 17 is still found even though 10 was deleted!
        Optional<Integer> s17AfterDel = table.search(17);
        System.out.printf("         - Search(17) after deleting 10: Found = %b (Search correctly bypassed tombstone!)%n%n",
                s17AfterDel.isPresent());

        // 8. Reusing deleted slot
        System.out.println("[Step 8] Reusing deleted slot (Tombstone reuse):");
        System.out.println("         Inserting key = 31 (31 mod 7 = 3).");
        OperationMetrics mReuse = table.insert(31, 310);
        System.out.printf("         => Result: Key 31 placed in reused slot [%d], Deleted Count is now %d%n%n",
                mReuse.getFinalSlotIndex(), table.getDeletedCount());

        // 9. Table Resizing
        System.out.println("[Step 9] Demonstrating automatic table resizing:");
        System.out.printf("         Current Capacity = %d, Size = %d, Load Factor = %.2f%n",
                table.capacity(), table.size(), table.loadFactor());
        System.out.println("         Inserting keys 50, 60, 70 to exceed 70% threshold...");
        table.insert(50, 500);
        table.insert(60, 600);
        table.insert(70, 700);
        System.out.printf("         => After Resizing: New Prime Capacity = %d, Active Size = %d, Load Factor = %.2f%n",
                table.capacity(), table.size(), table.loadFactor());
        System.out.printf("         - Verify all keys intact: contains(17)=%b, contains(31)=%b, contains(-4)=%b, contains(70)=%b%n",
                table.contains(17), table.contains(31), table.contains(-4), table.contains(70));
        System.out.println("========================================================================");
    }
}
