package io.github.tidefox.clientquirks.quit;

import static io.github.tidefox.clientquirks.quit.QuitDecision.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuitDecisionTest {

    // decide(closeRequested, enabled, mac, confirmEverywhere, worldLoaded, confirmScreenOpen)

    @Test
    void noRequestDoesNothing() {
        assertEquals(IGNORE, decide(false, true, true, true, true, false));
    }

    @Test
    void macInWorldAsks() {
        assertEquals(CONFIRM, decide(true, true, true, false, true, false));
    }

    @Test
    void secondRequestWhilePromptOpenQuits() {
        assertEquals(QUIT, decide(true, true, true, false, true, true));
    }

    @Test
    void menusQuitDirectly() {
        assertEquals(QUIT, decide(true, true, true, false, false, false));
    }

    @Test
    void disabledQuits() {
        assertEquals(QUIT, decide(true, false, true, true, true, false));
    }

    @Test
    void otherOsOnlyWithConfirmEverywhere() {
        assertEquals(QUIT, decide(true, true, false, false, true, false));
        assertEquals(CONFIRM, decide(true, true, false, true, true, false));
    }
}
