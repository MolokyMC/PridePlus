/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.ui.client.mainmenu;

import net.ccbluex.liquidbounce.ui.cnfont.FontDrawer;
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class MainMenuSmallButton extends GuiButton {
    private float cut;
    private float alpha;
    private String image;
    public MainMenuSmallButton(final int buttonId, final int x, final int y, final int width, final int height, final String image) {
        super(buttonId, x, y, width, height, "");
        this.image = image;
    }
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            final FontDrawer fontRenderer = FontLoaders.F22;
            hovered = (mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height);

            final int delta = RenderUtils.deltaTime;

            if (enabled && hovered) {
                cut += 0.05F * delta;

                if (cut >= 4) cut = 4;

                alpha += 0.3F * delta;

                if (alpha >= 210) alpha = 210;
            } else {
                cut -= 0.05F * delta;

                if (cut <= 0) cut = 0;

                alpha -= 0.3F * delta;

                if (alpha <= 120) alpha = 120;
            }

            RenderUtils.drawImage(new ResourceLocation("wawa/menu/"+this.image+".png"), this.x, this.y, this.width, this.height, 1F, 1F, 1F, 12F);

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            mouseDragged(mc, mouseX, mouseY);

            GlStateManager.resetColor();
        }
    }
}
