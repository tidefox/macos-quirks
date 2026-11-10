package io.github.tidefox.clientquirks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.tidefox.clientquirks.input.KeyResync;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {

    /**
     * §1.4: vanilla re-reads every key from LWJGL when you go back into the game
     * (setIngameFocus, Windows/Linux). Don't let stale keys come back pressed.
     */
    @WrapOperation(method = "updateKeyBindState", expect = 1,
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
    private static boolean cq$ignoreStaleKeys(int key, Operation<Boolean> original) {
        boolean raw = original.call(key);
        return raw && KeyResync.isKeyDown(key);
    }
}
