package io.github.tidefox.clientquirks.audio;

import io.github.tidefox.clientquirks.CQConfig;
import io.github.tidefox.clientquirks.ClientQuirks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * §1.2 automatic part: checks the default output device every {@value #POLL_TICKS} client
 * ticks (2 s) and asks for a sound reload when it changes. Runs on the main thread (client
 * tick) rather than a daemon thread so the OpenAL checks can never race a sound unload.
 */
public final class AudioWatcher {

    static final int POLL_TICKS = 40;
    private static final long STABLE_MS = 1000L;
    private static final long MIN_INTERVAL_MS = 5000L;

    private static final DeviceChangeDebouncer DEBOUNCER = new DeviceChangeDebouncer(STABLE_MS, MIN_INTERVAL_MS);
    private static DeviceProbe probe;
    private static boolean probeFailed;
    private static int ticks;

    private AudioWatcher() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !CQConfig.audio.autoSwitch || probeFailed) return;
        if (++ticks < POLL_TICKS) return;
        ticks = 0;
        // Skip while the game (or a previous reload) is still loading the sound system.
        if (!SoundReloader.isLoaded()) return;
        DeviceProbe p = probe();
        if (p == null) return;
        String device;
        try {
            device = p.current();
        } catch (Throwable t) {
            ClientQuirks.LOGGER.warn("{} device check failed; automatic sound device switching is off. "
                    + "Use the 'Reload sound device' key or button.", p.name(), t);
            probeFailed = true;
            return;
        }
        if (CQConfig.audio.debug) ClientQuirks.LOGGER.info("[audio] default output = {}", device);
        if (DEBOUNCER.observe(device, Minecraft.getSystemTime())) {
            ClientQuirks.LOGGER.info("Default sound output changed to {}", device);
            SoundReloader.request("auto");
        }
    }

    /** Any reload (ours, manual, or none of our business) restarts the baseline. */
    static void onReloaded() {
        DEBOUNCER.fired(Minecraft.getSystemTime());
    }

    private static DeviceProbe probe() {
        if (probe != null) return probe;
        if (Minecraft.IS_RUNNING_ON_MAC) {
            try {
                probe = new CoreAudioProbe();
            } catch (Throwable t) {
                ClientQuirks.LOGGER.warn("CoreAudio is not reachable through JNA ({}); falling back to OpenAL checks, "
                        + "which may miss some device switches on macOS", t.toString());
            }
        }
        if (probe == null) probe = new OpenAlProbe();
        ClientQuirks.LOGGER.info("Automatic sound device switching uses {}", probe.name());
        return probe;
    }
}
