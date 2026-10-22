# Architecture & Algorithmic Analysis

## 1. High-Level Architecture

The **Adaptive Collision Resolution Framework** is built using the Model-View-Controller (MVC) architectural pattern combined with the Strategy Pattern and an Adaptive Decision Heuristic Engine.

```text
[Java Swing UI / CLI]
        │
        ▼
 [HashController] ◄────────────────────────────────────────┐
        │                                                  │
        ├─────────────────────────────┐                    │
        ▼                             ▼                    │
 [HashTable Engine]         [AdaptiveDecisionEngine]       │
        │                             ▲                    │
        ├──────────────────┐          │                    │
        ▼                  ▼          │                    │
[Probing Strategies]  [HashFunction]  │                    │
        │                             │                    │
        ▼                             ▼                    │
[OperationMetrics] ──► [Statistics] ──┴─► [PerformanceAnalyzer]
        │
        ▼
[OperationHistory] (CSV Export)
```

---

## 2. Collision Resolution Probing Strategies

### 1. Linear Probing
- **Definition:** $h(k, i) = (h'(k) + i) \bmod m$
- **Average Search Cost (Successful):** $\approx \frac{1}{2} \left(1 + \frac{1}{1 - \alpha}\right)$
- **Average Search Cost (Unsuccessful):** $\approx \frac{1}{2} \left(1 + \frac{1}{(1 - \alpha)^2}\right)$
- **Issue:** Primary Clustering. Once a cluster of size $L$ forms, the probability that the next key joins and expands this cluster is $\frac{L + 1}{m}$, accelerating cluster growth.

### 2. Quadratic Probing
- **Definition:** $h(k, i) = (h'(k) + c_1 i + c_2 i^2) \bmod m$
- **Issue Solved:** Eliminates primary clustering.
- **Limitation:** Secondary Clustering. Keys with identical initial hash values share the identical probe sequence.

### 3. Double Hashing
- **Definition:** $h(k, i) = (h_1(k) + i \cdot h_2(k)) \bmod m$
- **Secondary Hash Step:** $h_2(k) = q - (k \bmod q)$, where $q = \text{largest prime } < m$.
- **Issue Solved:** Eliminates both primary and secondary clustering by assigning each key a unique step size.

---

## 3. Tombstone Traversal & Lazy Deletion

```text
Slot Index:   [0]      [1]          [2]          [3]      [4]
Entry:       EMPTY   OCCUPIED     DELETED     OCCUPIED   EMPTY
                      (key A)    (tombstone)   (key B)
```

- When searching for `key B`:
  1. Hashes to slot 1 (occupied by `key A`, collision).
  2. Probes slot 2 (`DELETED`). Search does **not** stop!
  3. Probes slot 3 (`OCCUPIED` by `key B`). **Found!**
- When inserting a new key `key C`:
  1. Hashes to slot 1 (occupied, collision).
  2. Probes slot 2 (`DELETED`). Remembers `firstDeletedSlot = 2`.
  3. Continues probing to slot 4 (`EMPTY`).
  4. Inserts `key C` into the remembered deleted slot 2! Decrements `deletedCount`, increments `size`.
