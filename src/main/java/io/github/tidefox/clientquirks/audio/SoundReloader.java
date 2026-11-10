package io.github.tidefox.clientquirks.audio;

import io.github.tidefox.clientquirks.CQConfig;
import io.github.tidefox.clientquirks.ClientQuirks;
import io.github.tidefox.clientquirks.mixin.SoundHandlerAccessor;
import io.github.tidefox.clientquirks.mixin.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.I18n;

/**
 * §1.2: restarts only the sound system (paulscode + OpenAL), which re-opens the
 * *current* default output device. Much cheaper than a resource reload (F3+T).
 * Music comes back by itself through MusicTicker.
 */
public final class SoundReloader {

    private static volatile int reloadCount;

    private SoundReloader() {}

    /** Number of reloads done by us so far. */
    public static int reloadCount() {
        return reloadCount;
    }

    /** Safe to call from any thread; the reload runs on the main thread. */
    public static void request(String reason) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> reloadNow(reason));
    }

    /** True once the sound system finished loading (it loads on its own thread). */
    public static boolean isLoaded() {
        SoundManager sm = soundManager();
        // Accessors are optional mixins: if one didn't apply, auto-switching just stays idle.
        return sm instanceof SoundManagerAccessor && ((SoundManagerAccessor) sm).cq$isLoaded();
    }

    static SoundManager soundManager() {
        Minecraft mc = Minecraft.getMinecraft();
        Object handler = mc.getSoundHandler();
        if (!(handler instanceof SoundHandlerAccessor)) return null;
        return ((SoundHandlerAccessor) handler).cq$getSndManager();
    }

    private static void reloadNow(String reason) {
        SoundManager sm = soundManager();
        if (sm == null) {
            ClientQuirks.LOGGER.warn("Can't reload the sound system: the SoundHandler accessor mixin was not applied");
            return;
        }
        ClientQuirks.LOGGER.info("Reloading sound system ({})", reason);
        long t = System.nanoTime();
        try {
            sm.reloadSoundSystem();
        } catch (Throwable e) {
            ClientQuirks.LOGGER.error("Sound system reload failed", e);
            return;
        }
        reloadCount++;
        AudioWatcher.onReloaded();
        ClientQuirks.LOGGER.info("Sound system reload started in {} ms (device opens in the background)",
                (System.nanoTime() - t) / 1_000_000L);
        if (CQConfig.audio.toast) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getToastGui().add(new ReloadToast(I18n.format("clientquirks.audio.reloaded"),
                    I18n.format("clientquirks.audio.reloaded." + reason)));
        }
    }
}
