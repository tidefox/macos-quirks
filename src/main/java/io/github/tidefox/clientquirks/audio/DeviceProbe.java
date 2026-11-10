package io.github.tidefox.clientquirks.audio;

/** Reports an identifier for the system's current default output device, or null if unknown right now. */
interface DeviceProbe {
    String current();

    String name();
}
