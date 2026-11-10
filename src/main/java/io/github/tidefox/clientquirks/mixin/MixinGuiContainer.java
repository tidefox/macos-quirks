package io.github.tidefox.clientquirks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.tidefox.clientquirks.input.DropHooks;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

    /**
     * §1.1: drop key over a slot; {@code handleMouseClick(slot, id, isCtrlKeyDown() ? 1 : 0, THROW)}.
     * The 1.12.2 bytecode calls the inherited static through GuiContainer, not GuiScreen.
     */
    @WrapOperation(method = "keyTyped", expect = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;isCtrlKeyDown()Z"))
    private boolean cq$dropWholeStack(Operation<Boolean> original) {
        return DropHooks.dropWholeStack(original.call());
    }
}
