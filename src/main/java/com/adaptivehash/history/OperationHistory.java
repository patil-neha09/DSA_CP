package com.adaptivehash.history;

import com.adaptivehash.statistics.OperationMetrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the sequential history of operations performed on the hash table.
 * Supports in-memory tracking and optional CSV data export/persistence.
 */
public class OperationHistory {

    private final List<OperationMetrics> history;
    private final int maxHistorySize;

    public OperationHistory() {
        this(1000);
    }

    public OperationHistory(int maxHistorySize) {
        this.history = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Records a completed operation into history.
     *
     * @param metrics the metrics from the operation
     */
    public synchronized void record(OperationMetrics metrics) {
        if (metrics == null) return;
        if (history.size() >= maxHistorySize) {
            history.remove(0);
        }
        history.add(metrics);
    }

    /**
     * Retrieves an unmodifiable view of all recorded operations.
     *
     * @return unmodifiable list of OperationMetrics
     */
    public synchronized List<OperationMetrics> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    /**
     * Retrieves the most recent operations up to the specified limit.
     *
     * @param count number of recent records to retrieve
     * @return list of recent OperationMetrics
     */
    public synchronized List<OperationMetrics> getRecent(int count) {
        int fromIndex = Math.max(0, history.size() - count);
        return Collections.unmodifiableList(new ArrayList<>(history.subList(fromIndex, history.size())));
    }

    /**
     * Exports recorded history to a CSV file.
     *
     * @param targetFile target CSV destination file
     * @return true if export succeeds, false otherwise
     */
    public synchronized boolean exportToCsv(File targetFile) {
        if (targetFile == null) {
            return false;
        }

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("Timestamp,Operation,Key,InitialHash,FinalSlot,Probes,Collisions,Success,DurationNanos");
            writer.newLine();

            for (OperationMetrics metric : history) {
                writer.write(String.format("%d,%s,%s,%d,%d,%d,%d,%b,%d",
                        metric.getTimestampMillis(),
                        metric.getOperationType(),
                        metric.getKey(),
                        metric.getInitialHash(),
                        metric.getFinalSlotIndex(),
                        metric.getProbeCount(),
                        metric.getCollisionCount(),
                        metric.isSuccessful(),
                        metric.getExecutionTimeNanos()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Clears all recorded operation history.
     */
    public synchronized void clear() {
        history.clear();
    }

    public synchronized int size() {
        return history.size();
    }
}
