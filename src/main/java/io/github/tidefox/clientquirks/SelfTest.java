package io.github.tidefox.clientquirks;

import io.github.tidefox.clientquirks.audio.SoundReloader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * CI only ({@code -Dclientquirks.selftest=true}): on the main menu, reload the sound
 * system through the same path as the button, wait until the engine is back, quit.
 * ci/smoke-client.sh checks the log lines.
 */
public final class SelfTest {

    static final boolean ENABLED = Boolean.getBoolean("clientquirks.selftest");

    private static int state; // 0 waiting for menu, 1 reload requested, 2 reload ran, 3 done
    private static int ticks;

    private SelfTest() {}

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        if (state == 0 && event.getGui() instanceof GuiMainMenu) {
            state = 1;
            ticks = 0;
            ClientQuirks.LOGGER.info("[selftest] main menu reached, sound loaded={}", SoundReloader.isLoaded());
            SoundReloader.request("manual");
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || state == 0 || state == 3) return;
        ticks++;
        boolean loaded = SoundReloader.isLoaded();
        if (state == 1 && SoundReloader.reloadCount() > 0) state = 2;
        if (state == 2 && loaded) {
            ClientQuirks.LOGGER.info("[selftest] sound reload OK after {} ticks", ticks);
            finish();
        } else if (ticks > 600) {
            ClientQuirks.LOGGER.error("[selftest] sound reload did not complete (state {}, loaded={})", state, loaded);
            finish();
        }
    }

    private static void finish() {
        state = 3;
        ClientQuirks.LOGGER.info("[selftest] done, quitting");
        Minecraft.getMinecraft().shutdown();
    }
}
