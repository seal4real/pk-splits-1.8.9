package org.polyfrost.example.game;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private List<com.higoodimdad.pksplits.Gate> startGates = new ArrayList<>();
    private List<com.higoodimdad.pksplits.Gate> finishGates = new ArrayList<>();
    private List<com.higoodimdad.pksplits.Gate> checkpoints = new ArrayList<>();

    // Adding

    public void addStartGate(com.higoodimdad.pksplits.Gate gate) {
        startGates.add(gate);
    }

    public void addCheckpoint(com.higoodimdad.pksplits.Gate gate) {
        checkpoints.add(gate);
    }

    public void addFinishGate(com.higoodimdad.pksplits.Gate gate) {
        finishGates.add(gate);
    }

    // Removing

    public void removeStartAt(int index) {
        startGates.remove(index);
    }

    public void removeCheckpointAt(int index) {
        checkpoints.remove(index);
    }

    public void removeFinishAt(int index) {
        finishGates.remove(index);
    }

    // Getters

    public List<com.higoodimdad.pksplits.Gate> getStartGates() {
        return new ArrayList<>(startGates);
    }

    public List<com.higoodimdad.pksplits.Gate> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public List<com.higoodimdad.pksplits.Gate> getFinishGates() {
        return new ArrayList<>(finishGates);
    }

    // Misc

    public int size() {
        return checkpoints.size();
    }

}

