package org.polyfrost.example.config;

import org.polyfrost.example.ParkourSplits;
import org.polyfrost.example.hud.TestHud;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See <a href="https://docs.polyfrost.cc/oneconfig/config/adding-options">this link</a> for more config Options
 */
public class TestConfig extends Config {
    @HUD(
            name = "Example HUD"
    )
    public TestHud hud = new TestHud();

    public TestConfig() {
        super(new Mod(ParkourSplits.NAME, ModType.THIRD_PARTY), ParkourSplits.MODID + ".json");
        initialize();
    }
}

