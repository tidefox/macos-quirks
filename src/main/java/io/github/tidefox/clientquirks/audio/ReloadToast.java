package io.github.tidefox.clientquirks.audio;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;

/** Same look as vanilla's SystemToast; one at a time (a new one replaces the old). */
final class ReloadToast implements IToast {

    private final String title;
    private final String subtitle;

    ReloadToast(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 64, 160, 32);
        toastGui.getMinecraft().fontRenderer.drawString(title, 18, 7, -256);
        toastGui.getMinecraft().fontRenderer.drawString(subtitle, 18, 18, -1);
        return delta < 3000L ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public Object getType() {
        return ReloadToast.class;
    }
}
