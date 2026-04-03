package org.polyfrost.example.game;

import java.util.ArrayList;
import java.util.List;

public class RunResult {

    private long totalTimeMillis;
    private List<Long> checkpointSplitTimesMillis;

    // For Gson deserialization
    private RunResult() {}

    public RunResult(long totalTimeMillis, List<Long> checkpointSplitTimesMillis) {
        this.totalTimeMillis = totalTimeMillis;
        this.checkpointSplitTimesMillis = new ArrayList<>(checkpointSplitTimesMillis);
    }

    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    public List<Long> getCheckpointSplitTimesMillis() {
        return checkpointSplitTimesMillis;
    }

    public boolean isFasterThan(RunResult other) {
        return this.totalTimeMillis < other.totalTimeMillis;
    }
}