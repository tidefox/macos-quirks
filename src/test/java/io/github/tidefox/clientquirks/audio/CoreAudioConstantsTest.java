package io.github.tidefox.clientquirks.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CoreAudioConstantsTest {
    @Test
    void fourCharCodesMatchAppleHeaders() {
        assertEquals(0x644F7574, CoreAudioProbe.fourCC("dOut")); // kAudioHardwarePropertyDefaultOutputDevice
        assertEquals(0x676C6F62, CoreAudioProbe.fourCC("glob")); // kAudioObjectPropertyScopeGlobal
    }
}
