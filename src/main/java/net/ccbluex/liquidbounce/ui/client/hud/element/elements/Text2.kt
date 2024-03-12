package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import me.utils.render.BlurBuffer
import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.render.Palette
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.sqrt

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "Text2")
class Text2(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F,
            side: Side = Side.default()) : Element(x, y, scale, side) {
    companion object {

        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        val HOUR_FORMAT = SimpleDateFormat("HH:mm")
        val DECIMAL_FORMAT = DecimalFormat("0.00")
        val DECIMAL_FORMAT_INT = DecimalFormat("0")

        /**
         * Create default element
         */
        fun defaultClient(): Text2 {
            val text = Text2(x = 2.0, y = 2.0, scale = 2F)

            text.displayString.set( Pride.CLIENT_NAME + " |" + "Fps:%fps% ")
            text.shadow.set(true)
            text.fontValue.set(Fonts.font40)
            text.setColor(Color(0, 111, 255))

            return text
        }

    }
    private val shadows = BoolValue("Text-shadow-bug+", false)
    val shadowStrength = FloatValue("Text-Shadow-Strength-bug+", 1F, 0.01F, 20F)
    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "Rainbow", "Fade", "Astolfo", "NewRainbow","Gident"), "Custom")
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val displayString = TextValue("DisplayText", "")
    private val redValue = IntegerValue("Text-R", 255, 0, 255)
    private val greenValue = IntegerValue("Text-G", 255, 0, 255)
    private val blueValue = IntegerValue("Text-B", 255, 0, 255)
    private val colorRedValue2 = IntegerValue("Text-R2", 0, 0, 255)
    private val colorGreenValue2 = IntegerValue("Text-G2", 111, 0, 255)
    private val colorBlueValue2 = IntegerValue("Text-B2", 255, 0, 255)
    private val Mode = ListValue("Border-Mode", arrayOf("Slide", "Skeet","Top", "Onetap"), "Onetap")
    private val blurValuee = BoolValue("Top-Blur", true)
    private val BlurStrength = FloatValue("BlurStrength", 5f,0f,20f)
    val shadowValueopen = BoolValue("shadow", true)
    private val shadowValue = FloatValue("shadow-Value", 10F, 0f, 20f)
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom"), "Background")
    private val r = IntegerValue("shadow-Red", 0, 0, 255)
    private val g = IntegerValue("shadow-Green", 0, 0, 255)
    private val b = IntegerValue("shadow-Blue", 255, 0, 255)
    private val shadowalpha = IntegerValue("Shadow-Alpha", 255, 0, 255)
    private val radiusValue = FloatValue("Top-Radius", 3f, 0f, 10f)
    private val gidentspeed = IntegerValue("GidentSpeed", 100, 1, 1000)
    private val newRainbowIndex = IntegerValue("NewRainbowOffset", 1, 1, 50)
    private val astolfoRainbowOffset = IntegerValue("AstolfoOffset", 5, 1, 20)
    private val astolfoclient = IntegerValue("AstolfoRange", 109, 1, 765)
    private val astolfoRainbowIndex = IntegerValue("AstolfoIndex", 109, 1, 300)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val rainbowX = FloatValue("Rainbow-X", -1000F, -2000F, 2000F)
    private val rainbowY = FloatValue("Rainbow-Y", -1000F, -2000F, 2000F)
    private val shadow = BoolValue("Text-Shadow", true)
    private val bord = BoolValue("Border", true)
    private val char = BoolValue("NotChar", false)
    private val balpha = IntegerValue("BordAlpha", 255, 0, 255)
    private val distanceValue = IntegerValue("Distance", 0, 0, 400)
    private val amountValue = IntegerValue("Amount", 25, 1, 50)
    private var fontValue = FontValue("Font", Fonts.font40)

    private var editMode = false
    private var editTicks = 0
    private var prevClick = 0L

    private var speedStr = ""
    private var displayText: String = Pride.CLIENT_NAME
    val hud = Pride.moduleManager[HUD::class.java] as HUD
    //val username: String = mc.player!!.name
    private val display: String
        get() {
            val textContent = if (displayString.get().isEmpty() && !editMode)
                Pride.CLIENT_NAME + " | Dev: WaWa"
            else
                displayString.get()


            return multiReplace(textContent)
        }

    private fun getReplacement(str: String): String? {
        if (mc.player != null) {
            when (str) {
                "x" -> return DECIMAL_FORMAT.format(mc.player!!.posX)
                "y" -> return DECIMAL_FORMAT.format(mc.player!!.posY)
                "z" -> return DECIMAL_FORMAT.format(mc.player!!.posZ)
                "xInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.posX)
                "yInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.posY)
                "zInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.posZ)
                "xdp" -> return mc.player!!.posX.toString()
                "ydp" -> return mc.player!!.posY.toString()
                "zdp" -> return mc.player!!.posZ.toString()
                "velocity" -> return DECIMAL_FORMAT.format(sqrt(mc.player!!.motionX * mc.player!!.motionX + mc.player!!.motionZ * mc.player!!.motionZ))
                "ping" -> return EntityUtils.getPing(mc.player!!).toString()
                "health" -> return DECIMAL_FORMAT.format(mc.player!!.health)
                "maxHealth" -> return DECIMAL_FORMAT.format(mc.player!!.maxHealth)
                "healthInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.health)
                "maxHealthInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.maxHealth)
                "yaw" -> return DECIMAL_FORMAT.format(mc.player!!.rotationYaw)
                "pitch" -> return DECIMAL_FORMAT.format(mc.player!!.rotationPitch)
                "yawInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.rotationYaw)
                "pitchInt" -> return DECIMAL_FORMAT_INT.format(mc.player!!.rotationPitch)
                "bps" -> return speedStr
                "hurtTime" -> return mc.player!!.hurtTime.toString()
                "onGround" -> return mc.player!!.onGround.toString()
            }
        }

        return when (str) {
            "clientname" -> Pride.CLIENT_NAME
            "clientversion" -> "b${Pride.CLIENT_VERSION}"
            "clientcreator" -> Pride.CLIENT_CREATOR
            "fps" -> Minecraft.getDebugFPS().toString()
            "date" -> DATE_FORMAT.format(System.currentTimeMillis())
            "time" -> HOUR_FORMAT.format(System.currentTimeMillis())
            "serverip" -> ServerUtils.getRemoteIp()
            "cps", "lcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toString()
            "mcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE).toString()
            "rcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toString()
            "userName" -> mc.session.username
            "clientName" -> Pride.CLIENT_NAME
            "clientVersion" -> Pride.CLIENT_VERSION
            "clientCreator" -> Pride.CLIENT_CREATOR
            "fps" -> Minecraft.getDebugFPS().toString()
            "date" -> DATE_FORMAT.format(System.currentTimeMillis())
            "time" -> HOUR_FORMAT.format(System.currentTimeMillis())
            "serverIp" -> ServerUtils.getRemoteIp()
            "cps", "lcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toString()
            "mcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE).toString()
            "rcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toString()
//            "watchdogLastMin" -> BanChecker.WATCHDOG_BAN_LAST_MIN.toString()
//            "staffLastMin" -> BanChecker.STAFF_BAN_LAST_MIN.toString()
//            "sessionTime" -> return SessionUtils.getFormatSessionTime()
//            "worldTime" -> return SessionUtils.getFormatWorldTime()
            else -> null // Null = don't replace
        }
    }

    private fun multiReplace(str: String): String {
        var lastPercent = -1
        val result = StringBuilder()
        for (i in str.indices) {
            if (str[i] == '%') {
                if (lastPercent != -1) {
                    if (lastPercent + 1 != i) {
                        val replacement = getReplacement(str.substring(lastPercent + 1, i))

                        if (replacement != null) {
                            result.append(replacement)
                            lastPercent = -1
                            continue
                        }
                    }
                    result.append(str, lastPercent, i)
                }
                lastPercent = i
            } else if (lastPercent == -1) {
                result.append(str[i])
            }
        }

        if (lastPercent != -1) {
            result.append(str, lastPercent, str.length)
        }

        return result.toString()
    }

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()
        var length2 = 4.5f
        val charArray = displayText.toCharArray()
        if(char.get()) {
            length2 = fontRenderer.getStringWidth(displayText).toFloat()
        } else {
            for (charIndex in charArray) {
                length2 += fontRenderer.getStringWidth(charIndex.toString())
            }
        }


            //shadow
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glScalef(1F, 1F, 1F)
            GL11.glPushMatrix()
            if (shadowValueopen.get()) {
                ShadowUtils.shadow(shadowValue.get(), {
                    GL11.glPushMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                    GL11.glScalef(scale, scale, scale)

                    RenderUtils.originalRoundedRect(
                            -2f, 0F, (fontRenderer.getStringWidth(displayText) + 2).toFloat(), fontRenderer.FONT_HEIGHT.toFloat(), radiusValue.get(),
                            if (shadowColorMode.get().equals("background", true))
                                Color(32, 30, 30).rgb
                            else
                                Color(r.get(), g.get(), b.get(),shadowalpha.get()).rgb
                    )
                    GL11.glPopMatrix()
                }, {
                    GL11.glPushMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                    GL11.glScalef(scale, scale, scale)
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderUtils.fastRoundedRect(-2f, 0f, (fontRenderer.getStringWidth(displayText) + 2).toFloat(), fontRenderer.FONT_HEIGHT.toFloat(), radiusValue.get())
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                    GL11.glPopMatrix()
                }
                )
            }
            GL11.glPopMatrix()
            GL11.glScalef(scale, scale, scale)
            GL11.glTranslated(renderX, renderY, 0.0)
        //blur
            if (blurValuee.get()){
                GL11.glTranslated(-renderX, -renderY, 0.0)
                GL11.glPushMatrix()
                BlurBuffer.CustomBlurRoundArea(renderX.toFloat()+-2, renderY.toFloat(), (fontRenderer.getStringWidth(displayText) + 3).toFloat()+1, fontRenderer.FONT_HEIGHT.toFloat()
                        , radiusValue.get(),BlurStrength.get())
                GL11.glPopMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
            }


        val colorMode = colorModeValue.get()
        val color = Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
        val rainbow = colorMode.equals("Rainbow", ignoreCase = true)
        if (bord.get()) {
            if (Mode.get() == "Skeet") {
                RenderUtils.autoExhibition(-4.0, -5.2, (length2).toDouble(), (fontRenderer.FONT_HEIGHT + 1.5).toDouble(),1.0)
                val barLength = (length2).toDouble()
                for (i in 0..(amountValue.get()-1)) {
                    val barStart = i.toDouble() / amountValue.get().toDouble() * barLength
                    val barEnd = (i + 1).toDouble() / amountValue.get().toDouble() * barLength
                    RenderUtils.drawGradientSideways(-1.4 + barStart, -2.7, -1.4 + barEnd, -2.0,
                            when {
                                rainbow -> 0
                                colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), displayText.length * 200).rgb
                                colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(i * distanceValue.get(), saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                                colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(),1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + i * distanceValue.get()) / 10)).rgb
                                colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(i * distanceValue.get(),newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                                else -> color
                            },
                            when {
                                rainbow -> 0
                                colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), displayText.length * 200).rgb
                                colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(i * distanceValue.get(), saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                                colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(),1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + i * distanceValue.get()) / 10)).rgb
                                colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(i * distanceValue.get(),newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                                else -> color
                            })
                }
            }
            if (Mode.get() == "Slide") {
                RenderUtils.drawRect(-4.0f, -4.5f, (length2).toFloat(), fontRenderer.FONT_HEIGHT.toFloat(), Color(0, 0, 0, balpha.get()).rgb)
                val barLength = (length2 + 1).toDouble()
                for (i in 0..(amountValue.get()-1)) {
                    val barStart = i.toDouble() / amountValue.get().toDouble() * barLength
                    val barEnd = (i + 1).toDouble() / amountValue.get().toDouble() * barLength
                    RenderUtils.drawGradientSideways(-4.0 + barStart, -4.2, -1.0 + barEnd, -3.0,
                            when {
                                rainbow -> 0
                                colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), displayText.length * 200).rgb
                                colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(i * distanceValue.get(), saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                                colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(),1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + i * distanceValue.get()) / 10)).rgb
                                colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(i * distanceValue.get(),newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                                else -> color
                            },
                            when {
                                rainbow -> 0
                                colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), displayText.length * 200).rgb
                                colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(i * distanceValue.get(), saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                                colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(),1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + i * distanceValue.get()) / 10)).rgb
                                colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(i * distanceValue.get(),newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                                else -> color
                            })
                }
            }
        }
        val counter = intArrayOf(0)
        if(char.get()){
            val rainbow = colorMode.equals("Rainbow", ignoreCase = true)
            RainbowFontShader.begin(rainbow, if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(), if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(), System.currentTimeMillis() % 10000 / 10000F).use {
                fontRenderer.drawString(displayText, 0F, 0F, when {
                    rainbow -> 0
                    colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), counter[0] * 100, displayText.length * 200).rgb
                    colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(counter[0] * 100, saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                    colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(counter[0] * 100, newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                    colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(), 1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + counter[0]) / 10)).rgb
                    else -> color
                }, shadow.get())
                if (editMode && mc.currentScreen is GuiHudDesigner && editTicks <= 40)
                    fontRenderer.drawString("_", fontRenderer.getStringWidth(displayText).toFloat(),
                            0F, when {
                        rainbow -> 0
                        colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), counter[0] * 100, displayText.length * 200).rgb
                        colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(counter[0] * 100, saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                        colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(), 1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + counter[0]) / 10)).rgb
                        colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(counter[0] * 100, newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                        else -> color
                    }, shadow.get())
                counter[0] += 1
            }
        } else {
            var length = 0
            RainbowFontShader.begin(rainbow, if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(), if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(), System.currentTimeMillis() % 10000 / 10000F).use {
                for (charIndex in charArray) {
                    val rainbow = colorMode.equals("Rainbow", ignoreCase = true)
                    fontRenderer.drawString(charIndex.toString(), length.toFloat(), 0F, when {
                        rainbow -> 0
                        colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), counter[0] * 100, displayText.length * 200).rgb
                        colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(counter[0] * 100, saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                        colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(counter[0] * 100, newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                        colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(), 1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + counter[0]) / 10)).rgb
                        else -> color
                    }, shadow.get())
                    counter[0] += 1
                    counter[0] = counter[0].coerceIn(0, displayText.length)
                    length += fontRenderer.getStringWidth(charIndex.toString())
                }
                if (editMode && mc.currentScreen is GuiHudDesigner && editTicks <= 40)
                    fontRenderer.drawString("_", length2,
                            0F, when {
                        rainbow -> 0
                        colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), counter[0] * 100, displayText.length * 200).rgb
                        colorMode.equals("Astolfo", ignoreCase = true) -> RenderUtils.Astolfo(counter[0] * 100, saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                        colorMode.equals("Gident", ignoreCase = true) -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(), 1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + counter[0]) / 10)).rgb
                        colorMode.equals("NewRainbow", ignoreCase = true) -> RenderUtils.getRainbow(counter[0] * 100, newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                        else -> color
                    }, shadow.get())
            }
        }
        if (shadows.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                fontRenderer.drawString(
                        displayText, 0F*scale, 0F*scale, when (colorModeValue.get().toLowerCase()) {
                    "Rainbow"-> 0
                    "Fade"-> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), counter[0] * 100, displayText.length * 200).rgb
                    "Astolfo" -> RenderUtils.Astolfo(counter[0] * 100, saturationValue.get(), brightnessValue.get(), astolfoRainbowOffset.get(), astolfoRainbowIndex.get(), astolfoclient.get().toFloat())
                    "Gident" -> RenderUtils.getGradientOffset(Color(redValue.get(), greenValue.get(), blueValue.get()), Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get(), 1), (Math.abs(System.currentTimeMillis() / gidentspeed.get().toDouble() + counter[0]) / 10)).rgb
                    "NewRainbow" -> RenderUtils.getRainbow(counter[0] * 100, newRainbowIndex.get(), saturationValue.get(), brightnessValue.get())
                    else -> color
                }, false)
                GL11.glPopMatrix()
            }, {})
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
        if (editMode && mc.currentScreen !is GuiHudDesigner) {
            editMode = false
            updateElement()
        }
        return Border(-2F, -2F, length2, fontRenderer.FONT_HEIGHT.toFloat())
    }

    override fun updateElement() {
        editTicks += 5
        if (editTicks > 80) editTicks = 0

        displayText = if (editMode) displayString.get() else display
    }

    override fun handleMouseClick(x: Double, y: Double, mouseButton: Int) {
        if (isInBorder(x, y) && mouseButton == 0) {
            if (System.currentTimeMillis() - prevClick <= 250L)
                editMode = true

            prevClick = System.currentTimeMillis()
        } else {
            editMode = false
        }
    }

    override fun handleKey(c: Char, keyCode: Int) {
        if (editMode && mc.currentScreen is GuiHudDesigner) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (displayString.get().isNotEmpty())
                    displayString.set(displayString.get().substring(0, displayString.get().length - 1))

                updateElement()
                return
            }

            if (ChatAllowedCharacters.isAllowedCharacter(c) || c == '§')
                displayString.set(displayString.get() + c)

            updateElement()
        }
    }

    fun setColor(c: Color): Text2 {
        redValue.set(c.red)
        greenValue.set(c.green)
        blueValue.set(c.blue)
        return this
    }


    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        glColor(color)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun drawRect(x: Double, y: Double, x2: Double, y2: Double, color: Int) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        glColor(color)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2d(x2, y)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x, y2)
        GL11.glVertex2d(x2, y2)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

}