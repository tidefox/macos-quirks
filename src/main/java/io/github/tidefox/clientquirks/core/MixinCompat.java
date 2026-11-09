package io.github.tidefox.clientquirks.core;

import org.spongepowered.asm.mixin.Mixins;

/** Isolates the Mixin reference so this class is only linked once we know Mixin exists. */
final class MixinCompat {

    private MixinCompat() {}

    static boolean isPresent() {
        ClassLoader cl = CQLoadingPlugin.class.getClassLoader();
        return cl != null && cl.getResource("org/spongepowered/asm/mixin/Mixins.class") != null;
    }

    static void addConfiguration(String config) {
        Mixins.addConfiguration(config);
    }
}
