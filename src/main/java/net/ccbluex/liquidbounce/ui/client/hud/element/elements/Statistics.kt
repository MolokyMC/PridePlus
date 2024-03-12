/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import me.utils.render.BlurBuffer
import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.InfosUtils.Recorder
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.hypot

@ElementInfo(name = "GameInfo")
class Statistics(
    x: Double = 3.39,
    y: Double = 24.48,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element(x, y, scale, side) {

    private val modeValue = ListValue("Mode", arrayOf("FDP", "Skid", "Tomk"), "FDP")

    private val redValue = IntegerValue("BackgroundRed", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val greenValue = IntegerValue("BackgroundGreen", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val blueValue = IntegerValue("BackgroundBlue", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val alpha = IntegerValue("BackgroundAlpha", 155, 0, 255).displayable { modeValue.get() == "Skid" }
    private val rredValue = IntegerValue("RectRed", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val rgreenValue = IntegerValue("RectGreen", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val rblueValue = IntegerValue("RectBlue", 0, 0, 255).displayable { modeValue.get() == "Skid" }
    private val ralpha = IntegerValue("RectAlpha", 192,0, 255).displayable { modeValue.get() == "Skid" }
    private val textredValue = IntegerValue("TextRed", 255, 0, 255).displayable { modeValue.get() == "Skid" }
    private val textgreenValue = IntegerValue("TextGreen", 244, 0, 255).displayable { modeValue.get() == "Skid" }
    private val textblueValue = IntegerValue("TextBlue", 255, 0, 255).displayable { modeValue.get() == "Skid" }

    private val bV = BoolValue("Tomk-Blur", true)
    private val blurStrength = FloatValue("Tomk-BlurStrength", 5f,0f,20f)
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)

    private val shadowValue = BoolValue("shadow", true)
    private val shadowStrength = FloatValue("Tomk-ShadowStrength", 5f,0f,20f).displayable { shadowValue.get() }

    private val rectR = IntegerValue("Rect-R", 0, 0, 255)
    private val rectG = IntegerValue("Rect-G", 0, 0, 255)
    private val rectB = IntegerValue("Rect-B", 0, 0, 255)
    private val rectAlpha = IntegerValue("RectAlpha", 150, 0, 255)
    private val rectShadow = BoolValue("RectShadow", false)
    
    private val textR = IntegerValue("Label-R", 255, 0, 255)
    private val textG = IntegerValue("Label-G", 255, 0, 255)
    private val textB = IntegerValue("Label-B", 255, 0, 255)
    private val textAlpha = IntegerValue("LabelAlpha", 200, 0, 255)
    
    private val infoR = IntegerValue("Info-R", 200, 0, 255)
    private val infoG = IntegerValue("Info-G", 200, 0, 255)
    private val infoB = IntegerValue("Info-B", 200, 0, 255)
    private val infoAlpha = IntegerValue("InfoAlpha", 180, 0, 255)

    //fdp
    private var minute = 0L
    private var second = 0L
    private var secondString = "0"
    val timer = MSTimer()

    //old pride plus
    private var gameInfoRows = 0
    private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")

    override fun drawElement(): Border? {

        var y1 = 0F
        var x1 = 0f

        var x = 0f
        var y = 0f

        when (modeValue.get().toLowerCase()){
            "tomk" -> {
                y1 = 1F
                x1 = -5F

                x = 125F
                y = 91F


                val x2 = 120f
                //val autoL = LiquidBounce.moduleManager.getModule(AutoL::class.java) as AutoL
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 2f + 5f,15F,15F,3f, Color(0,162,255,255))
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 4.5f + 5f,15F,15F,3f, Color(0,162,255,255))
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 7f + 5f,15F,15F,3f, Color(0,162,255,255))
                val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")
                Fonts.posterama50.drawCenteredString("Session", x2 / 2f, 5f, Color.WHITE.rgb)
                Fonts.icon80.drawString("B",7f,Fonts.posterama30.fontHeight * 2f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("Time:${DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))}", 30f, Fonts.posterama30.fontHeight * 2f + 8f, Color.WHITE.rgb)
                Fonts.icon80.drawString("F",7f,Fonts.posterama30.fontHeight * 4.5f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("Kill:" + Pride.combatManager.kill.toString(),30f, Fonts.posterama30.fontHeight * 4.5f + 8f, Color.WHITE.rgb)
                Fonts.icon80.drawString("E",7f,Fonts.posterama30.fontHeight * 7f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("FPS:"+ Minecraft.getDebugFPS(),30f, Fonts.posterama30.fontHeight * 7f + 8f, Color.WHITE.rgb)
                if (bV.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    BlurBuffer.CustomBlurRoundArea(
                        renderX.toFloat() - 5,
                        renderY.toFloat() + 1  ,
                        130F,
                        91F,
                        radiusValue.get(), blurStrength.get()
                    )
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 2f + 5f,15F,15F,3f, Color(0,162,255,255))
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 4.5f + 5f,15F,15F,3f, Color(0,162,255,255))
                RoundedUtil.drawRound(6F,Fonts.posterama30.fontHeight * 7f + 5f,15F,15F,3f, Color(0,162,255,255))
                Fonts.posterama50.drawCenteredString("Session", x2 / 2f, 5f, Color.WHITE.rgb)
                Fonts.icon80.drawString("B",7f,Fonts.posterama30.fontHeight * 2f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("Time:${DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))}", 30f, Fonts.posterama30.fontHeight * 2f + 8f, Color.WHITE.rgb)
                Fonts.icon80.drawString("F",7f,Fonts.posterama30.fontHeight * 4.5f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("Kill:" + Pride.combatManager.kill.toString(),30f, Fonts.posterama30.fontHeight * 4.5f + 8f, Color.WHITE.rgb)
                Fonts.icon80.drawString("E",7f,Fonts.posterama30.fontHeight * 7f + 9.3f,Color(0,0,0,255).rgb)
                Fonts.posterama40.drawString("FPS:"+ Minecraft.getDebugFPS(),30f, Fonts.posterama30.fontHeight * 7f + 8f, Color.WHITE.rgb)
                //shadow
                GL11.glTranslated(-renderX, -renderY, 0.0)
                GL11.glScalef(1F, 1F, 1F)
                GL11.glPushMatrix()
                if (shadowValue.get()) {
                    ShadowUtils.shadow(shadowStrength.get(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        GL11.glScalef(scale, scale, scale)

                        RenderUtils.originalRoundedRect(
                            -5f, 1F, 125F, 91F, radiusValue.get(),
                            Color(32, 30, 30).rgb
                        )
                        GL11.glPopMatrix()
                    }, {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        GL11.glScalef(scale, scale, scale)
                        GlStateManager.enableBlend()
                        GlStateManager.disableTexture2D()
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                        RenderUtils.fastRoundedRect(-5f, 1F, 125F, 91F, radiusValue.get())
                        GlStateManager.enableTexture2D()
                        GlStateManager.disableBlend()
                        GL11.glPopMatrix()
                    }
                    )
                }
                GL11.glPopMatrix()
                GL11.glScalef(scale, scale, scale)
                GL11.glTranslated(renderX, renderY, 0.0)
            }
            "fdp"->{
                if (timer.hasTimePassed(1000L)){
                    second += 1L
                    timer.reset()
                }
                if (second >= 60L){
                    second = 0L
                    minute += 1L
                }

                if (minute == 60L){
                    minute = 0L
                    second = 0L
                }

                secondString = second.toString()
                if (second < 10) {
                    secondString = "0$second"
                }

                val height = FontLoaders.F22.halfHeight + 14.0f +
                        41.0F

                y = height
                x = 140f
                y1 = 0f
                x1 = 0f

                if (rectShadow.get()) {
                    RenderUtils.drawShadow(0, 0, 140, height.toInt())
                    RenderUtils.drawShadow(-1, -1, 141, (height + 1).toInt())
                    RenderUtils.drawRect(0f, 0f, 140f, height, Color(rectR.get(), rectG.get(), rectB.get(), rectAlpha.get()).rgb)
                } else {
                    RenderUtils.drawRoundedCornerRect(0f, 0f, 140f, height, radiusValue.get(), Color(rectR.get(), rectG.get(), rectB.get(), rectAlpha.get()).rgb)
                }

                FontLoaders.F22.drawCenteredString("Statistics", (140f / 2f).toDouble(), 5.0, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
                RenderUtils.drawLine(
                    0f.toDouble(),
                    FontLoaders.F22.halfHeight + 7.0f.toDouble(),
                    140f.toDouble(),
                    FontLoaders.F22.halfHeight + 7.0f.toDouble(),
                    0.4f
                )

                GL11.glPushMatrix()
                GL11.glTranslated(0.0, FontLoaders.F22.halfHeight + 14.0f.toDouble(), 0.0)

                // Player name
                FontLoaders.F16.drawString("Player Name", 5.0F, 0.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
                FontLoaders.F16.drawString(mc.player!!.name, 135F - FontLoaders.F16.getStringWidth(mc.player!!.name), 0.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)

                // Play time
                FontLoaders.F16.drawString("Play Time", 5.0F, 10.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
                if (mc.world!!.isRemote) FontLoaders.F16.drawString("$minute:$second", 135F - FontLoaders.F16.getStringWidth("$minute:$second"), 10.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)
                else FontLoaders.F16.drawString("Singleplayer", 135F - FontLoaders.F16.getStringWidth("Singleplayer"), 10.0f, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)


                // Kills
                FontLoaders.F16.drawString("Kills", 5.0F, 20.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
                FontLoaders.F16.drawString(Pride.combatManager.kill.toString(), 135F - FontLoaders.F16.getStringWidth(Pride.combatManager.kill.toString()), 20.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)

                // Deaths
                FontLoaders.F16.drawString("Deaths", 5.0F, 30.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
                FontLoaders.F16.drawString(Pride.combatManager.death.toString(), 135F - FontLoaders.F16.getStringWidth(Pride.combatManager.death.toString()), 30.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)
                GL11.glPopMatrix()
            }
            "skid" -> {
                x = 150F
                y = 80F
                y1 = this.gameInfoRows * 18F + 12F
                x1 = 0f
                if(shadowValue.get()){
                    RenderUtils.drawShadowWithCustomAlpha(0F, 10.5F, 150F, 70F, 200F)
                }
                RenderUtils.drawRect(0F,11F,150F,12F, ColorUtils.hslRainbow(10, indexOffset = 30).rgb)
                RenderUtils.drawRect(0F, this.gameInfoRows * 18F + 12, 150F, this.gameInfoRows * 18F + 25F, Color(rredValue.get(), rgreenValue.get(), rblueValue.get(), ralpha.get()).rgb)
                RenderUtils.drawRect(0F, this.gameInfoRows * 18F + 25F, 150F, 80F, Color(redValue.get(), greenValue.get(), blueValue.get(), alpha.get()).rgb)
                Fonts.font40.drawStringWithShadow("Game Info", 5F,
                    this.gameInfoRows * 18F + 16, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
                Fonts.font35.drawStringWithShadow("BPS: " + calculateBPS(),
                    5F,
                    this.gameInfoRows * 18F + 30, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
                Fonts.font35.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), 5F,
                    this.gameInfoRows * 18F + 43, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
                Fonts.font35.drawStringWithShadow("Kills: " + Recorder.killCounts, 5F,
                    this.gameInfoRows * 18F + 54, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
                Fonts.font35.drawStringWithShadow("Played Time: ${DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))}" ,
                    5F,
                    this.gameInfoRows * 18F + 66, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
            }
        }

        return Border(x1, y1, x, y)
    }
    private fun calculateBPS(): Double {
        return if(mc.player != null) {
            val bps = hypot(
                mc.player!!.posX - mc.player!!.prevPosX,
                mc.player!!.posZ - mc.player!!.prevPosZ
            ) * (mc.timer as IMixinTimer).timerSpeed * 20
            Math.round(bps * 100.0) / 100.0
        }else{
            0.00
        }

    }
}
