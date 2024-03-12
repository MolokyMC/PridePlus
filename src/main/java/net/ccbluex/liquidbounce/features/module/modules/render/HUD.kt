/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import com.mojang.realmsclient.gui.ChatFormatting
import me.utils.render.VisualUtils
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.cnfont.FontDrawer
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.MobEffects
import net.minecraft.util.ResourceLocation
import op.wawa.utils.animation.AnimationUtil
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.RENDER, array = false)
class HUD : Module() {
    val hotbar = BoolValue("Hotbar", true)
    companion object {
        @JvmStatic
        val hotbarModeSection = ListValue("HotbarMode", arrayOf("PridePlus", "Pride", "LiquidBounce", "Off"), "Pride")
    }
    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", false)
    val fontChatValue = BoolValue("FontChat", false)

    val logValue = ListValue("LogMode", arrayOf("idk", "141Sense", "Jello", "PowerX", "None", "Novoline"), "None")
    val shadowValue = ListValue("TextShadowMode", arrayOf("Good", "Long", "D1ck"), "Good")
    val ClientName = TextValue("ClientName", "PridePlus")
    val DevName = TextValue("DevName", "WaWa")

    val radius = FloatValue("Radius",15F,0F,100F)

    val textRedValue = IntegerValue("Text-Red", 255, 0, 255)
    val textGreenValue = IntegerValue("Text-Green", 255, 0, 255)
    val textBlueValue = IntegerValue("Text-Blue", 255, 0, 255)

    val redValue = IntegerValue("NovolineRed", 255, 0, 255)
    val greenValue = IntegerValue("NovolineGreen", 255, 0, 255)
    val blueValue = IntegerValue("NovolineBlue", 255, 0, 255)
    @JvmField
    val domainValue = TextValue("Scoreboard-Domain", "PridePlus-2K23")
    val hueInterpolation = BoolValue("DoubleColor-Interpolate", false)
    val liteInfo = BoolValue("LiteInfo", true)
    val rainbowStart = FloatValue("RainbowStart", 0.41f, 0f, 1f)
    val rainbowStop = FloatValue("RainbowStop", 0.58f, 0f, 1f)
    val customColor = Color(redValue.get(), greenValue.get(), blueValue.get())
    val customTextColor = Color(textRedValue.get(), textGreenValue.get(), textBlueValue.get()).rgb

    private val decimalFormat:DecimalFormat = DecimalFormat()
    private var easingHealth = 0f
    private var easingFood = 0f
    private var easingaromor = 0f
    private var easingExp= 0f

    private var hotBarX = 0F

    private fun mixColors(color1: Color, color2: Color): Color {
        return ColorUtil.interpolateColorsBackAndForth(
            15,
            1,
            color1,
            color2,
            hueInterpolation.get()
        )
    }


    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        val sr = ScaledResolution(mc)
        val height = sr.scaledHeight
        val width = sr.scaledWidth

        if (liteInfo.get()){
            val font = Fonts.posterama35
            font.drawStringWithShadow("XYZ: ${mc.player.posX.toInt()}, ${mc.player.posY.toInt()}, ${mc.player.posZ.toInt()}", 3F, height - (3F+font.FONT_HEIGHT), customTextColor)
            font.drawStringWithShadow("BPS: ${String.format("%.2f", MovementUtils.bps)}", 3F, height - (3F+font.FONT_HEIGHT)*2, customTextColor)
            font.drawStringWithShadow("FPS: ${Minecraft.getDebugFPS()}", 3F, height - (3F+font.FONT_HEIGHT)*3, customTextColor)
        }

        val left: Int = width / 2 + 91
        val top: Int = height - 50
        val x = left - 8 - 180

        if (!this.hotbar.get() && mc.player != null) {
            var color = Color(252, 83, 86)
            if (this.easingHealth <= 0.0f)
                this.easingHealth = 0.0f

            if (this.easingHealth >= 20.0f)
                this.easingHealth = 20.0f

            if (this.easingFood <= 0.0f)
                this.easingFood = 0.0f

            if (this.easingFood >= 20.0f)
                this.easingFood = 20.0f



            if (mc.player.isPotionActive(MobEffects.REGENERATION))
                color = Color(244, 143, 177)


            RoundedUtil.drawRound(x.toFloat()+50, top+ 3F, 0F,0F,  3F,Color(255, 255, 255,0))

            RoundedUtil.drawRound(x.toFloat(), top.toFloat(), 90.0f, 6.0f,  3F,Color(20, 23, 22))
            RoundedUtil.drawRound(x.toFloat(), top.toFloat(), this.easingHealth / mc.player.maxHealth * 90.0f, 6.0f,3F,  color)
            var fontRenderer: FontDrawer = FontLoaders.F16
            val stringBuilder = StringBuilder()
            val decimalFormat: DecimalFormat = this.decimalFormat

            fontRenderer.drawString(
                stringBuilder.append(decimalFormat.format(java.lang.Float.valueOf(this.easingHealth / mc.player.maxHealth * 100f))).append("%").toString(),
                x.toFloat() + 1.0f, (top + 3 - fontRenderer.height / 2).toFloat(), -1)

            RoundedUtil.drawRound(x.toFloat(), top.toFloat() - 3.0f - 15.0f, 90.0f, 6.0f, 3F,Color(20, 23, 22))
            RoundedUtil.drawRound(
                x.toFloat(),
                top.toFloat() - 3.0f - 15.0f,
                this.easingFood / 20.0f * 90.0f,
                6.0f,
                3F,
                Color(255, 235, 100)
            )

            fontRenderer.drawString(this.decimalFormat.format(java.lang.Float.valueOf(this.easingFood / 20.0f * 100f)) + "%",
                x.toFloat() + 2.0f, (top + 3 - fontRenderer.height / 2).toFloat() - 3.0f - 15.0f, -1, true)

            RoundedUtil.drawRound(x.toFloat() + 10F + 90F,top.toFloat(),90F,6f,3F, Color(20, 23, 22))
            RoundedUtil.drawRound(
                x.toFloat() + 10F + 90F,
                top.toFloat(),
                this.easingaromor / 20.0f * 90.0f,
                6.0f,
                3F,
                Color(10, 100, 255)
            )

            fontRenderer.drawString(this.decimalFormat.format(java.lang.Float.valueOf(this.easingaromor / 20.0f * 100f)) + "%",
                x.toFloat() + 12.0f  + 90F, (top + 3 - fontRenderer.height / 2).toFloat(), -1, true)
            RoundedUtil.drawRound(x.toFloat() + 10F + 90F,top.toFloat() - 3.0f - 15.0f, 90F,6F,3F,Color(20, 23, 22))
            RoundedUtil.drawRound(
                x.toFloat() + 10F + 90F,
                top.toFloat()  - 3.0f - 15.0f ,
                this.easingExp  * 90.0f,
                6.0f,
                3F,
                Color(60, 255, 10)
            )
            fontRenderer.drawString(
                this.decimalFormat.format(java.lang.Float.valueOf(mc.player.experienceLevel.toFloat())) + "EXP",
                x.toFloat() + 12.0f  + 90F, (top + 3 - fontRenderer.height / 2) - 3.0f - 15.0f, -1, true)

            // Health
            this.easingHealth += (mc.player!!.health - this.easingHealth) / 2.0.pow(7.0)
                .toFloat() * RenderUtils.deltaTime.toFloat()

            // Food
            this.easingFood += (mc.player.foodStats.foodLevel.toFloat() - this.easingFood) / 2.0.pow(7.0)
                .toFloat() * RenderUtils.deltaTime.toFloat()

            // Armor
            this.easingaromor += (mc.player.totalArmorValue.toFloat() - this.easingaromor) / 2.0.pow(7.0)
                .toFloat() * RenderUtils.deltaTime.toFloat()

            // EXP
            this.easingExp += (mc.player.experience - this.easingExp) / 2.0.pow(7.0)
                .toFloat() * RenderUtils.deltaTime.toFloat()
        }

        when (logValue.get().toLowerCase()) {

            "novoline" -> {
                val time = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)
                Fonts.font35.drawString(
                        ClientName.get() + ChatFormatting.GRAY + " (" + ChatFormatting.WHITE + time + ChatFormatting.GRAY + ")",
                        3f,
                        4f,
                        customColor.rgb,
                        true
                )
            }

            "idk" -> {
                Fonts.fontSFUI35.drawStringWithShadow(ClientName.get() + "#0810", 7F, 10F,   Color(255, 255, 255, 245).rgb)
            }
            "powerx" ->{
                Gui.drawRect(2, 1, 78, 18, Color(10, 10, 10, 180).rgb)
                Gui.drawRect(2, 1, 4, 18, Color(240, 240, 240, 245).rgb)
                Fonts.fontSFUI56.drawStringWithShadow(ClientName.get(), 7F, 3F,   Color(255, 255, 255, 245).rgb)
            }
            "jello" ->{
                Fonts.fontSFUI120 .drawString(ClientName.get(), 10.0f, 10f, VisualUtils.reAlpha(Colors.WHITE.c, 0.75f))
            }
            "141sense" ->{
                RenderUtils.drawRect(3.0f, 3.0f, 142.0f, 63.0f, Color(255, 255, 255, 120).rgb)
                Fonts.font80.drawCenteredString(ClientName.get(), 71.0f, 7.0f, Color(0, 0, 0, 180).rgb)
                Fonts.font35.drawCenteredString("by" + DevName.get() , 71.0f, 25.0f, Color(0, 0, 0, 180).rgb)
                Fonts.font80.drawString("_______________", 6.0f, 19.0f, Color(0, 0, 0, 180).rgb)
                Fonts.font35.drawString("UserName:" + mc.player!!.name, 45.0f - Fonts.font35.getStringWidth("UserName:" + mc.player!!.name).toFloat() / 2.0f + 28.0f, 40.0f, Color(0, 0, 0, 180).rgb)
                Fonts.font35.drawString("FPS:" + Minecraft.getDebugFPS(), 45.0f - Fonts.font35.getStringWidth("FPS:" + Minecraft.getDebugFPS()).toFloat() / 2.0f + 28.0f, 52.0f, Color(0, 0, 0, 180).rgb)
            }
        }
        if (mc.currentScreen is GuiHudDesigner)
            return

        Pride.hud.render(false)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        Pride.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        Pride.hud.handleKey('a', event.key)
    }

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.world == null || mc.player == null) return
        if (state && blurValue.get() && !mc.entityRenderer.isShaderActive() && event.guiScreen != null &&
                !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) mc.entityRenderer.loadShader(
            ResourceLocation("pride/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
                mc.entityRenderer.shaderGroup.shaderGroupName.contains("pride/blur.json")) mc.entityRenderer.stopUseShader()
    }
    fun getAnimPos(pos: Float): Float {
        hotBarX = if (state && hotbarModeSection.get() == "Pride") AnimationUtil.animate(
            pos,
            hotBarX,
            0.02F * RenderUtils.deltaTime.toFloat()
        )
        else pos

        return hotBarX
    }

    init {
        state = true
    }
}