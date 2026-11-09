package io.github.tidefox.clientquirks.audio;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCdevice;

/**
 * Windows/Linux (and a fallback everywhere): OpenAL's default device name, plus
 * ALC_EXT_disconnect on the device the game has open. Must run on the main thread
 * so it never races the sound system's unload.
 *
 * Whether the bundled OpenAL Soft refreshes ALC_DEFAULT_DEVICE_SPECIFIER without being
 * re-initialised is unverified (see docs/spec.md §1.2); the disconnect check covers
 * devices that are unplugged either way.
 */
final class OpenAlProbe implements DeviceProbe {

    private static final int ALC_CONNECTED = 0x313;

    private final IntBuffer connected = BufferUtils.createIntBuffer(16);

    @Override
    public String current() {
        if (!AL.isCreated()) return null;
        String name = ALC10.alcGetString(null, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        ALCdevice open = AL.getDevice();
        if (open != null && ALC10.alcIsExtensionPresent(open, "ALC_EXT_disconnect")) {
            connected.clear();
            connected.put(0, 1);
            ALC10.alcGetInteger(open, ALC_CONNECTED, connected);
            if (connected.get(0) == 0) return name + "#disconnected";
        }
        return name;
    }

    @Override
    public String name() {
        return "OpenAL";
    }
}
