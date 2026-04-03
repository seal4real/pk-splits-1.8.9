package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommandGroup;
import org.polyfrost.example.ParkourSplits;

@Command(value = "splits", description = "For the Parkour Splits mod")
public class SplitsCommand {

    @SubCommand(description = "Show split stats")
    private void stats() {
        ParkourSplits.manager.stats();
    }

    @SubCommand(description = "Toggle splits on/off")
    private void toggle() {
        ParkourSplits.manager.toggle();
    }

    @SubCommandGroup(value = "add")
    private class AddGroup {

        @SubCommand(description = "Add a start gate at your position")
        private void start() {
            ParkourSplits.manager.addStart();
        }

        @SubCommand(description = "Add a checkpoint gate at your position")
        private void checkpoint() {
            ParkourSplits.manager.addCheckpoint();
        }

        @SubCommand(description = "Add a finish gate at your position")
        private void finish() {
            ParkourSplits.manager.addFinish();
        }
    }

    @SubCommandGroup(value = "remove")
    private class RemoveGroup {

        @SubCommand(description = "Remove the gate you are standing in")
        private void gate() {
            ParkourSplits.manager.removeGateAtPlayer();
        }
    }
}
