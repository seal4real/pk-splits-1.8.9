package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polyfrost.example.command.SplitsCommand;
import org.polyfrost.example.config.SplitsConfig;

@Mod(modid = ParkourSplits.MODID, name = ParkourSplits.NAME, version = ParkourSplits.VERSION)
public class ParkourSplits {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static ParkourSplits INSTANCE;

    public static SplitsConfig config;
    public static SplitsManager manager;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new SplitsConfig();
        manager = new SplitsManager(config.hud);

        MinecraftForge.EVENT_BUS.register(manager);
        MinecraftForge.EVENT_BUS.register(manager.gateRenderer);

        CommandManager.INSTANCE.registerCommand(new SplitsCommand());
    }
}
