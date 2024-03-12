/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package op.wawa.utils.render

import me.utils.render.GaussianBlur
import me.utils.render.StencilUtil
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import java.awt.Color

object BlurUtils {

    private val shaderGroup = ShaderGroup(Minecraft.getMinecraft().textureManager, Minecraft.getMinecraft().resourceManager, Minecraft.getMinecraft().getFramebuffer(), ResourceLocation("shaders/post/blurarea.json"))
    private val framebuffer = shaderGroup.mainFramebuffer
    private val frbuffer = shaderGroup.getFramebufferRaw("result")

    private var lastFactor = 0
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastWeight = 0

    private var lastX = 0F
    private var lastY = 0F
    private var lastW = 0F
    private var lastH = 0F


    private var lastStrength = 5F


    fun CustomBlurRoundArea(x: Float, y: Float, width: Float, height: Float, radius: Float, BlurStrength: Float) {
        val hud = Pride.moduleManager.getModule(
            HUD::class.java
        ) as HUD
        StencilUtil.initStencilToWrite()
        RenderUtils.drawRoundedRect2(x, y, x + width, y + height, radius, 6, Color(-2).rgb)
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(BlurStrength)
        StencilUtil.uninitStencilBuffer()
    }

    private fun setupFramebuffers() {
        try {
            shaderGroup.createBindFramebuffers(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight)
        } catch (e : Exception) {
            ClientUtils.getLogger().error("Exception caught while setting up shader group", e)
        }
    }

    private fun setValues(strength: Float, x: Float, y: Float, w: Float, h: Float, width: Float, height: Float, force: Boolean = false) {
        if (!force && strength == lastStrength && lastX == x && lastY == y && lastW == w && lastH == h) return
        lastStrength = strength
        lastX = x
        lastY = y
        lastW = w
        lastH = h

        for (i in 0..1) {
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("Radius")?.set(strength)
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("BlurXY")?.set(x, height - y - h)
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("BlurCoord")?.set(w, h)
        }
    }

    @JvmStatic
    fun blur(posX: Float, posY: Float, posXEnd: Float, posYEnd: Float, blurStrength: Float, displayClipMask: Boolean, triggerMethod: () -> Unit) {
        if (!OpenGlHelper.isFramebufferEnabled()) return

        var x = posX
        var y = posY
        var x2 = posXEnd
        var y2 = posYEnd

        if (x > x2) {
            val z = x
            x = x2
            x2 = z
        }

        if (y > y2) {
            val z = y
            y = y2
            y2 = y
        }

        val sc = ScaledResolution(Minecraft.getMinecraft())
        val scaleFactor = sc.scaleFactor
        val width = sc.scaledWidth
        val height = sc.scaledHeight

        if (sizeHasChanged(scaleFactor, width, height)) {
            setupFramebuffers()
            setValues(blurStrength, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat(), true)
        }

        lastFactor = scaleFactor
        lastWidth = width
        lastHeight = height

        setValues(blurStrength, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat())

        framebuffer.bindFramebuffer(true)
        shaderGroup.render(Minecraft.getMinecraft().timer.renderPartialTicks)
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true)

        Stencil.write(displayClipMask)
        triggerMethod()

        Stencil.erase(true)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.pushMatrix()
        GlStateManager.colorMask(true, true, true, false)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        frbuffer.bindFramebufferTexture()
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        val f2 = frbuffer.framebufferWidth.toDouble() / frbuffer.framebufferTextureWidth.toDouble()
        val f3 = frbuffer.framebufferHeight.toDouble() / frbuffer.framebufferTextureHeight.toDouble()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.buffer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex()
        tessellator.draw()
        frbuffer.unbindFramebufferTexture()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.popMatrix()
        GlStateManager.disableBlend()

        Stencil.dispose()
        GlStateManager.enableAlpha()
    }

    @JvmStatic
    fun blurArea(x: Float, y: Float, x2: Float, y2: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(x, y, x2, y2)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
    fun CustomBlurArea(x: Float, y: Float, width: Float, height: Float, BlurStrength: Float) {
        StencilUtil.initStencilToWrite()
        RenderUtils.drawRect(x, y, x + width, y + height, Color(-2).rgb)
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(BlurStrength)
        StencilUtil.uninitStencilBuffer()
    }

    @JvmStatic
    fun blurAreaRounded(x: Float, y: Float, x2: Float, y2: Float, rad: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(x, y, x2, y2, rad)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
    fun blurAreaRounded2(x: Float, y: Float, width: Float, height: Float, radius: Float, blurStrength: Float) {
        StencilUtil.initStencilToWrite()
        RenderUtils.drawRoundedRect2(x, y, x + width, y + height, radius, 6, Color(-2).rgb)
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(blurStrength)
        StencilUtil.uninitStencilBuffer()
    }

    fun shapeBlur(content: Runnable, blurStrength: Float) {
        StencilUtil.initStencilToWrite()
        content.run()
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(blurStrength)
        StencilUtil.uninitStencilBuffer()
    }

    fun sizeHasChanged(scaleFactor: Int, width: Int, height: Int): Boolean = (lastFactor != scaleFactor || lastWidth != width || lastHeight != height)
}


