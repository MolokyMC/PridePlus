package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Translate
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

class HotbarUtil {
    val translate = Translate(0f , 0f)
    var size = 1.0f

    fun renderHotbarItem(index: Int, xPos: Float, yPos: Float, partialTicks: Float) {
        val itemStack = mc.player.inventory.mainInventory[index]
        val animation = itemStack.animationsToGo.toFloat() - partialTicks

        if (animation > 0.0f) {
            GlStateManager.pushMatrix()
            val animation2 = 1.0f + animation / 5.0f
            GlStateManager.translate((xPos + 8), (yPos + 12), 0.0f)
            GlStateManager.scale(1.0f / animation2, (animation2 + 1.0f) / 2.0f, 1.0f)
            GlStateManager.translate((-(xPos + 8)), (-(yPos + 12)), 0.0f)
        }

        RenderUtils.drawTexturedRect((xPos - 7).toInt(), (yPos - 7).toInt(), 30, 30,"hotbar",  ScaledResolution(mc));
        RenderUtils.drawTexturedRect((xPos - 7).toInt(), (yPos - 7).toInt(), 30, 30,"hotbar",  ScaledResolution(mc));

        if (animation > 0.0f) {
            GlStateManager.popMatrix()
        }

        mc.renderItem.renderItemOverlays(Fonts.font35, itemStack, xPos.toInt(), yPos.toInt())
    }
}