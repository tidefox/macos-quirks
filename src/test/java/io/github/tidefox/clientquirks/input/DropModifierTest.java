package io.github.tidefox.clientquirks.input;

import static io.github.tidefox.clientquirks.input.DropModifier.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;

class DropModifierTest {

    private static IntPredicate down(int... keys) {
        return k -> {
            for (int x : keys) if (x == k) return true;
            return false;
        };
    }

    @Test
    void macPhysicalCtrlDropsStack() {
        assertTrue(dropWholeStack(false, true, false, true, down(KEY_LCONTROL)));
        assertTrue(dropWholeStack(false, true, false, true, down(KEY_RCONTROL)));
    }

    @Test
    void macPhysicalCtrlIgnoredWhenToggleOff() {
        assertFalse(dropWholeStack(false, true, false, false, down(KEY_LCONTROL)));
    }

    @Test
    void macCmdStillWorks() {
        assertTrue(dropWholeStack(true, true, false, true, down(KEY_LMETA)));
        assertTrue(dropWholeStack(false, true, true, true, down(KEY_RMETA)));
    }

    @Test
    void nonMacUnchanged() {
        assertFalse(dropWholeStack(false, false, false, true, down(KEY_LMETA)));
        assertTrue(dropWholeStack(true, false, false, true, down(KEY_LCONTROL)));
        assertTrue(dropWholeStack(false, false, true, true, down(KEY_RCONTROL)));
    }

    @Test
    void recomputeOverridesStaleVanillaResult() {
        // vanilla saw a stuck Cmd (true), but the filtered state says nothing is held
        assertFalse(dropWholeStack(true, true, true, true, down()));
        assertTrue(dropWholeStack(true, true, false, true, down()), "without recompute vanilla wins");
    }

    @Test
    void vanillaCtrlMatchesGuiScreen() {
        assertTrue(vanillaCtrl(true, down(KEY_LMETA)));
        assertFalse(vanillaCtrl(true, down(KEY_LCONTROL)));
        assertTrue(vanillaCtrl(false, down(KEY_LCONTROL)));
        assertFalse(vanillaCtrl(false, down(KEY_LMETA)));
    }
}
