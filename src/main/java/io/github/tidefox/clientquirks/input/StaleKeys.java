package io.github.tidefox.clientquirks.input;

import java.util.BitSet;
import java.util.function.IntPredicate;

/**
 * Keys LWJGL still reports as held when the window regains focus are "stale": their
 * key-up may have gone to another app (Cmd+Tab / Alt+Tab). A stale key counts as up
 * until LWJGL sends a real event for it (a press or a release), or until LWJGL itself
 * reports it up.
 *
 * Pure logic, no Minecraft or LWJGL references, so it can be unit tested.
 */
public final class StaleKeys {

    public static final int KEYBOARD_SIZE = 256;

    private final BitSet stale = new BitSet(KEYBOARD_SIZE);

    /** Window regained focus: everything LWJGL says is down right now becomes suspect. */
    public synchronized int markHeldAsStale(IntPredicate rawIsKeyDown) {
        stale.clear();
        for (int key = 1; key < KEYBOARD_SIZE; key++) {
            if (rawIsKeyDown.test(key)) stale.set(key);
        }
        return stale.cardinality();
    }

    /** A real keyboard event arrived for {@code key}: from now on LWJGL's state for it is trustworthy. */
    public synchronized void onKeyEvent(int key) {
        if (key > 0 && key < KEYBOARD_SIZE) stale.clear(key);
    }

    /** {@code rawDown} filtered through the stale set. Clears entries LWJGL now reports up. */
    public synchronized boolean filter(int key, boolean rawDown) {
        if (key <= 0 || key >= KEYBOARD_SIZE) return rawDown;
        if (!rawDown) {
            stale.clear(key);
            return false;
        }
        return !stale.get(key);
    }

    public synchronized boolean isStale(int key) {
        return key > 0 && key < KEYBOARD_SIZE && stale.get(key);
    }

    public synchronized void clear() {
        stale.clear();
    }
}
