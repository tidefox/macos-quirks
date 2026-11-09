package io.github.tidefox.clientquirks.input;

import io.github.tidefox.clientquirks.CQConfig;
import io.github.tidefox.clientquirks.ClientQuirks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

/**
 * §1.4: when the window regains focus, keys LWJGL still reports as held are treated as
 * released until a real key event confirms them. Also feeds the filtered key state to
 * the drop-stack fix and (via MixinKeyBinding) to vanilla's KeyBinding.updateKeyBindState.
 */
public final class KeyResync {

    public static final StaleKeys STALE = new StaleKeys();

    private static boolean wasActive = true;

    private KeyResync() {}

    /** Key state with stale keys filtered out (when the fix is on). */
    public static boolean isKeyDown(int key) {
        boolean raw = Keyboard.isKeyDown(key);
        return CQConfig.focus.resyncKeys ? STALE.filter(key, raw) : raw;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean active = Display.isCreated() && Display.isActive();
        if (active && !wasActive) onFocusRegained();
        wasActive = active;
    }

    private static void onFocusRegained() {
        if (!CQConfig.focus.resyncKeys || !Keyboard.isCreated()) {
            STALE.clear();
            return;
        }
        int n = STALE.markHeldAsStale(Keyboard::isKeyDown);
        if (n == 0) return;
        ClientQuirks.LOGGER.debug("Focus regained with {} key(s) still reported held; ignoring them until re-pressed", n);
        // Release everything, then re-press only bindings whose key is genuinely held.
        KeyBinding.unPressAllKeys();
        for (KeyBinding kb : Minecraft.getMinecraft().gameSettings.keyBindings) {
            int code = kb.getKeyCode();
            if (code > 0 && code < StaleKeys.KEYBOARD_SIZE && isKeyDown(code)) {
                KeyBinding.setKeyBindState(code, true);
            }
        }
    }

    // A real event for a key means LWJGL's state for it is current again.

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        STALE.onKeyEvent(Keyboard.getEventKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        STALE.onKeyEvent(Keyboard.getEventKey());
    }
}
