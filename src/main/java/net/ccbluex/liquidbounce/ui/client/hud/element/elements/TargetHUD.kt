package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import com.mojang.realmsclient.gui.ChatFormatting
import me.utils.render.BlurBuffer
import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "TargetHUD")
class TargetHUD : Element(-46.0,-40.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    private val bV = BoolValue("Blur", true)
    private val BlurStrength = FloatValue("BlurStrength", 5f,0f,20f)
    val shadowValueopen = BoolValue("shadow", true)
    private val shadowValue = FloatValue("shadow-Value", 10F, 0f, 20f)
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom"), "Background")
    private val r = IntegerValue("shadow-Red", 0, 0, 255)
    private val g = IntegerValue("shadow-Green", 0, 0, 255)
    private val b = IntegerValue("shadow-Blue", 255, 0, 255)
    private val shadowalpha = IntegerValue("Shadow-Alpha", 255, 0, 255)
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)
    private val modeValue = ListValue("Mode", arrayOf("distance"), "distance")
    private val animSpeedValue = IntegerValue("AnimSpeed",10,5,20)
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed",8,5,40)



    private var prevTarget: EntityLivingBase?=null
    private var lastHealth=20F
    private var lastChangeHealth=20F
    private var changeTime=System.currentTimeMillis()
    private var displayPercent=0f
    private var lastUpdate = System.currentTimeMillis()
    private fun getHealth(entity: EntityLivingBase?):Float{
        return if(entity==null || entity.isDead){ 0f }else{ entity.health }
    }
    override fun drawElement(): Border? {
        var target=(Pride.moduleManager[KillAura::class.java] as KillAura).target
        val time=System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get()*50f)
        lastUpdate=System.currentTimeMillis()

        if ((mc.currentScreen is GuiHudDesigner)|| (mc.currentScreen is GuiChat)) {
            target=mc.player
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


        when(modeValue.get().toLowerCase()) {
            "distance" -> distance(prevTarget!!,nowAnimHP)
        }

        return getTBorder()
    }


    private fun distance(target: EntityLivingBase, easingHealth: Float) {
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
                        0f, 0f, 150F, 30F, radiusValue.get(),
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
                RenderUtils.fastRoundedRect(0f, 0f, 150F, 30F, radiusValue.get())
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
                GL11.glPopMatrix()
            }
            )
        }
        GL11.glPopMatrix()
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslated(renderX, renderY, 0.0)

        Fonts.font35.drawString(target.name!!, 36, 6, -1)
        Fonts.font35.drawString("Distance:   " + ChatFormatting.WHITE + Math.round(target.getDistance(mc.player!!.posX,mc.player!!.posY,mc.player!!.posZ)) + "m", 36, 18, Color(0, 162, 255).rgb)
        RenderUtils.drawCircle(123f, 15f,10f, -90, (270f * (easingHealth / 20f)).toInt(), Color(0, 162, 255))
        Fonts.font35.drawCenteredString(Math.round(easingHealth).toString(), 123.1f, 12f, -1)
        RoundedUtil.drawRound(5.1F,3.8f,23F,23F,3f, Color(0,162,255,255))
        //Fonts.font35.drawString("d",5f,6.8f,Color(0,0,0,255).rgb)
        //blur
        if (bV.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            BlurBuffer.CustomBlurRoundArea(
                    renderX.toFloat(),
                    renderY.toFloat(),
                    150F,
                    30F,
                    radiusValue.get(), BlurStrength.get()
            )
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
        Fonts.font35.drawString(target.name!!, 36, 6, -1)
        Fonts.font35.drawString("Distance:   " + ChatFormatting.WHITE + Math.round(target.getDistance(mc.player!!.posX,mc.player!!.posY,mc.player!!.posZ)) + "m", 36, 18, Color(0, 162, 255).rgb)
        RenderUtils.drawCircle(123f, 15f,10f, -90, (270f * (easingHealth / 20f)).toInt(), Color(0, 162, 255))
        Fonts.font35.drawCenteredString(Math.round(easingHealth).toString(), 123.1f, 12f, -1)
        RoundedUtil.drawRound(5.1F,3.8f,23F,23F,3f, Color(0,162,255,255))
        //Fonts.nbicon45.drawString("d",5f,6.8f,Color(0,0,0,255).rgb)
    }

    private fun getTBorder():Border?{
        return when(modeValue.get().toLowerCase()){
            "distance"->Border(0F,0F,150F,30F)
            "home" -> Border(0F,0F,Fonts.font40.getStringWidth("Name: " + "SBSBSB") + 65f,50f)
            else -> null
        }

    }
}