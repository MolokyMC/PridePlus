package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.util.concurrent.CopyOnWriteArrayList

@ModuleInfo(name = "JumpCircle", category = ModuleCategory.RENDER, description = "Skid")
class JumpCircle : Module() {
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val radiusValue = IntegerValue("Radius", 3, 1, 5)
    private val widthValue = FloatValue("Width", 0.5f, 0.1f, 50f)
    private val strengthValue = FloatValue("Strength", 0.02f, 0.01f, 0.2f)
    private val circles = CopyOnWriteArrayList<Circle>()
    private var lastOnGround = false
    private var allplayerlastOnGround = false
    override fun onEnable() {
        lastOnGround = true
        allplayerlastOnGround = true
    }
    var allplayer: EntityLivingBase? = null
    @EventTarget
    fun onUpdate(ignored: UpdateEvent?) {
        if (onlyself.get()) {
            if (mc.player!!.onGround && !lastOnGround) {
                circles.add(
                    Circle(
                        mc.player!!.posX,
                        mc.player!!.posY,
                        mc.player!!.posZ,
                        mc.player!!.lastTickPosX,
                        mc.player!!.lastTickPosY,
                        mc.player!!.lastTickPosZ,
                        widthValue.get()
                    )
                )
            }
        } else {
            if (allplayer!!.onGround && !allplayerlastOnGround) {
                circles.add(
                    Circle(
                        allplayer!!.posX,
                        allplayer!!.posY,
                        allplayer!!.posZ,
                        allplayer!!.lastTickPosX,
                        allplayer!!.lastTickPosY,
                        allplayer!!.lastTickPosZ,
                        widthValue.get()
                    )
                )
            }
        }
        lastOnGround = mc.player!!.onGround
        allplayerlastOnGround = allplayer!!.onGround
    }

    @EventTarget
    fun onRender3D(ignored: Render3DEvent?) {
        if (!circles.isEmpty()) {
            for (circle in circles) {
                if (circle.add(strengthValue.get().toDouble()) > radiusValue.get()) {
                    circles.remove(circle)
                    continue
                }
                GL11.glPushMatrix()
                GL11.glTranslated(
                    circle.posX - mc.renderManager.renderPosX,
                    circle.posY - mc.renderManager.renderPosY,
                    circle.posZ - mc.renderManager.renderPosZ
                )
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glLineWidth(circle.width)
                GL11.glColor4f(
                    redValue.get() / 255.0f,
                    greenValue.get() / 255.0f,
                    blueValue.get() / 255.0f,
                    (radiusValue.get() - circle.radius) / radiusValue.get()
                )
                GL11.glRotatef(90f, 1f, 0f, 0f)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                var i = 0
                while (i <= 360) {
                    // You can change circle accuracy  (60 - accuracy)
                    GL11.glVertex2f(
                        (Math.cos(i * Math.PI / 180.0) * circle.radius).toFloat(),
                        (Math.sin(i * Math.PI / 180.0) * circle.radius).toFloat()
                    )
                    i += 5
                }
                GL11.glEnd()
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glPopMatrix()
            }
        }
    }

    internal class Circle(
        var posX: Double,
        var posY: Double,
        var posZ: Double,
        var lastTickPosX: Double,
        var lastTickPosY: Double,
        var lastTickPosZ: Double,
        var width: Float
    ) {
        var radius = 0f
        fun add(radius: Double): Double {
            this.radius += radius.toFloat()
            return this.radius.toDouble()
        }
    }

    companion object {
        val onlyself = BoolValue("OnlySelf", true)
    }
}