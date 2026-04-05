package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommandGroup;
import org.polyfrost.example.PksMod;

@Command(value = "pks", description = "Commands for the pk-splits mod")
public class PksCommand {

    @SubCommand(description = "Show split stats")
    private void stats() {
        PksMod.manager.stats();
    }

    @SubCommand(description = "Toggle splits on/off")
    private void toggle() {
        PksMod.manager.toggle();
    }

    @SubCommandGroup(value = "add")
    private class AddGroup {

        @SubCommand(description = "Add a start gate at your position")
        private void start() {
            PksMod.manager.addStart();
        }

        @SubCommand(description = "Add a checkpoint gate at your position")
        private void checkpoint() {
            PksMod.manager.addCheckpoint();
        }

        @SubCommand(description = "Add a finish gate at your position")
        private void finish() {
            PksMod.manager.addFinish();
        }
    }

    @SubCommandGroup(value = "remove")
    private class RemoveGroup {

        @SubCommand(description = "Remove the gate you are standing in")
        private void gate() {
            PksMod.manager.removeGateAtPlayer();
        }
    }

    @SubCommandGroup(value = "route")
    private class RouteGroup {

        @SubCommand(description = "Create a new route")
        private void add(String routeName) {
            PksMod.manager.createRoute(routeName);
        }

        @SubCommand(description = "Delete a route")
        private void remove(String routeName) {
            PksMod.manager.removeRoute(routeName);
        }

        @SubCommand(description = "Switch to a route")
        private void checkout(String routeName) {
            PksMod.manager.checkoutRoute(routeName);
        }

        @SubCommand(description = "List all routes")
        private void list() {
            PksMod.manager.listRoutes();
        }

        @SubCommand(description = "Load a route from a shared code")
        private void load(String routeName, String routeCode) {
            PksMod.manager.loadRoute(routeName, routeCode);
        }

        @SubCommand(description = "Share the current route as a code")
        private void share() {
            PksMod.manager.shareRoute();
        }
    }
}
