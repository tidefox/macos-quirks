package io.github.tidefox.clientquirks.input;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class StaleKeysTest {

    private static final int W = 17, Q = 16, LMETA = 219;

    @Test
    void keysHeldAtFocusRegainCountAsUpUntilAnEvent() {
        Set<Integer> held = new HashSet<>();
        held.add(LMETA);
        held.add(W);
        StaleKeys s = new StaleKeys();
        assertEquals(2, s.markHeldAsStale(held::contains));

        assertFalse(s.filter(LMETA, true), "stuck Cmd is ignored");
        assertFalse(s.filter(W, true), "stuck W is ignored");
        assertTrue(s.filter(Q, true), "keys pressed after focus are real");

        s.onKeyEvent(LMETA); // user really presses Cmd again
        assertTrue(s.filter(LMETA, true));
        assertFalse(s.filter(W, true));
    }

    @Test
    void lwjglReportingUpClearsTheEntry() {
        StaleKeys s = new StaleKeys();
        s.markHeldAsStale(k -> k == W);
        assertFalse(s.filter(W, false));
        assertFalse(s.isStale(W));
        assertTrue(s.filter(W, true), "once seen up, a later down is real");
    }

    @Test
    void newFocusRegainReplacesOldSet() {
        StaleKeys s = new StaleKeys();
        s.markHeldAsStale(k -> k == W);
        s.markHeldAsStale(k -> k == Q);
        assertFalse(s.isStale(W));
        assertTrue(s.isStale(Q));
    }

    @Test
    void outOfRangeKeysPassThrough() {
        StaleKeys s = new StaleKeys();
        s.markHeldAsStale(k -> true);
        assertTrue(s.filter(0, true));
        assertTrue(s.filter(300, true));
        s.onKeyEvent(-1); // no exception
    }
}
