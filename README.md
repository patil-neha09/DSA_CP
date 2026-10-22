# Adaptive Collision Resolution Framework

An interactive, educational Java application designed for Data Structures and Algorithms (DSA) coursework. This framework implements, analyzes, and compares open-addressing collision resolution strategies (**Linear Probing**, **Quadratic Probing**, and **Double Hashing**), featuring a real-time **Adaptive Decision Engine** that dynamically switches probing strategies to mitigate performance degradation (such as primary and secondary clustering).

---

## 1. Technology Stack

- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing (`javax.swing`)
- **CLI Interface:** Interactive Console Menu with guided educational demonstration
- **Build & Dependency Tool:** Apache Maven
- **Testing Framework:** JUnit 5 (Jupiter)
- **Architecture:** Model-View-Controller (MVC) with Strategy Pattern & Adaptive Decision Engine
- **Persistence:** CSV export for operation history logs

> **Note:** Strictly built without external web or backend frameworks (no Spring Boot, REST APIs, JavaScript, or MySQL). Direct Java method invocations only.

---

## 2. Architecture & Design Decisions

```text
[Java Swing UI / Interactive CLI]
               │
               ▼
       [HashController]
               │
       ┌───────┴────────────────────────────────┐
       ▼                                        ▼
[HashTable Engine] ◄────────────► [Adaptive Decision Engine]
       │                                        ▲
       ▼                                        │
[Probing Strategies]              [Performance Analyzer]
 (Linear, Quadratic, Double)                    │
       │                                        │
       ▼                                        │
[Statistics & Operation History] ───────────────┘
```

### Key Design Decisions:
1. **Strategy Pattern (`ProbingStrategy`):**
   Decouples collision resolution from the hash table. Allows hot-swapping between `LinearProbing`, `QuadraticProbing`, and `DoubleHashing` at runtime.
2. **Explicit Slot States (`SlotState`):**
   Uses explicit states: `EMPTY` (slot never occupied), `OCCUPIED` (active entry present), and `DELETED` (tombstone).
3. **Safe Tombstone Traversal & Lazy Deletion:**
   - **Search:** Bypasses `DELETED` slots to prevent prematurely breaking probe chains.
   - **Insert:** Tracks the *first* encountered `DELETED` slot to reuse storage, but continues probing until an `EMPTY` slot is reached to verify that duplicate keys do not already exist.
4. **Modulo & Negative Keys:**
   Uses `Math.floorMod()` throughout to ensure valid array indices `[0, capacity - 1]` for negative integer keys (e.g. `Math.floorMod(-4, 7) == 3`) and extreme boundaries such as `Integer.MIN_VALUE`.
5. **Prime Capacities & Coprimality Guarantee:**
   Maintains prime table sizes via `HashFunction.nextPrime()`. For Double Hashing, step size $h_2(k)$ is verified coprime with table capacity $m$ via Euclidean GCD ($\gcd(step, m) = 1$), guaranteeing complete traversal across all $m$ slots without infinite loops.
6. **Automatic Resizing & Rehashing:**
   When the active load factor would exceed 70% ($\alpha = \frac{n + 1}{m} > 0.70$) or when slots are exhausted, the table automatically resizes to the next prime capacity $m' > 2m$, rehashing all active entries and purging tombstones. When adaptive strategy switching occurs, entries are rehashed into the new probe distribution.

---

## 3. Mathematical Formulas

### Linear Probing
$$h(k, i) = (h'(k) + i) \bmod m$$
- **Characteristics:** Cache-friendly due to spatial locality, but vulnerable to **Primary Clustering** (long contiguous blocks of occupied slots that grow rapidly).

### Quadratic Probing
$$h(k, i) = (h'(k) + c_1 \cdot i + c_2 \cdot i^2) \bmod m$$
- **Default Coefficients:** $c_1 = 1, c_2 = 1$.
- **Characteristics:** Eliminates primary clustering by jumping quadratically, but keys with the same initial hash follow the same probe sequence (**Secondary Clustering**). Bounded at $m$ attempts to avoid cycling.

### Double Hashing
$$h(k, i) = (h_1(k) + i \cdot h_2(k)) \bmod m$$
- **Primary Hash:** $h_1(k) = k \bmod m$ (via `Math.floorMod`)
- **Secondary Hash Step:** $h_2(k) = q - (k \bmod q)$, where $q = \text{largest prime } < m$.
- **Coprimality Guarantee:** If $\gcd(h_2(k), m) > 1$, step size is safely adjusted to ensure full permutation of all $m$ slots.
- **Characteristics:** Eliminates both primary and secondary clustering.

### Clustering Factor Heuristic
The performance analyzer calculates the weighted mean cluster length:
$$\text{Cluster Factor} = \frac{\sum L_i^2}{\sum L_i}$$
where $L_i$ are contiguous runs of occupied slots in the circular array.

---

## 4. Package Organization

| Package | Purpose | Key Classes & Interfaces |
|---|---|---|
| `com.adaptivehash` | Application entry point & interactive CLI | `Main.java` |
| `com.adaptivehash.core` | Core hash table engine & hash function contracts | `HashTable.java`, `HashFunction.java` |
| `com.adaptivehash.probing` | Open addressing probing strategies | `ProbingStrategy.java`, `LinearProbing.java`, `QuadraticProbing.java`, `DoubleHashing.java` |
| `com.adaptivehash.adaptive` | Dynamic adaptation and clustering analysis | `AdaptiveDecisionEngine.java`, `PerformanceAnalyzer.java` |
| `com.adaptivehash.statistics` | Real-time metrics and aggregated telemetry | `Statistics.java`, `OperationMetrics.java` |
| `com.adaptivehash.history` | Audit log and CSV persistence | `OperationHistory.java` |
| `com.adaptivehash.controller` | Direct method invocation mediator | `HashController.java` |
| `com.adaptivehash.ui` | Swing desktop interface panels and main frame | `MainFrame.java`, `DashboardPanel.java`, `HashTablePanel.java`, `StatisticsPanel.java`, `HistoryPanel.java` |

---

## 5. Building and Running the Project

### Prerequisites
- JDK 17 or higher
- Apache Maven 3.8+ (bundled or installed)

### Build Commands
```bash
# Clean and compile the codebase
mvn clean compile

# Run JUnit 5 test suite
mvn test

# Package into runnable executable JAR
mvn clean package

# Run interactive CLI application via Maven Exec Plugin
mvn exec:java -Dexec.mainClass="com.adaptivehash.Main"

# Alternatively, run packaged JAR directly
java -jar target/adaptive-collision-resolution-framework-1.0.0-SNAPSHOT.jar

# Run Java Swing Desktop GUI
java -jar target/adaptive-collision-resolution-framework-1.0.0-SNAPSHOT.jar --gui
```

---

## 6. Interactive Menu & Educational Demonstration

When running the application in a terminal, the interactive menu presents 10 options:

```text
========================================================================
      WELCOME TO THE ADAPTIVE COLLISION RESOLUTION FRAMEWORK (DSA)
========================================================================
------------------------------------------------------------------------
  Active Strategy: Linear Probing     | Adaptive Mode: ENABLED (Auto)
  Capacity: 7      | Size: 0      | Deleted: 0      | Load Factor: 0.00
------------------------------------------------------------------------
 1. Choose a probing strategy, or toggle adaptive strategy engine
 2. Insert key
 3. Search key
 4. Delete key (lazy deletion / tombstone)
 5. Display complete table (slots, keys, and states)
 6. Display current size, capacity, load factor, and deleted-slot count
 7. Display operation metrics and telemetry history
 8. Compare the three strategies using the same input dataset
 9. Run guided educational demonstration (10 & 17 collision, resize, etc.)
10. Launch Desktop GUI (Swing)
 0. Exit
```

### Sample Output from Guided Educational Demo (Option 9)

```text
========================================================================
   GUIDED EDUCATIONAL DEMONSTRATION: Collision Resolution & Adaptation
========================================================================
[Step 1] Initialized HashTable: Capacity = 7, Strategy = Linear Probing

[Step 2] Inserting key = 10:
         Formula: 10 mod 7 = 3. Slot 3 is empty.
         => Result: Slot [3], Probes = 1, Collisions = 0, Success = true

[Step 3] Inserting key = 17:
         Formula: 17 mod 7 = 3. Slot 3 is OCCUPIED by key 10 -> COLLISION!
         Linear Probing attempts slot (3 + 1) mod 7 = 4. Slot 4 is empty.
         => Result: Slot [4], Probes = 2, Collisions = 1, Success = true

[Step 4] Attempting to insert duplicate key = 10:
         => Result: Success = false (Duplicate rejected), Probes = 1, Collisions = 0

[Step 5] Handling negative integer key = -4:
         Math.floorMod(-4, 7) = 3 (since -4 = -1*7 + 3).
         Slot 3 occupied (10), Slot 4 occupied (17) -> Probes to slot 5.
         => Result: Slot [5], Probes = 3, Collisions = 2, Success = true

[Step 6] Searching for keys:
         - Search(17): Present = true, Value = 170, Probes = 2, Collisions = 1
         - Search(99): Present = false (Key missing), Probes = 1, Collisions = 0

[Step 7] Lazy deletion of key = 10:
         - Delete(10): Success = true. Slot 3 is marked as DELETED (tombstone).
         - Active Size = 2, Deleted Count = 1
         - Search(17) after deleting 10: Found = true (Search correctly bypassed tombstone!)

[Step 8] Reusing deleted slot (Tombstone reuse):
         Inserting key = 31 (31 mod 7 = 3).
         => Result: Key 31 placed in reused slot [3], Deleted Count is now 0

[Step 9] Demonstrating automatic table resizing:
         Current Capacity = 7, Size = 3, Load Factor = 0.43
         Inserting keys 50, 60, 70 to exceed 70% threshold...
         => After Resizing: New Prime Capacity = 17, Active Size = 6, Load Factor = 0.35
         - Verify all keys intact: contains(17)=true, contains(31)=true, contains(-4)=true, contains(70)=true
========================================================================
```

### Sample Output from Strategy Comparison (Option 8)

```text
========================================================================
   BENCHMARK: Comparing Probing Strategies on Identical Collision Dataset
========================================================================
Initial Capacity : 11
Input Keys       : [12, 23, 34, 45, 56, 67, 78] (all keys map initially to index 1)
------------------------------------------------------------------------
Strategy             | Total Keys   | Total Probes   | Avg Probes/Ins  | Collisions  
------------------------------------------------------------------------
Linear Probing       | 7            | 28             | 4.00            | 21          
Quadratic Probing    | 7            | 28             | 4.00            | 21          
Double Hashing       | 7            | 13             | 1.86            | 6           
------------------------------------------------------------------------
DSA Analysis: Linear Probing suffers from Primary Clustering as clusters merge.
Quadratic Probing eliminates primary clustering, while Double Hashing scatters
keys across the table using key-dependent step sizes, minimizing cluster formation.
```

---

## 7. Development Roadmap

- [x] Phase 1: Folder structure, package declarations, class skeletons, and Maven configuration.
- [x] Phase 2: Core hash table algorithms, collision tracking, and probing implementations.
- [x] Phase 3: Adaptive decision engine, clustering heuristic calculations, and strategy switching.
- [x] Phase 4: Interactive Java Swing visualization grid, dashboard controls, and real-time graphs.
- [x] Phase 5: CSV history export, edge case validation, and automated stress testing.
- [x] Phase 6: Comprehensive unit test suite with 100% pass rate.
