package io.github.tidefox.clientquirks.audio;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * macOS: asks CoreAudio for kAudioHardwarePropertyDefaultOutputDevice through JNA
 * (shipped with 1.12.2 for the narrator). Only touched behind a try/catch: on a JNA
 * without darwin-aarch64 natives (Apple Silicon) loading fails and we fall back.
 */
final class CoreAudioProbe implements DeviceProbe {

    private static final int kAudioObjectSystemObject = 1;
    private static final int kAudioHardwarePropertyDefaultOutputDevice = fourCC("dOut");
    private static final int kAudioObjectPropertyScopeGlobal = fourCC("glob");
    private static final int kAudioObjectPropertyElementMain = 0;

    public interface CoreAudio extends Library {
        int AudioObjectGetPropertyData(int inObjectID, AudioObjectPropertyAddress inAddress,
                                       int inQualifierDataSize, Pointer inQualifierData,
                                       IntByReference ioDataSize, IntByReference outData);
    }

    public static class AudioObjectPropertyAddress extends Structure {
        public int mSelector;
        public int mScope;
        public int mElement;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("mSelector", "mScope", "mElement");
        }
    }

    private final CoreAudio lib;
    private final AudioObjectPropertyAddress address = new AudioObjectPropertyAddress();

    CoreAudioProbe() {
        lib = (CoreAudio) Native.loadLibrary("CoreAudio", CoreAudio.class);
        address.mSelector = kAudioHardwarePropertyDefaultOutputDevice;
        address.mScope = kAudioObjectPropertyScopeGlobal;
        address.mElement = kAudioObjectPropertyElementMain;
        if (current() == null) throw new IllegalStateException("CoreAudio returned no default output device");
    }

    @Override
    public String current() {
        IntByReference size = new IntByReference(4);
        IntByReference device = new IntByReference(0);
        int status = lib.AudioObjectGetPropertyData(kAudioObjectSystemObject, address, 0, null, size, device);
        if (status != 0 || device.getValue() == 0) return null;
        return "coreaudio:" + device.getValue();
    }

    @Override
    public String name() {
        return "CoreAudio";
    }

    static int fourCC(String s) {
        return (s.charAt(0) << 24) | (s.charAt(1) << 16) | (s.charAt(2) << 8) | s.charAt(3);
    }
}
