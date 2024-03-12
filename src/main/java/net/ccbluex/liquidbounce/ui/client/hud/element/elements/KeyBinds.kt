package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color


@ElementInfo(name = "KeyBinds")
class KeyBinds(x: Double = 85.11, y: Double = 21.11, scale: Float = 1F,
               side: Side = Side(Side.Horizontal.LEFT, Side.Vertical.UP)
) : Element(x, y, scale, side) {
    private val onlyState = BoolValue("OnlyModuleState", true)

    private val shadowValue = BoolValue("Shadow", false)
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val alphaValue = IntegerValue("Alpha", 103, 0, 255)
    private val bgredValue = IntegerValue("Background-Red", 255, 0, 255)
    private val bggreenValue = IntegerValue("Background-Green", 255, 0, 255)
    private val bgblueValue = IntegerValue("Background-Blue", 255, 0, 255)
    private val bgalphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private var GameInfoRows = 0
    override fun drawElement(): Border? {
        var y2 = this.GameInfoRows * 18+16+20

        RenderUtils.drawRoundRect(0F, this.GameInfoRows * 18F + 12, 176F, this.GameInfoRows * 18F + 25F,5F, Color(redValue.get(), greenValue.get(), blueValue.get(), bgalphaValue.get()).rgb)

        RenderUtils.drawRoundRect(0F,this.GameInfoRows * 18F + 30F,176F,y2.toFloat(),5F,Color(bgredValue.get(),bggreenValue.get(),bgblueValue.get(),bgalphaValue.get()).rgb)

        if (shadowValue.get()){
            RenderUtils.drawShadow2(0, this.GameInfoRows * 18 + 12, 176F, this.GameInfoRows * 18F + 25)
        }
//        if (outline.get()){
//            RenderUtils.drawGidentOutlinedRoundedRect(0.0, 0.0, 114.0,anmitY.toDouble(), 8.0,linewidth.get())
//        }



        GlStateManager.resetColor()

        //draw Title
        val fwidth = 10F

        FontLoaders.F16.drawStringWithShadow("按键显示", 7.0,
            (this.GameInfoRows * 18 + 16).toDouble(),Color(255,255,255,255).rgb)
        for (m : Module in Pride.moduleManager.modules){
            if (m.keyBind == 0) continue
            if (onlyState.get()) {
                if (!m.state) continue
            }
            FontLoaders.F16.drawStringWithShadow(m.name,
                6.0, y2.toDouble(), Color(redValue.get(),greenValue.get(),blueValue.get(),alphaValue.get()).rgb)
            FontLoaders.F16.drawStringWithShadow(if (m.state) "[开启]" else "[关闭]",
                (100 - FontLoaders.F16.getStringWidth(m.keyBind.toString())).toDouble(),
                y2.toDouble(),Color(redValue.get(),greenValue.get(),blueValue.get(),alphaValue.get()).rgb)
            y2 += 10

        }

        return Border(0F, this.GameInfoRows * 18F + 12F, 176F, 80F)
    }
}