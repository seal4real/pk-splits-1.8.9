package org.polyfrost.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polyfrost.example.command.SplitsCommand;
import org.polyfrost.example.config.TestConfig;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import net.minecraftforge.fml.common.Mod;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * The entrypoint of the Example Mod that initializes it.
 *
 * @see Mod
 * @see InitializationEvent
 */
@Mod(modid = ParkourSplits.MODID, name = ParkourSplits.NAME, version = ParkourSplits.VERSION)
public class ParkourSplits {

    // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    @Mod.Instance(MODID)
    public static ParkourSplits INSTANCE; // Adds the instance of the mod, so we can access other variables.
    public static TestConfig config;

    // Register the config and commands.
    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new TestConfig();
        CommandManager.INSTANCE.registerCommand(new SplitsCommand());
    }
}
