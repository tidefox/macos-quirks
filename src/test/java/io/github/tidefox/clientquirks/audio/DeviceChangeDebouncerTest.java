package io.github.tidefox.clientquirks.audio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DeviceChangeDebouncerTest {

    private final DeviceChangeDebouncer d = new DeviceChangeDebouncer(1000, 5000);

    @Test
    void firstObservationIsBaseline() {
        assertFalse(d.observe("speakers", 0));
        assertFalse(d.observe("speakers", 2000));
    }

    @Test
    void changeMustBeStableBeforeFiring() {
        d.observe("speakers", 0);
        assertFalse(d.observe("airpods", 10_000), "first sighting only starts the timer");
        assertFalse(d.observe("airpods", 10_500));
        assertTrue(d.observe("airpods", 11_000));
    }

    @Test
    void flappingBackToBaselineCancels() {
        d.observe("speakers", 0);
        d.observe("airpods", 10_000);
        assertFalse(d.observe("speakers", 10_500));
        assertFalse(d.observe("airpods", 12_000));
        assertTrue(d.observe("airpods", 13_000));
    }

    @Test
    void rateLimitedButNotLost() {
        d.observe("a", 0);
        d.observe("b", 10_000);
        assertTrue(d.observe("b", 11_000));
        d.observe("b", 12_000); // new baseline after reload
        d.observe("c", 13_000);
        assertFalse(d.observe("c", 14_500), "within 5 s of the last reload");
        assertTrue(d.observe("c", 16_000), "fires once the interval has passed");
    }

    @Test
    void nullsAreIgnored() {
        d.observe("a", 0);
        assertFalse(d.observe(null, 10_000));
        d.observe("b", 10_000);
        assertFalse(d.observe(null, 10_500));
        assertTrue(d.observe("b", 11_000));
    }

    @Test
    void externalReloadResetsBaseline() {
        d.observe("a", 0);
        d.fired(10_000);
        assertNull(d.baseline());
        assertFalse(d.observe("b", 20_000), "b becomes the new baseline");
        assertEquals("b", d.baseline());
    }
}
