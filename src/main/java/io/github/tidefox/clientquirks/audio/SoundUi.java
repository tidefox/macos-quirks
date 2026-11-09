package io.github.tidefox.clientquirks.audio;

import io.github.tidefox.clientquirks.CQConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

/** §1.2 manual part: an (unbound) key and a button on the Music & Sounds screen. */
public final class SoundUi {

    public static final KeyBinding RELOAD_KEY = new KeyBinding("key.clientquirks.reloadSound",
            KeyConflictContext.UNIVERSAL, Keyboard.KEY_NONE, "key.categories.clientquirks");

    private static final int BUTTON_ID = 0x43510001;

    private SoundUi() {}

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (RELOAD_KEY.isPressed()) SoundReloader.request("manual");
    }

    @SubscribeEvent
    public static void onGuiKey(GuiScreenEvent.KeyboardInputEvent.Post event) {
        if (Keyboard.getEventKeyState() && RELOAD_KEY.isActiveAndMatches(Keyboard.getEventKey())) {
            SoundReloader.request("manual");
        }
    }

    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!CQConfig.audio.button || !(event.getGui() instanceof GuiScreenOptionsSounds)) return;
        int w = 150;
        event.getButtonList().add(new GuiButton(BUTTON_ID, event.getGui().width - w - 5,
                event.getGui().height - 25, w, 20, I18n.format("clientquirks.audio.button")));
    }

    @SubscribeEvent
    public static void onAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getButton().id == BUTTON_ID && event.getGui() instanceof GuiScreenOptionsSounds) {
            SoundReloader.request("manual");
        }
    }
}
