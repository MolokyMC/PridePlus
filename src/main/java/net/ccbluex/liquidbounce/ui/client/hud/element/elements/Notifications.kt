
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max


/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications")
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {


    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val titleShadow = BoolValue("TitleShadow", false)
    private val motionBlur = BoolValue("Motionblur", false)
    private val contentShadow = BoolValue("ContentShadow", true)
    private val whiteText = BoolValue("WhiteTextColor", true)
    private val modeColored = BoolValue("CustomModeColored", true)
    companion object {
        val styleValue = ListValue("Mode", arrayOf("Tomk","Classic", "FDP", "Modern", "Tenacity",  "Skid", "Tena-Classic", "Astolfo","Intellij","LiquidBounce"), "Tenacity")
        val radius = FloatValue("Radius", 5F, 0F, 20F).displayable { styleValue.get() == "Tomk" }
    }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        // bypass java.util.ConcurrentModificationException
        Pride.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(index, Fonts.font35, backGroundAlphaValue.get(), 0F, this.renderX.toFloat(), this.renderY.toFloat(), scale,contentShadow.get(),titleShadow.get(),motionBlur.get(),whiteText.get(),modeColored.get(), Companion)) {
                Pride.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!Pride.hud.notifications.contains(exampleNotification)) {
                Pride.hud.addNotification(exampleNotification)
            }

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }

    fun drawBoarderBlur(blurRadius: Float) {
    }
}


class Notification(
    val title: String,
    val content: String,
    val type: NotifyType,
    val time: Int = 1500,
    private val animeTime: Int = 500
) {
    var width = 100
    val height = 30

    private val classicHeight = 30
    var x = 0F
    var textLengthtitle = 0
    var textLengthcontent = 0
    var textLength = 0f
    init {
        textLengthtitle = Fonts.font35.getStringWidth(title)
        textLengthcontent = Fonts.font35.getStringWidth(content)
        textLength = textLengthcontent.toFloat() + textLengthtitle.toFloat()
    }

    var fadeState = FadeState.IN
    private var nowY = -height
    var displayTime = System.currentTimeMillis()
    private var animeXTime = System.currentTimeMillis()
    private var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(
        index: Int, font: FontRenderer, alpha: Int, blurRadius: Float, x: Float, y: Float, scale: Float,
        contentShadow: Boolean,
        titleShadow: Boolean,
        motionBlur: Boolean,
        whiteText: Boolean,
        modeColored: Boolean,
        parent: Notifications.Companion

    ): Boolean {
        this.width = 100.coerceAtLeast(
            font.getStringWidth(content)
                .coerceAtLeast(font.getStringWidth(title)) + 15
        )
        val realY = if(parent.styleValue.get().equals("Tomk")){
            -(index + 1) * (height + 11)
        } else {
            -(index+1) * (height + 2)
        }
        val nowTime = System.currentTimeMillis()
        var transY = nowY.toDouble()
        var lbtl = font.getStringWidth(title + ": " + content)
        var x = 0f

        var textColor = Color(255, 255, 255).rgb

        if (whiteText) {
            textColor = Color(255, 255, 255).rgb
        } else {
            textColor = Color(10, 10, 10).rgb
        }

        //Y-Axis Animation
        if(nowY!=realY){
            var pct=(nowTime-animeYTime)/animeTime.toDouble()
            if(pct>1){
                nowY=realY
                pct=1.0
            }else{
                pct= EaseUtils.easeOutQuart(pct)
            }
            GL11.glTranslated(0.0,(realY-nowY)*pct,0.0)
        }else{
            animeYTime=nowTime

        }
        GL11.glTranslated(1.0,nowY.toDouble(),0.0)

        // X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct= EaseUtils.easeOutQuart(pct)
                transY+=(realY-nowY)*pct
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct > 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 2.0
                }
                pct=1- EaseUtils.easeInQuart(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        val transX=width-(width*pct)-width
        GL11.glTranslated(width-(width*pct),0.0,0.0)
        GL11.glTranslatef(-width.toFloat(),0F,0F)
        // draw notify
        val style = parent.styleValue.get()
        val nTypeWarning = if(type.renderColor == Color(0xF5FD00)){ true } else { false }
        val nTypeInfo = if(type.renderColor == Color(0x6490A7)) { true } else { false }
        val nTypeSuccess = if(type.renderColor == Color(0x60E092)) { true } else { false }
        val nTypeError = if(type.renderColor == Color(0xFF2F2F)) { true } else { false }


        if (style.equals("Modern")) {


            var colorRed = type.renderColor.red
            var colorGreen = type.renderColor.green
            var colorBlue = type.renderColor.blue

            if (modeColored) {
                //success
                if (colorRed    == 60)   colorRed    = 36
                if (colorGreen  == 224)  colorGreen  = 211
                if (colorBlue   == 92)   colorBlue   = 99

                //error
                if (colorRed    == 255) colorRed    = 248
                if (colorGreen  == 47)  colorGreen  = 72
                if (colorBlue   == 47)  colorBlue   = 72

                //warning
                if (colorRed    == 245) colorRed    = 251
                if (colorGreen  == 253)  colorGreen  = 189
                if (colorBlue   == 0)  colorBlue   = 23

                //info
                if (colorRed    == 64) colorRed    = 242
                if (colorGreen  == 90)  colorGreen  = 242
                if (colorBlue   == 167)  colorBlue   = 242
            }


            val colors = Color(colorRed, colorGreen, colorBlue, alpha / 3)

            if (motionBlur) {
                when (fadeState) {
                    FadeState.IN -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.STAY -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.OUT -> {
                        RenderUtils.drawRoundedCornerRect(4F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(5F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }
                }
            } else {
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            }
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 5f, 0F), 27f - 5f, 2f, Color(0, 0, 0, 26).rgb)
            Fonts.font35.drawString(title, 6F, 3F, textColor, titleShadow)
            font.drawString(content, 6F, 12F, textColor, contentShadow)
            return false
        }

        if (style.equals("FDP")) {



            val colors = Color(0, 0, 0, alpha / 4)

            if (motionBlur) {
                when (fadeState) {
                    FadeState.IN -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.STAY -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.OUT -> {
                        RenderUtils.drawRoundedCornerRect(4F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(5F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }
                }
            } else {
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            }
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 5f, 0F), 27f - 5f, 2f, Color(0, 0, 0, 40).rgb)
            Fonts.font35.drawString(title, 6F, 3F, textColor, titleShadow)
            font.drawString(content, 6F, 12F, textColor, contentShadow)
            return false
        }

        // lbtl means liquidbounce text length
         if(style.equals("LiquidBounce")) {
            RenderUtils.drawRect(-1F, 0F, lbtl + 9F, -20F, Color(0, 0, 0, alpha))
            Fonts.font35.drawString(title + ": " + content, -4F, 3F, textColor, titleShadow)
            RenderUtils.drawRect(-1F + max(lbtl + 5F - (lbtl+ 5F) * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), 0F, 4F + max(lbtl + 5F - (lbtl+ 5F) * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), -20F, Color(0, 0, 0, alpha))
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        }


/*        if(style.equals("Simple")) {
            RenderUtils.customRoundedinf(-x + 8F + lbtl, -y, -x - 2F, -18F - y, 0F, 3F, 3F, 0F, Color(0,0,0, alpha).rgb)
            RenderUtils.customRoundedinf(-x - 2F, -y, -x - 5F, -18F - y, 3F, 0F, 0F, 3F, type.renderColor)
            Fonts.font40.drawString("$title: $content", -x + 3, -13F - y, -1)
            }*/

        if(style.equals("Skid")){

            val colors=Color(type.renderColor.red,type.renderColor.green,type.renderColor.blue,alpha/3)
            RenderUtils.drawRect(2, 0, 4, 27 - 5, colors.rgb)
            RenderUtils.drawRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, Color(0,0,0,150))
            RenderUtils.drawGradientSidewaysH(3.0, 0.0, 20.0, 27f - 5.0, colors.rgb, Color(0,0,0,0).rgb)
            RenderUtils.drawRect(2f, 27f-6f, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time))+5f, 0F), 27f - 5f ,Color(52, 97, 237).rgb)
            RenderUtils.drawShadowWithCustomAlpha(3F, 0F, width.toFloat() + 5f, 27f - 5f,255f)
            Fonts.font35.drawString(title, 6F, 3F, textColor, titleShadow)
            font.drawString(content, 6F, 12F, textColor, contentShadow)
            return false
        }

        if(style.equals("Tenacity")){
            val fontRenderer = Fonts.font35
            val thisWidth=100.coerceAtLeast(fontRenderer.getStringWidth(this.title).coerceAtLeast(fontRenderer.getStringWidth(this.content)) + 40)
            val error = ResourceLocation("pride/notification/error.png")
            val successful = ResourceLocation("pride/notification/success.png")
            val warn = ResourceLocation("pride/notification/warning.png")
            val info = ResourceLocation("pride/notification/info.png")
            if(type.renderColor == Color(0xFF2F2F)){
                RenderUtils.drawRoundedCornerRect(-18F,1F,thisWidth.toFloat(),height.toFloat() - 2F,5f,Color(180,0,0,190).rgb)
                RenderUtils.drawImage(error,-13,5,18,18)
                Fonts.font35.drawString(title,9F,16F,Color(255,255,255,255).rgb)
                Fonts.font40.drawString(content,9F,6F,Color(255,255,255,255).rgb)
            }else if(type.renderColor == Color(0x60E092)){
                RenderUtils.drawRoundedCornerRect(-16F,1F,thisWidth.toFloat(),height.toFloat() - 2F,5f,Color(0,180,0,190).rgb)
                RenderUtils.drawImage(successful,-13,5,18,18)
                Fonts.font35.drawString(title,9F,16F,Color(255,255,255,255).rgb)
                Fonts.font40.drawString(content,9F,6F,Color(255,255,255,255).rgb)
            } else if(type.renderColor == Color(0xF5FD00)){
                RenderUtils.drawRoundedCornerRect(-16F,1F,thisWidth.toFloat(),height.toFloat() - 2F,5f,Color(0,0,0,190).rgb)
                RenderUtils.drawImage(warn,-13,5,18,18)
                Fonts.font35.drawString(title,9F,16F,Color(255,255,255,255).rgb)
                Fonts.font40.drawString(content,9F,6F,Color(255,255,255,255).rgb)
            } else {
                RenderUtils.drawRoundedCornerRect(-16F,1F,thisWidth.toFloat(),height.toFloat() - 2F,5f,Color(0,0,0,190).rgb)
                RenderUtils.drawImage(info,-13,5,18,18)
                Fonts.font35.drawString(title,9F,16F,Color(255,255,255,255).rgb)
                Fonts.font40.drawString(content,9F,6F,Color(255,255,255,255).rgb)
            }
            return false
        }

        if(style.equals("Classic")) {
            if (blurRadius != 0f)

                RenderUtils.drawRect(0F, 0F, width.toFloat(), classicHeight.toFloat(), Color(0, 0, 0, alpha))
            RenderUtils.drawRect(0F, classicHeight - 2F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), classicHeight.toFloat(), type.renderColor)
            font.drawString(title, 4F, 4F, textColor, false)
            font.drawString(content, 4F, 17F, textColor, false)
            return false
        }


        if(style.equals("Tena-Classic")) {
            val thisWidth = 116.coerceAtLeast(font.getStringWidth(this.title).coerceAtLeast(font.getStringWidth(this.content)) + 56)

            if(type.renderColor == Color(0xFF2F2F)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/error.png"),3,5,18,18)
            }else if(type.renderColor == Color(0x60E092)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/success.png"),3,5,18,18)
            } else if(type.renderColor == Color(0xF5FD00)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/warning.png"),3,5,18,18)
            } else {
                RenderUtils.drawImage(ResourceLocation("pride/notification/info.png"),3,5,18,18)
            }

            if (blurRadius != 0f)


                RenderUtils.drawRoundedCornerRect(0F, 0F, thisWidth.toFloat(), classicHeight.toFloat(), 3F, Color(0, 0, 0, alpha).rgb)
            RenderUtils.drawRect(0F, classicHeight - 2F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), classicHeight.toFloat(), type.renderColor)
            font.drawString(title, 25F, 4F, textColor, false)
            font.drawString(content, 25F, 17F, textColor, false)
            return false
        }

        if(style.equals("Astolfo")) {
            val thisWidth = 116.coerceAtLeast(font.getStringWidth(this.title).coerceAtLeast(font.getStringWidth(this.content)) + 56)

            if(type.renderColor == Color(0xFF2F2F)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/error.png"),3,5,18,18)
            }else if(type.renderColor == Color(0x60E092)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/success.png"),3,5,18,18)
            } else if(type.renderColor == Color(0xF5FD00)){
                RenderUtils.drawImage(ResourceLocation("pride/notification/warning.png"),3,5,18,18)
            } else {
                RenderUtils.drawImage(ResourceLocation("pride/notification/info.png"),3,5,18,18)
            }


            RenderUtils.drawRoundedCornerRect(0F, 0F, thisWidth.toFloat(), classicHeight.toFloat(), 3F, Color(72, 71, 89).rgb)
            RenderUtils.drawRect(2F, classicHeight - 4F, thisWidth.toFloat() - 2F, classicHeight.toFloat() - 2F, Color(0, 0, 0, 50))
            RenderUtils.drawRect(2F, classicHeight - 4F, 2F + max((thisWidth.toFloat() - 4F) - (thisWidth.toFloat() - 4F) * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), classicHeight.toFloat() - 2F, type.renderColor)
            font.drawString(title, 25F, 4F, textColor, false)
            font.drawString(content, 25F, 17F, textColor, false)
            return false
        }

        if(style.equals("Intellij")) {
            val notifyDir = "pride/notification/"
            val imgSuccess = ResourceLocation("${notifyDir}success.png")
            val imgError = ResourceLocation("${notifyDir}error.png")
            val imgWarning = ResourceLocation("${notifyDir}warning.png")
            val imgInfo = ResourceLocation("${notifyDir}info.png")

            val dist = (x + 1 + 26F) - (x - 8 - textLength)
            val kek = -x - 1 - 20F

            GlStateManager.resetColor()

            Stencil.write(true)
            if(nTypeError){
                RenderUtils.drawRoundedRect(-x + 9 + textLength, 1f, kek - 1, -28F - 1, 0, Color(115,69,75).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, 0f, kek, -28F, 0, Color(89,61,65).rgb)
                Fonts.font35.drawString(title, -x + 6, -25F, Color(249,130,108).rgb, true)
            }
            if(nTypeInfo) {
                RenderUtils.drawRoundedRect(-x + 9 + textLength,  1f, kek - 1, -28F - 1, 0, Color(70,94,115).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, 0f, kek, -28F, 0, Color(61,72,87).rgb)
                Fonts.font35.drawString(title, -x + 6, -25F, Color(119,145,147).rgb, true)
            }
            if(nTypeSuccess){
                RenderUtils.drawRoundedRect(-x + 9 + textLength, 1f, kek - 1, -28F - 1, 0, Color(67,104,67).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, 0f, kek, -28F, 0, Color(55,78,55).rgb)
                Fonts.font35.drawString(title,-x + 6, -25F, Color(10,142,2).rgb, true)
            }
            if(nTypeWarning){
                RenderUtils.drawRoundedRect(-x + 9 + textLength, 1f, kek - 1, -28F - 1, 0, Color(103,103,63).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, 0f, kek, -28F, 0, Color(80,80,57).rgb)
                Fonts.font35.drawString(title, -x + 6, -25F, Color(175,163,0).rgb, true)
            }

            Stencil.erase(true)

            GlStateManager.resetColor()

            Stencil.dispose()

            GL11.glPushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.resetColor()
            GL11.glColor4f(1F, 1F, 1F, 1F)
            RenderUtils.drawImage(if(nTypeSuccess){imgSuccess} else if(nTypeError){imgError} else if(nTypeWarning){imgWarning} else {imgInfo},
                (kek + 5).toInt(), (-25F - y).toInt(), 7, 7)
            GlStateManager.enableAlpha()
            GL11.glPopMatrix()


            Fonts.font35.drawString(content, -x + 6, -13F, -1, true)
            return false
        }

        if (style.equals("Tomk")){
            val s2 = "wawa/notification/info.png"
            val width = 150.coerceAtLeast((Fonts.posterama40.getStringWidth(this.content)) + 33)
            val height = 40
            RoundedUtil.drawRound(
                0f,
                0f, width.toFloat(), height.toFloat() - 10f, parent.radius.get(), Color(0,0,0,104)
            )
            RenderUtils.drawImage(ResourceLocation(s2),5,5,20,20)
            RenderUtils.drawRect(30F,5F,31f,height-15f,Color.WHITE.rgb)
            Fonts.posterama40.drawString(
                title, 35F,
                ((Fonts.posterama50.fontHeight / 2f).toDouble() -1).toFloat(), Color.WHITE.rgb, false
            )
            Fonts.posterama40.drawString(
                content,
                35F,
                ((Fonts.posterama40.fontHeight / 2f).toDouble() + 12.5).toFloat(),
                Color.WHITE.rgb,
                false
            )
            RoundedUtil.drawRound(0F, height.toFloat() - 10,  max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), -22F),2f,1f, Color(0, 162, 255))
            return false
        }

        return false
    }

}

//NotifyType Color
enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E092)),
    ERROR(Color(0xFF2F2F)),
    WARNING(Color(0xF5FD00)),
    INFO(Color(0x6490A7));
}
//classic
// SUCCESS(Color((0x60E092)),
// ERROR(Color(0xFF2F2F)),
// WARNING(Color(0xF5FD00)),
// INFO(Color( 0x6490A7)));
//modern (shitty)
//    SUCCESS(Color(0x36D399)),
// ERROR(Color(0xF87272)),
// WARNING(Color(0xFBBD23)),
// INFO(Color(0xF2F2F2));


enum class FadeState { IN, STAY, OUT, END }


