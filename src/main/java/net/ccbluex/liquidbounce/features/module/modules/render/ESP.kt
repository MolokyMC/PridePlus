/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector3f
import java.awt.Color

@ModuleInfo(name = "ESP", description = "Allows you to see targets through walls.", category = ModuleCategory.RENDER)
class ESP : Module() {
    @JvmField
    val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Outline", "ShaderOutline", "ShaderGlow","yaoer","Rikka"), "Box")

    @JvmField
    val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f)

    @JvmField
    val wireframeWidth = FloatValue("WireFrame-Width", 2f, 0.5f, 5f)
    private val shaderOutlineRadius = FloatValue("ShaderOutline-Radius", 1.35f, 1f, 2f)
    private val shaderGlowRadius = FloatValue("ShaderGlow-Radius", 2.3f, 2f, 3f)
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)
    private val colorTeam = BoolValue("Team", false)

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val mode = modeValue.get()
        val mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)
        val real2d = mode.equals("real2d", ignoreCase = true)

        //<editor-fold desc="Real2D-Setup">
        if (real2d) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableTexture2D()
            GL11.glDepthMask(true)
            GL11.glLineWidth(1.0f)
        }
        //</editor-fold>
        for (entity in mc.world!!.loadedEntityList) {
            if (entity != mc.player && EntityUtils.isSelected(entity, false)) {
                val entityLiving = entity as EntityLivingBase
                val color = getColor(entityLiving)

                when (mode.toLowerCase()) {
                    "box", "otherbox" -> RenderUtils.drawEntityBox(entity, color, !mode.equals("otherbox", ignoreCase = true))
                    "2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val posX: Double = entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX
                        val posY: Double = entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY
                        val posZ: Double = entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
                        RenderUtils.draw2D(entityLiving, posX, posY, posZ, color.rgb, Color.BLACK.rgb)
                    }
                    "yaoer" -> {
                        val pX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosX
                        val  pY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosY
                        val pZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosZ

                        GL11.glPushMatrix()
                        GL11.glTranslatef(pX.toFloat(), (pY + if(entity.isSneaking)0.8F else 1.3F).toFloat(),
                            pZ.toFloat()
                        )
                        GL11.glNormal3f(1.0F, 1.0F, 1.0F)
                        mc.renderManager; GL11.glRotatef(-mc.renderManager.playerViewY, 0.0F, 1.0F, 0.0F)
                        mc.renderManager; GL11.glRotatef(mc.renderManager.playerViewX, 1.0F, 0.0F, 0.0F)
                        val scale = 0.06F
                        GL11.glScalef(-scale, -scale, scale)

                        GL11.glDisable(2896)
                        GL11.glDisable(2929)
                        GL11.glEnable(3042)
                        GL11.glBlendFunc(770, 771)

                        GL11.glPushMatrix()
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)

                        RenderUtils.drawImage("pride/custom_hud_icon.png", -8, -14, 16, 16)
                        GL11.glPopMatrix()
                        GL11.glPopMatrix()
                    }
                    "rikka" -> {
                        val pX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosX
                        val  pY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosY
                        val pZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks -
                                mc.renderManager.renderPosZ

                        GL11.glPushMatrix()
                        GL11.glTranslatef(pX.toFloat(), (pY + if(entity.isSneaking)0.8F else 1.3F).toFloat(),
                            pZ.toFloat()
                        )
                        GL11.glNormal3f(1.0F, 1.0F, 1.0F)
                        mc.renderManager; GL11.glRotatef(-mc.renderManager.playerViewY, 0.0F, 1.0F, 0.0F)
                        mc.renderManager; GL11.glRotatef(mc.renderManager.playerViewX, 1.0F, 0.0F, 0.0F)
                        val scale = 0.06F
                        GL11.glScalef(-scale, -scale, scale)

                        GL11.glDisable(2896)
                        GL11.glDisable(2929)
                        GL11.glEnable(3042)
                        GL11.glBlendFunc(770, 771)

                        GL11.glPushMatrix()
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)

                        RenderUtils.drawImage("pride/rikka.png", -8, -14, 16, 16)
                        GL11.glPopMatrix()
                        GL11.glPopMatrix()
                    }
                    "real2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val bb = entityLiving.entityBoundingBox
                            .offset(-entityLiving.posX, -entityLiving.posY, -entityLiving.posZ)
                            .offset(entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks,
                                entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks,
                                entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks)
                            .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
                        val boxVertices = arrayOf(doubleArrayOf(bb.minX, bb.minY, bb.minZ), doubleArrayOf(bb.minX, bb.maxY, bb.minZ), doubleArrayOf(bb.maxX, bb.maxY, bb.minZ), doubleArrayOf(bb.maxX, bb.minY, bb.minZ), doubleArrayOf(bb.minX, bb.minY, bb.maxZ), doubleArrayOf(bb.minX, bb.maxY, bb.maxZ), doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ), doubleArrayOf(bb.maxX, bb.minY, bb.maxZ))
                        var minX = Float.MAX_VALUE
                        var minY = Float.MAX_VALUE
                        var maxX = -1f
                        var maxY = -1f
                        for (boxVertex in boxVertices) {
                            val screenPos = WorldToScreen.worldToScreen(Vector3f(boxVertex[0].toFloat(), boxVertex[1].toFloat(), boxVertex[2].toFloat()), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight)
                                ?: continue
                            minX = Math.min(screenPos.x, minX)
                            minY = Math.min(screenPos.y, minY)
                            maxX = Math.max(screenPos.x, maxX)
                            maxY = Math.max(screenPos.y, maxY)
                        }
                        if (minX > 0 || minY > 0 || maxX <= mc.displayWidth || maxY <= mc.displayWidth) {
                            GL11.glColor4f(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, 1.0f)
                            GL11.glBegin(GL11.GL_LINE_LOOP)
                            GL11.glVertex2f(minX, minY)
                            GL11.glVertex2f(minX, maxY)
                            GL11.glVertex2f(maxX, maxY)
                            GL11.glVertex2f(maxX, minY)
                            GL11.glEnd()
                        }
                    }
                }
            }
        }
        if (real2d) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val mode = modeValue.get().toLowerCase()
        val shader = (if (mode.equals("shaderoutline", ignoreCase = true)) OutlineShader.OUTLINE_SHADER else if (mode.equals("shaderglow", ignoreCase = true)) GlowShader.GLOW_SHADER else null)
            ?: return
        shader.startDraw(event.partialTicks)
        renderNameTags = false
        try {
            for (entity in mc.world!!.loadedEntityList) {
                if (!EntityUtils.isSelected(entity, false)) continue
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
            }
        } catch (ex: Exception) {
            ClientUtils.getLogger().error("An error occurred while rendering all entities for shader esp", ex)
        }
        renderNameTags = true
        val radius = if (mode.equals("shaderoutline", ignoreCase = true)) shaderOutlineRadius.get() else if (mode.equals("shaderglow", ignoreCase = true)) shaderGlowRadius.get() else 1f
        shader.stopDraw(getColor(null), radius, 1f)
    }

    override val tag: String
        get() = modeValue.get()

    fun getColor(entity: Entity?): Color {
        run {
            if (entity != null && (entity is EntityLivingBase)) {
                val entityLivingBase = entity

                if (entityLivingBase.hurtTime > 0) return Color.RED
                if (EntityUtils.isFriend(entityLivingBase)) return Color.BLUE
                if (colorTeam.get()) {
                    val chars: CharArray = (entityLivingBase.displayName ?: return@run).formattedText.toCharArray()
                    var color = Int.MAX_VALUE
                    for (i in chars.indices) {
                        if (chars[i] != '§' || i + 1 >= chars.size) continue
                        val index = getColorIndex(chars[i + 1])
                        if (index < 0 || index > 15) continue
                        color = ColorUtils.hexColors[index]
                        break
                    }
                    return Color(color)
                }
            }
        }
        return if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }

    companion object {
        @JvmField
        var renderNameTags = true
    }
}