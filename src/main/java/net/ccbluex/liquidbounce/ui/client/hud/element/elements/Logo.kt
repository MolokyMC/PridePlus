/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.Pride.CLIENT_NAME
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color


/**
 * CustomHUD image element
 *
 * Draw custom image
 */
@ElementInfo(name = "Logo")
class Logo(x: Double = 0.0, y: Double = 0.0, scale: Float = 1.00F,
           side: Side = Side.default()) : Element(x, y, scale, side) {



    private val modeValue = ListValue("Mode", arrayOf("Image", "Text"),"Text")
    private var width = 256
    private var height = 256

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        if (modeValue.get() == "Image") {
            RenderUtils.drawImage("pride/big.png", 0, 0, width, height)
            return Border(0F, 0F, width.toFloat(), height.toFloat())
        }
        ShadowUtils.shadow(5f, {
            GL11.glPushMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
            GL11.glScalef(scale, scale, scale)

            RenderUtils.originalRoundedRect(
                0f, 0f, Fonts.posterama100.getStringWidth(CLIENT_NAME).toFloat(), Fonts.posterama100.fontHeight.toFloat(), 6.0f,
                Color(40,250,220).rgb
            )
            GL11.glPopMatrix()
        }, {
            GL11.glPushMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
            GL11.glScalef(scale, scale, scale)
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            RenderUtils.fastRoundedRect(-5f, 1F, Fonts.posterama100.getStringWidth(CLIENT_NAME).toFloat(), Fonts.posterama100.fontHeight.toFloat(), 6.0f)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        }
        )
        Fonts.posterama100.drawString(CLIENT_NAME, 0, 0, Color(40,250,220).rgb)

        return Border(0F, 0F, Fonts.posterama100.getStringWidth(CLIENT_NAME).toFloat(), Fonts.posterama100.fontHeight.toFloat())
    }

}