package io.github.tidefox.clientquirks.input;

import java.util.function.IntPredicate;

/**
 * Decides whether a drop should throw the whole stack. Vanilla uses
 * {@code GuiScreen.isCtrlKeyDown()}, which is Cmd on macOS and Ctrl elsewhere.
 * Pure logic for unit tests; {@code keyDown} is the (stale-filtered) key state.
 */
public final class DropModifier {

    public static final int KEY_LCONTROL = 29;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_LMETA = 219;
    public static final int KEY_RMETA = 220;

    private DropModifier() {}

    /** Same keys as vanilla {@code GuiScreen.isCtrlKeyDown()}. */
    public static boolean vanillaCtrl(boolean mac, IntPredicate keyDown) {
        return mac
                ? keyDown.test(KEY_LMETA) || keyDown.test(KEY_RMETA)
                : keyDown.test(KEY_LCONTROL) || keyDown.test(KEY_RCONTROL);
    }

    /**
     * @param vanillaResult what the original {@code isCtrlKeyDown()} call returned
     * @param recompute     true to ignore {@code vanillaResult} and recompute it from {@code keyDown}
     *                      (used when stale-key filtering is on, so a stuck Cmd doesn't count)
     * @param physicalCtrl  macOS only: also accept the physical Ctrl key
     */
    public static boolean dropWholeStack(boolean vanillaResult, boolean mac, boolean recompute,
                                         boolean physicalCtrl, IntPredicate keyDown) {
        boolean result = recompute ? vanillaCtrl(mac, keyDown) : vanillaResult;
        if (mac && physicalCtrl && !result) {
            result = keyDown.test(KEY_LCONTROL) || keyDown.test(KEY_RCONTROL);
        }
        return result;
    }
}
