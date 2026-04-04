package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polyfrost.example.command.PksCommand;
import org.polyfrost.example.config.PksConfig;

@Mod(modid = PksMod.MODID, name = PksMod.NAME, version = PksMod.VERSION)
public class PksMod {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static PksMod INSTANCE;

    public static PksConfig config;
    public static SplitsManager manager;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new PksConfig();
        manager = new SplitsManager(config.hud);

        MinecraftForge.EVENT_BUS.register(manager);
        MinecraftForge.EVENT_BUS.register(manager.gateRenderer);

        CommandManager.INSTANCE.registerCommand(new PksCommand());
    }
}
