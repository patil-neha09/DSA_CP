# Adaptive Collision Resolution Framework

An interactive, educational Java Swing desktop application designed for Data Structures and Algorithms (DSA) coursework. This framework demonstrates and analyzes open-addressing collision resolution strategies (**Linear Probing**, **Quadratic Probing**, and **Double Hashing**), featuring a real-time **Adaptive Decision Engine** that dynamically switches probing strategies to mitigate performance degradation (such as primary and secondary clustering).

---

## 1. Technology Stack

- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing (`javax.swing`)
- **Build & Dependency Tool:** Apache Maven
- **Testing Framework:** JUnit 5 (Jupiter)
- **Architecture:** Model-View-Controller (MVC) with Strategy Pattern & Adaptive Decision Engine
- **Persistence:** Optional CSV export for operation history logs

> **Note:** Strictly built without external web or backend frameworks (no Spring Boot, REST APIs, JavaScript, or MySQL). Direct Java method invocations only.

---

## 2. Architecture & Design

The application follows a clean layered separation of concerns:

```text
[Java Swing UI]
       │
       ▼
[HashController]
       │
       ├────────────────────────────────────────┐
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

- **UI Layer (`com.adaptivehash.ui`):** Standalone desktop GUI built with standard Swing components (`JFrame`, `JPanel`, `JSplitPane`, `JTable`).
- **Controller Layer (`com.adaptivehash.controller`):** Dispatches user actions, synchronizes UI state, and triggers adaptive analysis.
- **Core Engine (`com.adaptivehash.core`):** Open-addressed hash table engine with modular hash functions.
- **Probing Strategies (`com.adaptivehash.probing`):** Polymorphic collision resolution strategies implementing `ProbingStrategy`.
- **Adaptive Engine (`com.adaptivehash.adaptive`):** Detects clustering and excessive probe sequences, providing algorithmic recommendations.
- **Statistics & History (`com.adaptivehash.statistics`, `com.adaptivehash.history`):** Real-time operational metric collection and historical audit logging.

---

## 3. Package Organization

| Package | Purpose | Key Classes & Interfaces |
|---|---|---|
| `com.adaptivehash` | Application entry point | `Main.java` |
| `com.adaptivehash.core` | Core hash table engine & hash function contracts | `HashTable.java`, `HashFunction.java` |
| `com.adaptivehash.probing` | Open addressing probing strategies | `ProbingStrategy.java`, `LinearProbing.java`, `QuadraticProbing.java`, `DoubleHashing.java` |
| `com.adaptivehash.adaptive` | Dynamic adaptation and clustering analysis | `AdaptiveDecisionEngine.java`, `PerformanceAnalyzer.java` |
| `com.adaptivehash.statistics` | Real-time metrics and aggregated telemetry | `Statistics.java`, `OperationMetrics.java` |
| `com.adaptivehash.history` | Audit log and optional CSV persistence | `OperationHistory.java` |
| `com.adaptivehash.controller` | Direct method invocation mediator | `HashController.java` |
| `com.adaptivehash.ui` | Swing desktop interface panels and main frame | `MainFrame.java`, `DashboardPanel.java`, `HashTablePanel.java`, `StatisticsPanel.java`, `HistoryPanel.java` |

---

## 4. Building and Running the Project

### Prerequisites
- JDK 17 or higher
- Apache Maven 3.8+ (or Maven bundled with IntelliJ IDEA)

### Build Commands
```bash
# Clean and compile the codebase
mvn clean compile

# Run JUnit 5 test suite
mvn test

# Package into an executable JAR
mvn clean package

# Run the packaged application
java -jar target/adaptive-collision-resolution-framework-1.0.0-SNAPSHOT.jar
```

### Running in IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File -> Open...** and choose the `adaptive_collision_framework` root directory.
3. IntelliJ will automatically detect `pom.xml` as a Maven project.
4. Navigate to `src/main/java/com/adaptivehash/Main.java` and click **Run 'Main.main()'**.

---

## 5. Development Roadmap
- [x] Phase 1: Folder structure, package declarations, class skeletons, and Maven configuration.
- [ ] Phase 2: Core hash table algorithms, collision tracking, and probing implementations.
- [ ] Phase 3: Adaptive decision engine, clustering heuristic calculations, and strategy switching.
- [ ] Phase 4: Interactive Java Swing visualization grid, dashboard controls, and real-time graphs.
- [ ] Phase 5: CSV history export, edge case validation, and automated stress testing.
