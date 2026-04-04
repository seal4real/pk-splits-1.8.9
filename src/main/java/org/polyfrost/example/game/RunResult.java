package org.polyfrost.example.game;

import java.util.ArrayList;
import java.util.List;

public class RunResult {

    private int totalTimeTicks;
    private List<Integer> checkpointSplitTicks;

    // For Gson deserialization
    private RunResult() {}

    public RunResult(int totalTimeTicks, List<Integer> checkpointSplitTicks) {
        this.totalTimeTicks = totalTimeTicks;
        this.checkpointSplitTicks = new ArrayList<>(checkpointSplitTicks);
    }

    public int getTotalTimeTicks() {
        return totalTimeTicks;
    }

    public List<Integer> getCheckpointSplitTicks() {
        return checkpointSplitTicks;
    }

    public boolean isFasterThan(RunResult other) {
        return this.totalTimeTicks < other.totalTimeTicks;
    }
}
