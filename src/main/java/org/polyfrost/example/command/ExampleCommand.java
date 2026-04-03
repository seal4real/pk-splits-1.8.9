package org.polyfrost.example.command;

import org.polyfrost.example.ParkourSplits;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;

/**
 * An example command implementing the Command api of OneConfig.
 * Registered in ExampleMod.java with `CommandManager.INSTANCE.registerCommand(new ExampleCommand());`
 *
 * @see Command
 * @see Main
 * @see ParkourSplits
 */
@Command(value = ParkourSplits.MODID, description = "Access the " + ParkourSplits.NAME + " GUI.")
public class ExampleCommand {
    @Main
    private void handle() {
        ParkourSplits.INSTANCE.config.openGui();
    }
}