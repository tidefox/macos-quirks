package io.github.tidefox.clientquirks.audio;

import java.util.Objects;

/**
 * Turns a stream of "current default output device" observations into reload decisions.
 * <ul>
 *   <li>The first observation (and the first after each reload) is the baseline.</li>
 *   <li>A different device must be seen continuously for {@code stableMs} before it counts.</li>
 *   <li>Never fires twice within {@code minIntervalMs}; a pending change waits instead.</li>
 *   <li>{@code null} means "can't tell right now" (sound system loading): ignored.</li>
 * </ul>
 * Pure logic with an injected clock, for unit tests. Not thread-safe; call from one thread.
 */
public final class DeviceChangeDebouncer {

    private final long stableMs;
    private final long minIntervalMs;

    private String baseline;
    private String candidate;
    private long candidateSince;
    private long lastFire = Long.MIN_VALUE / 2;

    public DeviceChangeDebouncer(long stableMs, long minIntervalMs) {
        this.stableMs = stableMs;
        this.minIntervalMs = minIntervalMs;
    }

    /** @return true if a sound reload should be scheduled now. */
    public boolean observe(String device, long nowMs) {
        if (device == null) return false;
        if (baseline == null) {
            baseline = device;
            candidate = null;
            return false;
        }
        if (device.equals(baseline)) {
            candidate = null;
            return false;
        }
        if (!device.equals(candidate)) {
            candidate = device;
            candidateSince = nowMs;
            return false;
        }
        if (nowMs - candidateSince < stableMs) return false;
        if (nowMs - lastFire < minIntervalMs) return false;
        fired(nowMs);
        return true;
    }

    /** A reload happened (ours or a manual one): re-learn the baseline from the next observation. */
    public void fired(long nowMs) {
        lastFire = nowMs;
        baseline = null;
        candidate = null;
    }

    String baseline() {
        return baseline;
    }

    @Override
    public String toString() {
        return "DeviceChangeDebouncer{baseline=" + baseline + ", candidate=" + Objects.toString(candidate) + "}";
    }
}
