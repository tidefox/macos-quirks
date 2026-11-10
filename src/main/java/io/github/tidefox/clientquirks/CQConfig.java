package io.github.tidefox.clientquirks;

import net.minecraftforge.common.config.Config;

/** config/clientquirks.cfg. One category per fix; editable in-game from the Mods list. */
@Config(modid = Tags.MOD_ID)
@Config.LangKey("clientquirks.config.title")
public final class CQConfig {

    private CQConfig() {}

    @Config.Comment("Ctrl+Q drops the whole stack on macOS (vanilla only accepts Cmd+Q there).")
    @Config.LangKey("clientquirks.config.drop")
    public static final Drop drop = new Drop();

    @Config.Comment("Move sound to the new output device (headphones, AirPods, HDMI) without restarting.")
    @Config.LangKey("clientquirks.config.audio")
    public static final Audio audio = new Audio();

    @Config.Comment("Ask before quitting when Cmd+Q or the window's close button is used in a world.")
    @Config.LangKey("clientquirks.config.quit")
    public static final Quit quit = new Quit();

    @Config.Comment("Release keys that look stuck after switching away from the game (Cmd+Tab / Alt+Tab).")
    @Config.LangKey("clientquirks.config.focus")
    public static final Focus focus = new Focus();

    public static final class Drop {
        @Config.Comment("macOS: the physical Ctrl key works like Cmd for 'drop whole stack', in the world and in inventories. "
                + "No effect on other systems.")
        @Config.LangKey("clientquirks.config.drop.physicalCtrl")
        public boolean physicalCtrl = true;
    }

    public static final class Audio {
        @Config.Comment("Show a 'Reload sound device' button on the Music & Sounds screen.")
        @Config.LangKey("clientquirks.config.audio.button")
        public boolean button = true;

        @Config.Comment("Reload the sound system automatically when the system's default output device changes.")
        @Config.LangKey("clientquirks.config.audio.autoSwitch")
        public boolean autoSwitch = true;

        @Config.Comment("Show a toast when the sound device is reloaded.")
        @Config.LangKey("clientquirks.config.audio.toast")
        public boolean toast = true;

        @Config.Comment("Log every default-device check (for bug reports).")
        @Config.LangKey("clientquirks.config.audio.debug")
        public boolean debug = false;
    }

    public static final class Quit {
        @Config.Comment("macOS: ask for confirmation before quitting while a world is loaded.")
        @Config.LangKey("clientquirks.config.quit.confirm")
        public boolean confirm = true;

        @Config.Comment("Also ask on Windows and Linux (window close button / Alt+F4). Needs 'confirm'.")
        @Config.LangKey("clientquirks.config.quit.confirmEverywhere")
        public boolean confirmEverywhere = false;
    }

    public static final class Focus {
        @Config.Comment("When the window regains focus, keys that still look held are ignored until they are pressed "
                + "or released again. Stops walking on by itself and Q dropping whole stacks after Cmd+Tab.")
        @Config.LangKey("clientquirks.config.focus.resyncKeys")
        public boolean resyncKeys = true;
    }
}
