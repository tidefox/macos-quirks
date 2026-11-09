package io.github.tidefox.clientquirks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.tidefox.clientquirks.input.DropHooks;
import io.github.tidefox.clientquirks.quit.QuitGuard;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /** §1.1: in-world drop key; {@code player.dropItem(GuiScreen.isCtrlKeyDown())}. */
    @WrapOperation(method = "processKeyBinds", expect = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;isCtrlKeyDown()Z"))
    private boolean cq$dropWholeStack(Operation<Boolean> original) {
        return DropHooks.dropWholeStack(original.call());
    }

    /** §1.3: {@code if (Display.isCreated() && Display.isCloseRequested()) shutdown();} */
    @WrapOperation(method = "runGameLoop", expect = 1,
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;isCloseRequested()Z", remap = false))
    private boolean cq$confirmQuit(Operation<Boolean> original) {
        return QuitGuard.filterCloseRequest(original.call());
    }
}
