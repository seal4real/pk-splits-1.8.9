package org.polyfrost.example.game;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private List<Gate> startGates = new ArrayList<>();
    private List<Gate> finishGates = new ArrayList<>();
    private List<Gate> checkpoints = new ArrayList<>();

    // Adding

    public void addStartGate(Gate gate) {
        startGates.add(gate);
    }

    public void addCheckpoint(Gate gate) {
        checkpoints.add(gate);
    }

    public void addFinishGate(Gate gate) {
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

    public List<Gate> getStartGates() {
        return startGates;
    }

    public List<Gate> getCheckpoints() {
        return checkpoints;
    }

    public List<Gate> getFinishGates() {
        return finishGates;
    }

    // Misc

    public boolean isEmpty() {
        return (startGates.size() + finishGates.size() + checkpoints.size()) == 0;
    }

}
