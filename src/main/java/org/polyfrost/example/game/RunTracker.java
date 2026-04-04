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
    private int elapsedTicks = 0;

    // Tracks which checkpoint indices have been hit this run
    private final Set<Integer> hitCheckpointIndices = new HashSet<>();

    // Maps checkpoint index -> elapsed ticks at time of hit; LinkedHashMap preserves hit order
    private final Map<Integer, Integer> checkpointSplitTicks = new LinkedHashMap<>();

    // Elapsed ticks when finish was crossed; null until finished
    private Integer finishSplitTicks = null;

    public RunTracker(Route route) {
        this.route = route;
    }

    // Run lifecycle

    public void tick() {
        if (active && !isFinished()) {
            elapsedTicks++;
        }
    }

    public void hitStart() {
        reset();
        active = true;
    }

    public void hitCheckpoint(int index) {
        if (!active || isFinished() || hitCheckpointIndices.contains(index)) return;
        hitCheckpointIndices.add(index);
        checkpointSplitTicks.put(index, elapsedTicks);
    }

    public void hitFinish() {
        if (!canHitFinish()) return;
        finishSplitTicks = elapsedTicks;
    }

    public void reset() {
        active = false;
        elapsedTicks = 0;
        hitCheckpointIndices.clear();
        checkpointSplitTicks.clear();
        finishSplitTicks = null;
    }

    // State queries

    public boolean isActive() {
        return active;
    }

    public boolean isFinished() {
        return finishSplitTicks != null;
    }

    public boolean isCheckpointHit(int index) {
        return hitCheckpointIndices.contains(index);
    }

    public boolean canHitFinish() {
        return active && !isFinished() && hitCheckpointIndices.size() == route.getCheckpoints().size();
    }

    public int getCurrentElapsedTicks() {
        return elapsedTicks;
    }

    // Results

    public Map<Integer, Integer> getCheckpointSplitTicks() {
        return Collections.unmodifiableMap(checkpointSplitTicks);
    }

    // Snapshot the current finished run as a RunResult. Only valid to call after hitFinish().
    public RunResult toResult() {
        List<Integer> splits = new ArrayList<>();
        for (int i = 0; i < route.getCheckpoints().size(); i++) {
            splits.add(checkpointSplitTicks.getOrDefault(i, 0));
        }
        return new RunResult(finishSplitTicks, splits);
    }

}