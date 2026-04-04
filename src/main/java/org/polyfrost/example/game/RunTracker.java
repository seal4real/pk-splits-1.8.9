package org.polyfrost.example.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunTracker {

    private final Route route;

    private boolean active = false;
    private long startTimeNanos = 0L;

    // Tracks which checkpoint indices have been hit this run
    private final Set<Integer> hitCheckpointIndices = new HashSet<>();

    // Maps checkpoint index -> elapsed ms at time of hit; LinkedHashMap preserves hit order
    private final Map<Integer, Long> checkpointSplitTimesMillis = new LinkedHashMap<>();

    // Elapsed ms when finish was crossed; null until finished
    private Long finishSplitMillis = null;

    public RunTracker(Route route) {
        this.route = route;
    }

    // Run lifecycle

    public void hitStart() {
        reset();
        active = true;
        startTimeNanos = System.nanoTime();
    }

    public void hitCheckpoint(int index) {
        if (!active || isFinished() || hitCheckpointIndices.contains(index)) return;
        long elapsed = getCurrentElapsedMillis();
        hitCheckpointIndices.add(index);
        checkpointSplitTimesMillis.put(index, elapsed);
    }

    public void hitFinish() {
        if (!canHitFinish()) return;
        finishSplitMillis = getCurrentElapsedMillis();
    }

    public void reset() {
        active = false;
        startTimeNanos = 0L;
        hitCheckpointIndices.clear();
        checkpointSplitTimesMillis.clear();
        finishSplitMillis = null;
    }

    // State queries

    public boolean isActive() {
        return active;
    }

    public boolean isFinished() {
        return finishSplitMillis != null;
    }

    public boolean isCheckpointHit(int index) {
        return hitCheckpointIndices.contains(index);
    }

    public boolean canHitFinish() {
        return active && !isFinished() && hitCheckpointIndices.size() == route.getCheckpoints().size();
    }

    public long getCurrentElapsedMillis() {
        if (!active) return 0L;
        return nanosToMillis(System.nanoTime() - startTimeNanos);
    }

    // Results

    public Map<Integer, Long> getCheckpointSplitTimesMillis() {
        return Collections.unmodifiableMap(checkpointSplitTimesMillis);
    }

    // Snapshot the current finished run as a RunResult. Only valid to call after hitFinish().
    public RunResult toResult() {
        List<Long> splits = new ArrayList<>();
        for (int i = 0; i < route.getCheckpoints().size(); i++) {
            splits.add(checkpointSplitTimesMillis.getOrDefault(i, 0L));
        }
        return new RunResult(finishSplitMillis, splits);
    }

    // Helpers

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000L;
    }

}