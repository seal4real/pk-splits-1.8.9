package org.polyfrost.example.config;

import org.polyfrost.example.ParkourSplits;
import org.polyfrost.example.hud.SplitHud;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See <a href="https://docs.polyfrost.cc/oneconfig/config/adding-options">this link</a> for more config Options
 */
public class TestConfig extends Config {
    @HUD(
            name = "Split Hud"
    )
    public SplitHud hud = new SplitHud();

    public TestConfig() {
        super(new Mod(ParkourSplits.NAME, ModType.THIRD_PARTY), ParkourSplits.MODID + ".json");
        initialize();
    }
}

