package io.github.tidefox.clientquirks.input;

import io.github.tidefox.clientquirks.CQConfig;
import net.minecraft.client.Minecraft;

/** Called from the drop-key mixins in place of {@code GuiScreen.isCtrlKeyDown()}. */
public final class DropHooks {

    private DropHooks() {}

    public static boolean dropWholeStack(boolean vanillaResult) {
        return DropModifier.dropWholeStack(vanillaResult, Minecraft.IS_RUNNING_ON_MAC,
                CQConfig.focus.resyncKeys, CQConfig.drop.physicalCtrl, KeyResync::isKeyDown);
    }
}
