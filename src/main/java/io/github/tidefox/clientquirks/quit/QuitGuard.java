package io.github.tidefox.clientquirks.quit;

import io.github.tidefox.clientquirks.CQConfig;
import io.github.tidefox.clientquirks.ClientQuirks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;

/** §1.3: turns a window close request (Cmd+Q, red button) into a confirm screen while a world is loaded. */
public final class QuitGuard {

    private QuitGuard() {}

    /** Wraps {@code Display.isCloseRequested()} in {@code Minecraft.runGameLoop}. Main thread. */
    public static boolean filterCloseRequest(boolean closeRequested) {
        if (!closeRequested) return false;
        Minecraft mc = Minecraft.getMinecraft();
        QuitDecision d = QuitDecision.decide(true, CQConfig.quit.confirm, Minecraft.IS_RUNNING_ON_MAC,
                CQConfig.quit.confirmEverywhere, mc.world != null, mc.currentScreen instanceof ConfirmScreen);
        if (d != QuitDecision.CONFIRM) return d == QuitDecision.QUIT;
        try {
            mc.displayGuiScreen(ConfirmScreen.create(mc.currentScreen, mc.isSingleplayer()));
            return false;
        } catch (Throwable t) {
            // Never trap the player in a game they can't quit.
            ClientQuirks.LOGGER.error("Could not show the quit confirmation; quitting normally", t);
            return true;
        }
    }

    /** Marker subclass so a second close request can recognise the prompt. */
    static final class ConfirmScreen extends GuiYesNo {

        private ConfirmScreen(GuiYesNoCallback callback, String line1, String line2, String yes, String no) {
            super(callback, line1, line2, yes, no, 0);
        }

        static ConfirmScreen create(GuiScreen previous, boolean singleplayer) {
            GuiYesNoCallback callback = (result, id) -> {
                Minecraft mc = Minecraft.getMinecraft();
                if (result) {
                    // Normal shutdown path: the world is unloaded (and saved) on the way out.
                    mc.shutdown();
                } else {
                    mc.displayGuiScreen(previous);
                }
            };
            return new ConfirmScreen(callback,
                    I18n.format("clientquirks.quit.title"),
                    I18n.format(singleplayer ? "clientquirks.quit.singleplayer" : "clientquirks.quit.multiplayer"),
                    I18n.format("clientquirks.quit.yes"),
                    I18n.format("gui.cancel"));
        }
    }
}
