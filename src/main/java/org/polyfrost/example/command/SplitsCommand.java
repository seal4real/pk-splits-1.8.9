package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommandGroup;
import cc.polyfrost.oneconfig.libs.universal.UChat;

@Command(value = "splits", description = "Manage split points and split HUD")
public class SplitsCommand {

    @SubCommand(description = "Show split stats")
    private void stats() {
        UChat.chat("Called /splits stats");
    }

    @SubCommand(description = "Toggle splits display")
    private void toggle() {
        UChat.chat("Called /splits toggle");
    }

    @SubCommandGroup(value = "add")
    private class AddGroup {

        @SubCommand(description = "Add a start gate")
        private void start() {
            UChat.chat("Called /splits add start");
        }

        @SubCommand(description = "Add a checkpoint gate")
        private void checkpoint() {
            UChat.chat("Called /splits add checkpoint");
        }

        @SubCommand(description = "Add a finish gate")
        private void finish() {
            UChat.chat("Called /splits add finish");
        }
    }

    @SubCommandGroup(value = "remove")
    private class RemoveGroup {

        @SubCommand(description = "Remove the gate at the player position")
        private void gate() {
            UChat.chat("Called /splits remove gate");
        }
    }
}