package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.Palette
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ElementInfo(name = "Target")
class Target : Element(-46.0,-40.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    private val modeValue = ListValue("Mode", arrayOf("WaterMelon","SparklingWater","Best","Novoline","Astolfo","Liquid","Flux","Rise","Zamorozka","novoline2","moon","novoline3","newnovoline","tenacity"), "Rise")
    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide","Zoom","None"), "Slide")
    private val animSpeedValue = IntegerValue("AnimSpeed",10,5,20)
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed",20,5,40)
    private val fontValue = FontValue("Font", Fonts.font40)
    val backgroundalpha = IntegerValue("Alpha", 120, 0, 255)
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val gredValue = IntegerValue("GradientRed", 255, 0, 255)
    private val ggreenValue = IntegerValue("GradientGreen", 255, 0, 255)
    private val gblueValue = IntegerValue("GradientBlue", 255, 0, 255)
    private var easingHP = 0f
    private var prevTarget: EntityLivingBase?=null
    private var lastHealth=20F
    private var lastChangeHealth=20F
    private var changeTime=System.currentTimeMillis()
    private var displayPercent=0f
    private var lastUpdate = System.currentTimeMillis()
    private val decimalFormat = DecimalFormat("0.0")
    private  val counter1 = intArrayOf(50)
    private val counter2 = intArrayOf(80)

    private fun getHealth(entity: EntityLivingBase?):Float{
        return if(entity==null || entity.isDead){ 0f }else{ entity.health }
    }

    override fun drawElement(): Border? {
        var target=(Pride.moduleManager[KillAura::class.java] as KillAura).target
        val time=System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get()*50f)
        lastUpdate=System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target = mc.player
        }
        if (target != null) {
            prevTarget = target
        }
        prevTarget ?: return getTBorder()

        if (target!=null) {
            if (displayPercent < 1) {
                displayPercent += pct
            }
            if (displayPercent > 1) {
                displayPercent = 1f
            }
        } else {
            if (displayPercent > 0) {
                displayPercent -= pct
            }
            if (displayPercent < 0) {
                displayPercent = 0f
                prevTarget=null
                return getTBorder()
            }
        }

        if(getHealth(prevTarget)!=lastHealth){
            lastChangeHealth=lastHealth
            lastHealth=getHealth(prevTarget)
            changeTime=time
        }
        val nowAnimHP=if((time-(animSpeedValue.get()*50))<changeTime){
            getHealth(prevTarget)+(lastChangeHealth-getHealth(prevTarget))*(1-((time-changeTime)/(animSpeedValue.get()*50F)))
        }else{
            getHealth(prevTarget)
        }

        when(switchModeValue.get().toLowerCase()){
            "zoom" -> {
                val border=getTBorder() ?: return null
                GL11.glScalef(displayPercent,displayPercent,displayPercent)
                GL11.glTranslatef(((border.x2 * 0.5f * (1-displayPercent))/displayPercent), ((border.y2 * 0.5f * (1-displayPercent))/displayPercent).toFloat(), 0f)
            }
            "slide" -> {
                val percent= EaseUtils.easeInQuint(1.0-displayPercent)
                val xAxis= ScaledResolution(mc).scaledWidth-renderX
                GL11.glTranslated(xAxis*percent,0.0,0.0)
            }
        }

        when(modeValue.get().toLowerCase()){

            "sparklingwater" -> drawSparklingWater(prevTarget!!,nowAnimHP)
            "novoline" -> drawNovo(prevTarget!!,nowAnimHP)
            "astolfo" -> drawAstolfo(prevTarget!!,nowAnimHP)
            "liquid" -> drawLiquid(prevTarget!!,nowAnimHP)
            "flux" -> drawFlux(prevTarget!!,nowAnimHP)
            "rise" -> drawRise(prevTarget!!,nowAnimHP)
            "best"->drawBest(prevTarget!!,nowAnimHP)
            "zamorozka" -> drawZamorozka(prevTarget!!,nowAnimHP)
            "novoline2"-> drawnovoline2(prevTarget!!,nowAnimHP)
            "moon"-> drawMoon(prevTarget!!,nowAnimHP)
            "novoline3"-> drawnovoline3(prevTarget!!,nowAnimHP)
            "newnovoline"-> drawnewnovo(prevTarget!!,nowAnimHP)
            "tenacity"-> drawTenacity(prevTarget!!,nowAnimHP)
            "watermelon" -> drawWaterMelon(prevTarget!!,nowAnimHP)
        }

        return getTBorder()
    }
    private fun drawWaterMelon(target: EntityLivingBase, easingHealth: Float) {
        // background rect
        RenderUtils.drawRoundedCornerRect(
            -1.5f, 2.5f, 152.5f, 52.5f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -1f, 2f, 152f, 52f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0.5f, 1.5f, 151.5f, 51.5f,
            5.0f, Color(0, 0, 0, 40).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0f, 1f, 151.0f, 51.0f,
            5.0f, Color(0, 0, 0, 60).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            0.5f, 0.5f, 150.5f, 50.5f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            1f, 0f, 150.0f, 50.0f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        // head size based on hurt
        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) {
            1f
        } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35
        // draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(mc.connection!!.getPlayerInfo(target.uniqueID).locationSkin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        GL11.glPopMatrix()
        // draw name of target
        Fonts.font35.drawString("${target.name}", 45f, 12f, Color.WHITE.rgb)
        val df = DecimalFormat("0.00");
        // draw armour percent
        Fonts.font30.drawString(
            "Armor ${(df.format(PlayerUtils.getAr(target) * 100))}%",
            45f,
            24f,
            Color(200, 200, 200).rgb
        )
        // draw bar
        RenderUtils.drawRoundedCornerRect(45f, 32f, 145f, 42f, 5f, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRoundedCornerRect(
            45f,

            32f,
            45f + (easingHealth / target.maxHealth) * 100f,
            42f,
            5f,
            ColorUtils.rainbow().rgb
        )
        // draw hp as text
        Fonts.font30.drawString(
            "${((df.format((easingHealth / target.maxHealth) * 100)))}%",
            80f,
            34f,
            Color(255, 255, 255).rgb,
            true
        )
    }

    private fun drawSparklingWater(target: EntityLivingBase, easingHealth: Float) {
        // background
        RenderUtils.drawRoundedCornerRect(
            -1.5f, 2.5f, 152.5f, 52.5f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -1f, 2f, 152f, 52f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0.5f, 1.5f, 151.5f, 51.5f,
            5.0f, Color(0, 0, 0, 40).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0f, 1f, 151.0f, 51.0f,
            5.0f, Color(0, 0, 0, 60).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            0.5f, 0.5f, 150.5f, 50.5f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            1f, 0f, 150.0f, 50.0f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        // draw entity
        if(target.hurtTime > 1) {
            GL11.glColor4f(1f, 0f, 0f, 0.5f)
            RenderUtils.drawEntityOnScreen(25, 48, 32, target)
        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            RenderUtils.drawEntityOnScreen(25, 45, 30, target)
        }

        // target text
        Fonts.font35.drawString("${target.name}", 45f, 6f, Color.WHITE.rgb)
        val df = DecimalFormat("0.00");
        // armour text
        Fonts.font30.drawString(
            "Armor ${(df.format(PlayerUtils.getAr(target) * 100))}%",
            45f,
            40f,
            Color(200, 200, 200).rgb
        )//bar
        RenderUtils.drawRoundedCornerRect(45f, 23f, 145f, 33f, 5f, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRoundedCornerRect(
            45f,
            23f,
            45f + (easingHealth / target.maxHealth) * 100f,
            33f,
            5f,
            ColorUtils.rainbow().rgb
        )
        Fonts.font30.drawString(
            "${((df.format((easingHealth / target.maxHealth) * 100)))}%",
            80f,
            25f,
            Color(255, 255, 255).rgb,
            true
        )
        /*
                // draw items
                 GlStateManager.resetColor()
                GL11.glPushMatrix()
                GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
                GlStateManager.enableRescaleNormal()
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                RenderHelper.enableGUIStandardItemLighting()

                val renderItem = mc.renderItem

                var x = 45
                var y = 28

                for (index in 3 downTo 0) {
                    val stack = entity.inventory.armorInventory[index] ?: continue

                    if (stack.item == null)
                        continue

                    renderItem.renderItemIntoGUI(stack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
                    RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())

                    x += 16
                }

                val mainStack = entity.heldItem
                if (mainStack != null && mainStack.item != null) {
                    renderItem.renderItemIntoGUI(mainStack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
                    RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
                }

                RenderHelper.disableStandardItemLighting()
                GlStateManager.disableRescaleNormal()
                GlStateManager.enableAlpha()
                GlStateManager.disableBlend()
                GlStateManager.disableLighting()
                GlStateManager.disableCull()
                GL11.glPopMatrix()
                 */
    }

    private fun drawAstolfo(target: EntityLivingBase, nowAnimHP: Float){
        val font=fontValue.get()
        val color=RenderUtils.skyRainbow(1,1F,0.9F)
        val hpPct=nowAnimHP/target.maxHealth

        RenderUtils.drawRect(0F,0F, 140F, 60F, Color(0,0,0,110).rgb)

        // health rect
        RenderUtils.drawRect(3F, 55F, 137F, 58F,ColorUtils.reAlpha(color,100).rgb)
        RenderUtils.drawRect(3F,55F,3+(hpPct*134F),58F,color.rgb)
        GL11.glColor4f(1f,1f,1f,1f)
        RenderUtils.drawEntityOnScreen(18,46,20,target)

        font.drawStringWithShadow(target.name!!, 37F, 6F, -1)
        GL11.glPushMatrix()
        GL11.glScalef(2F,2F,2F)
        font.drawString("${getHealth(target).roundToInt()} ❤", 19,9, color.rgb)
        GL11.glPopMatrix()
    }

    private fun drawNovo(target: EntityLivingBase, nowAnimHP: Float){
        val font=fontValue.get()
        val color=ColorUtils.healthColor(getHealth(target),target.maxHealth)
        val darkColor=ColorUtils.darker(color,0.6F)
        val hpPos=33F+((getHealth(target) / target.maxHealth * 10000).roundToInt() / 100)

        RenderUtils.drawRect(0F,0F, 140F, 40F, Color(40,40,40).rgb)
        font.drawString(target.name!!, 33, 5, Color.WHITE.rgb)
        RenderUtils.drawEntityOnScreen(20, 35, 15, target)
        RenderUtils.drawRect(hpPos, 18F, 33F + ((nowAnimHP / target.maxHealth * 10000).roundToInt() / 100), 25F, darkColor)
        RenderUtils.drawRect(33F, 18F, hpPos, 25F, color)
        font.drawString("❤", 33, 30, Color.RED.rgb)
        font.drawString(decimalFormat.format(getHealth(target)), 43, 30, Color.WHITE.rgb)
    }

    private fun drawLiquid(target: EntityLivingBase, easingHealth: Float){
        val width = (38 + Fonts.font40.getStringWidth(target.name!!))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)

        // Damage animation
        if (easingHealth > getHealth(target))
            RenderUtils.drawRect(0F, 34F, (easingHealth / target.maxHealth) * width,
                36F, Color(252, 185, 65).rgb)

        // Health bar
        RenderUtils.drawRect(0F, 34F, (getHealth(target) / target.maxHealth) * width,
            36F, Color(252, 96, 66).rgb)

        // Heal animation
        if (easingHealth < getHealth(target))
            RenderUtils.drawRect((easingHealth / target.maxHealth) * width, 34F,
                (getHealth(target) / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb)


        target.name.let { Fonts.font40.drawString(it, 36, 3, 0xffffff) }
        Fonts.font35.drawString("Distance: ${decimalFormat.format(mc.player!!.getDistanceToEntityBox(target))}", 36, 15, 0xffffff)

        // Draw info
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 2, 2, 30, 30)
        val playerInfo = mc.connection!!.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            Fonts.font35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff)
        }
    }

    private fun drawZamorozka(target: EntityLivingBase, easingHealth: Float){
        val font=fontValue.get()

        // Frame
        RenderUtils.drawCircleRect(0f,0f,150f,55f,5f,Color(0,0,0,70).rgb)
        RenderUtils.drawRect(7f,7f,35f,40f,Color(0,0,0,70).rgb)
        GL11.glColor4f(1f,1f,1f,1f)
        RenderUtils.drawEntityOnScreen(21, 38, 15, target)

        // Healthbar
        val barLength=143-7f
        RenderUtils.drawCircleRect(7f,45f,143f,50f,2.5f,Color(0,0,0,70).rgb)
        RenderUtils.drawCircleRect(7f,45f,7+((easingHealth/target.maxHealth)*barLength).coerceAtLeast(5f),50f,2.5f,ColorUtils.rainbowWithAlpha(90).rgb)
        RenderUtils.drawCircleRect(7f,45f,7+((target.health/target.maxHealth)*barLength).coerceAtLeast(5f),50f,2.5f,ColorUtils.rainbow().rgb)

        // Info
        RenderUtils.drawCircleRect(43f,15f-font.FONT_HEIGHT,143f,17f,(font.FONT_HEIGHT+1)*0.45f,Color(0,0,0,70).rgb)
        font.drawString("${target.name} ${if(EntityUtils.getPing(target as EntityPlayer) != -1) { "§f${EntityUtils.getPing(target)}ms" } else { "" }}",
            93f - font.getStringWidth("${target.name} ${if(EntityUtils.getPing(target) != -1) { "§f${EntityUtils.getPing(target)}ms" } else { "" }}") / 2, 16f-font.FONT_HEIGHT, ColorUtils.rainbow().rgb, false)
        font.drawString("Health: ${decimalFormat.format(easingHealth)} §7/ ${decimalFormat.format(target.maxHealth)}", 43, 11+font.FONT_HEIGHT,Color.WHITE.rgb)
        font.drawString("Distance: ${decimalFormat.format(mc.player!!.getDistanceToEntityBox(target))}", 43, 11+font.FONT_HEIGHT*2,Color.WHITE.rgb)
    }

    private fun drawMoon(target: EntityLivingBase, easingHealth: Float){
        val font = fontValue.get()
        val hp = decimalFormat.format(easingHealth)
        val additionalWidth = font.getStringWidth("${target.name}  ${hp} hp").coerceAtLeast(75)
        //RenderUtils.drawBorderCircle(0F,0F,45F+additionalWidth,35F,4f,3f,Color(205,70,205,255).rgb)


        // info text
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        val yPos = 5 + font.FONT_HEIGHT + 3f

        val stopPos =
            (5 + ((135 - font.getStringWidth(decimalFormat.format(target.maxHealth))) * (easingHealth / target.maxHealth))).toInt()
        for (i in 5..stopPos step 5) {
            val x1 = (i + 5).coerceAtMost(stopPos).toDouble()
            RenderUtils.quickDrawGradientSideways(i.toDouble()-5.0, 0.0-1/3, 45.0 + additionalWidth-1, 1.0,
                ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(x1.toInt(), indexOffset = 0).rgb)
        }


        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)




        // hp bar
        RenderUtils.drawRect(37f, yPos+5, 37f + additionalWidth, yPos + 13,Color(0, 0, 0,100).rgb)
        if (target.health<=target.maxHealth){
            RenderUtils.drawCircleRect(37f, yPos+5, 37f + ((easingHealth / target.maxHealth * 8100).roundToInt() / 100), yPos + 13,3f,Color(0, 255, 0).rgb)
        }
        if (target.health<target.maxHealth/2){
            RenderUtils.drawCircleRect(37f, yPos+5, 37f + ((easingHealth / target.maxHealth * 8100).roundToInt() / 100), yPos + 13,3f,Color(255, 255, 0).rgb)
        }
        if (target.health<target.maxHealth/4){
            RenderUtils.drawCircleRect(37f, yPos+5, 37f + ((easingHealth / target.maxHealth * 8100).roundToInt() / 100), yPos + 13,3f,Color(255, 0, 0).rgb)
        }


        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        font.drawString(target.name!!, 37, 5, Color.WHITE.rgb)
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID).locationSkin, 2, 2, 32, 32)
        GL11.glScaled(0.7, 0.7, 0.7)
        "$hp hp".also {
            font.drawString(it, 53 , 23, Color.LIGHT_GRAY.rgb)
        }
    }


    private fun drawRise(target: EntityLivingBase, easingHealth: Float){
        val font=fontValue.get()

        RenderUtils.drawCircleRect(0f,0f,150f,50f,5f,Color(0,0,0,130).rgb)

        val hurtPercent=target.hurtPercent
        val scale=if(hurtPercent==0f){ 1f }
        else if(hurtPercent<0.5f){
            1-(0.2f*hurtPercent*2)
        }else{
            0.8f+(0.2f*(hurtPercent-0.5f)*2)
        }
        val size=30

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1-scale))/scale), ((size * 0.5f * (1-scale))/scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1-hurtPercent, 1-hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.quickDrawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 0, 0, size, size)
        GL11.glPopMatrix()

        font.drawString("Name ${target.name}", 40, 11,Color.WHITE.rgb)
        font.drawString("Distance ${decimalFormat.format(mc.player!!.getDistanceToEntityBox(target))} Hurt ${target.hurtTime}", 40, 11+font.FONT_HEIGHT,Color.WHITE.rgb)

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        fun renderSideway(x: Int,x1: Int){
            RenderUtils.quickDrawGradientSideways(x.toDouble(),39.0, x1.toDouble(),45.0,ColorUtils.hslRainbow(x,indexOffset = 10).rgb,ColorUtils.hslRainbow(x1,indexOffset = 10).rgb)
        }
        val stopPos=(5+((135-font.getStringWidth(decimalFormat.format(target.maxHealth)))*(easingHealth/target.maxHealth))).toInt()
        for(i in 5..stopPos step 5){
            renderSideway(i, (i + 5).coerceAtMost(stopPos))
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        font.drawString(decimalFormat.format(easingHealth),stopPos+5,43-font.FONT_HEIGHT/2,Color.WHITE.rgb)
    }
    private fun drawBest(target: EntityLivingBase, easingHealth: Float){
        val font = fontValue.get()
        val addedLen = (60 + font.getStringWidth(target.name!!) * 1.60f).toFloat()

        RenderUtils.drawRect(0f, 0f, addedLen, 47f, Color(0, 0, 0, 120).rgb)
        RenderUtils.drawRoundedCornerRect(0f, 0f, (easingHealth / target.maxHealth) * addedLen, 47f, 3f, Color(0, 0, 0, 90).rgb)

        RenderUtils.drawShadowWithCustomAlpha(0f, 0f, addedLen, 47f,200F)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin,0,0, size, size)
        GL11.glPopMatrix()

        GL11.glPushMatrix()
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        font.drawString(target.name!!, 39, 8, Color.WHITE.rgb)

        GL11.glPopMatrix()
        font.drawString("Health ${target.health.roundToInt()}", 56, 20 + (font.FONT_HEIGHT * 1.5).toInt(), Color.WHITE.rgb)
    }
    private fun drawFlux(target: EntityLivingBase, nowAnimHP: Float){
        val width = (38 + Fonts.font40.getStringWidth(target.name!!))
            .coerceAtLeast(70)
            .toFloat()

        // draw background
        RenderUtils.drawRect(0F, 0F, width,34F,Color(40,40,40).rgb)
        RenderUtils.drawRect(2F, 22F, width-2F, 24F, Color.BLACK.rgb)
        RenderUtils.drawRect(2F, 28F, width-2F, 30F, Color.BLACK.rgb)

        // draw bars
        RenderUtils.drawRect(2F, 22F, 2+(nowAnimHP / target.maxHealth) * (width-4), 24F, Color(231,182,0).rgb)
        RenderUtils.drawRect(2F, 22F, 2+(getHealth(target) / target.maxHealth) * (width-4), 24F, Color(0, 224, 84).rgb)
        RenderUtils.drawRect(2F, 28F, 2+(target.totalArmorValue / 20F) * (width-4), 30F, Color(77, 128, 255).rgb)

        // draw text
        Fonts.font40.drawString(target.name!!,22,3,Color.WHITE.rgb)
        GL11.glPushMatrix()
        GL11.glScaled(0.7,0.7,0.7)
        Fonts.font35.drawString("Health: ${decimalFormat.format(getHealth(target))}",22/0.7F,(4+Fonts.font40.FONT_HEIGHT)/0.7F,Color.WHITE.rgb)
        GL11.glPopMatrix()

        // Draw head
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 2,2,16,16)
    }
    private fun drawnovoline2(target: EntityLivingBase, easingHealth: Float){
        val width = (38 + Fonts.font40.getStringWidth(target.name!!)).coerceAtLeast(118).toFloat()
        RenderUtils.drawRect(0f, 0f, width + 14f, 44f, Color(0, 0, 0, backgroundalpha.get()).rgb)
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 3,3,30,30)
        Fonts.font40.drawString(target.name!!, 34.5f, 4f, Color.WHITE.rgb)
        Fonts.font40.drawString("Health: ${decimalFormat.format(target.health)}", 34.5f, 14f, Color.WHITE.rgb)
        Fonts.font40.drawString(
            "Distance: ${decimalFormat.format(mc.player!!.getDistanceToEntityBox(target))}m",
            34.5f,
            24f,
            Color.WHITE.rgb
        )
        RenderUtils.drawRect(2.5f, 35.5f, width + 11.5f, 37.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(3f, 36f, 3f + (easingHealth / target.maxHealth) * (width + 8f), 37f, Color(redValue.get(), greenValue.get(), blueValue.get()).rgb)
        RenderUtils.drawRect(2.5f, 39.5f, width + 11.5f, 41.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            3f,
            40f,
            3f + (target.totalArmorValue / 20F) * (width + 8f),
            41f,
            Color(77, 128, 255).rgb
        )
        //easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
    }
    private fun drawnovoline3(target: EntityLivingBase, easingHealth: Float){
        val mainColor = Color(redValue.get(), greenValue.get(), blueValue.get())
        val percent = target.health.toInt()
        val nameLength = (Fonts.font40.getStringWidth(target.name!!)).coerceAtLeast(
            Fonts.font40.getStringWidth(
                "${
                    decimalFormat.format(percent)
                }"
            )
        ).toFloat() + 20F
        val barWidth = (target.health / target.maxHealth).coerceIn(0F, target.maxHealth) * (nameLength - 2F)
        RenderUtils.drawRect(-2F, -2F, 3F + nameLength + 36F, 2F + 36F, Color(50, 50, 50, 150).rgb)
        RenderUtils.drawRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 0,0,36,36)
        Fonts.minecraftFont.drawStringWithShadow(target.name!!, (2 + 36).toFloat(), 2F, -1)
        RenderUtils.drawRect(37F, 14F, 37F + nameLength, 24F, Color(0, 0, 0, 200).rgb)
        //easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
        val animateThingy =
            (easingHealth.coerceIn(target.health, target.maxHealth) / target.maxHealth) * (nameLength - 2F)
        if (easingHealth > target.health)
            RenderUtils.drawRect(38F, 15F, 38F + animateThingy, 23F, mainColor.darker().rgb)
        RenderUtils.drawRect(38F, 15F, 38F + barWidth, 23F, mainColor.rgb)
        Fonts.minecraftFont.drawStringWithShadow("${decimalFormat.format(percent)}", 38F, 26F, Color.WHITE.rgb)
        //Fonts.font35.drawStringWithShadow(
        //"❤",
        ///Fonts.minecraftFont.getStringWidth("${decimalFormat.format(percent)}") + 40F,
        //27F,
        //Color(redValue.get(), greenValue.get(), blueValue.get()).rgb

    }
    private fun drawnewnovo(target: EntityLivingBase, easingHealth: Float){
        val width = (38 + Fonts.minecraftFont.getStringWidth(target.name!!))
            .coerceAtLeast(118)
            .toFloat()
        counter1[0] += 1
        counter2[0] += 1
        counter1[0] = counter1[0].coerceIn(0, 50)
        counter2[0] = counter2[0].coerceIn(0, 80)
        RenderUtils.drawRect(0F, 0F, width, 34.5F, Color(0, 0, 0, backgroundalpha.get()))
        //val customColor = Color(redValue.get(), greenValue.get(), blueValue.get(), 255)
        //val customColor1 = Color(gredValue.get(), ggreenValue.get(), gblueValue.get(), 255)
        val customColor = Color(redValue.get(), greenValue.get(), blueValue.get(), 255)
        val customColor1 = Color(gredValue.get(), ggreenValue.get(), gblueValue.get(), 255)
        RenderUtils.drawGradientSideways(
            34.0, 16.0, width.toDouble() - 2,
            24.0, Color(40, 40, 40, 220).rgb, Color(60, 60, 60, 255).rgb
        )
        RenderUtils.drawGradientSideways(
            34.0, 16.0, (36.0F + (easingHealth / target.maxHealth) * (width - 36.0F)).toDouble() - 2,
            24.0, Palette.fade2(customColor, counter1[0],Fonts.font35.FONT_HEIGHT).rgb,
            Palette.fade2(customColor1, counter2[0], Fonts.font35.FONT_HEIGHT).rgb
        )
        //easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
        Fonts.minecraftFont.drawString(target.name!!, 34, 4, Color(255, 255, 255, 255).rgb)
        RenderUtils.drawHead(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin, 2,2,30,30)
        Fonts.minecraftFont.drawStringWithShadow(
            BigDecimal((target.health / target.maxHealth * 100).toDouble()).setScale(
                1,
                BigDecimal.ROUND_HALF_UP
            ).toString() + "%", width/ 2 + 5, 17F, Color.white.rgb
        )
    }

    private fun drawTenacity(target: EntityLivingBase, easingHealth: Float) {
        val font = fontValue.get()

        val additionalWidth = font.getStringWidth(target.name!!).coerceAtLeast(75)
        val hurtPercent = target.hurtPercent

        //background is halal

        RenderUtils.drawRoundedCornerRect(0f, 5f, 59f + additionalWidth.toFloat(), 45f, 6f, ColorUtils.rainbow().rgb)




        //draw head stuff
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35


        // circle player avatar + rise anims
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        //scale
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        //color
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        //draw
        mc.textureManager.bindTexture(mc.connection!!.getPlayerInfo(target.uniqueID)!!.locationSkin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        GL11.glPopMatrix()



        // info text
        font.drawString(target.name, 45 + (additionalWidth / 2f) - font.getStringWidth(target.name) / 2, 1f + font.FONT_HEIGHT, Color.WHITE.rgb, false)
        val infoStr = ((((easingHealth / target.maxHealth) * 100).roundToInt()).toString() + " - " + ((mc.player!!.getDistanceToEntityBox(target)).roundToInt()).toString() + "M")

        font.drawString(infoStr, 45f + ((additionalWidth - font.getStringWidth(infoStr)) / 2f), 2f + (font.FONT_HEIGHT + font.FONT_HEIGHT).toFloat(), Color.WHITE.rgb, false)



        //hp bar
        RenderUtils.drawRoundedCornerRect(44f, 32f, 44f + additionalWidth, 38f, 2.5f, Color(60, 60, 60, 130).rgb)
        RenderUtils.drawRoundedCornerRect(44f, 32f, 44f + (easingHealth / target.maxHealth) * additionalWidth, 38f, 2.5f, Color(240, 240, 240, 250).rgb)
    }




    private fun getTBorder():Border?{
        return when(modeValue.get().toLowerCase()){
            "novoline" -> Border(0F,0F,140F,40F)
            "astolfo" -> Border(0F,0F,140F,60F)
            "liquid" -> Border(0F,0F
                ,(38 + Fonts.font40.getStringWidth(mc.player!!.name!!).coerceAtLeast(118).toFloat()),36F)
            "flux" -> Border(0F,0F,(38 +Fonts.font40.getStringWidth(mc.player!!.name!!))
                .coerceAtLeast(70)
                .toFloat(),34F)
            "rise" -> Border(0F,0F,150F,55F)
            "zamorozka" -> Border(0F,0F,150F,55F)
            "exhibition" -> Border(0F, 0F, 140F, 45F)
            "best"->Border(0F, 0F, 150F, 47F)
            "novoline2"-> Border(0F,0F,140F,40F)
            "novoline3"-> Border(0F,0F,140F,40F)
            "newnovoline"-> Border(0F,0F,140F,40F)
            "moon"-> Border(0F,0F,140F,40F)

            "watermelon" -> Border(0F, 0F, 120F, 48F)
            "sparklingwater" -> Border(0F, 0F, 120F, 48F)
            "tenacity"-> Border(0F,0F,140F,40F)
            else -> null
        }
    }
}