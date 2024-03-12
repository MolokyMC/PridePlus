/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.Breadcrumbs
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.CPacketPlayer.Position
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.server.SPacketEntityVelocity
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


@ModuleInfo(name = "Blink", description = "Blink , but only little Render", category = ModuleCategory.PLAYER)
class Blink : Module() {
    private val packets = LinkedBlockingQueue<Packet<*>>()
    private var disableLogger = false
    private val positions = LinkedList<DoubleArray>()
    private val pulseValue = BoolValue("Pulse", false)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000)
    private val CancelC0f = BoolValue("CancelC0F", false)
    private val CancelAllCpacket = BoolValue("CancelAllClientPacket", false)
    private val CancelServerpacket = BoolValue("CancelServerPacket", false)
    private val inBus = LinkedList<Packet<INetHandlerPlayClient>>()
    var CanBlink: Boolean? = null
    private val pulseTimer = MSTimer()
    override fun onEnable() {
        if (mc.player == null) return
        val player = mc.player ?: return
        pulseTimer.reset()

        synchronized(positions) {
            positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY + player.eyeHeight / 2, player.posZ))
            positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY, player.posZ))
        }
    }

    override fun onDisable() {
        if (mc.player == null) return
        blink()
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.player == null || disableLogger) return

        if (packet is CPacketPlayer) // Cancel all movement stuff
            event.cancelEvent()
        if (packet is Position || packet is PositionRotation ||
            packet is CPacketPlayerTryUseItemOnBlock ||
            packet is CPacketAnimation ||
            packet is CPacketEntityAction || packet is CPacketUseEntity || (packet::class.java.simpleName.startsWith("C", true) && CancelAllCpacket.get())
        ) {
            event.cancelEvent()
            packets.add(packet)
        }
        if (packet is CPacketConfirmTransaction && CancelC0f.get()) {
            event.cancelEvent()
            packets.add(packet)
        }

        if(packet::class.java.getSimpleName().startsWith("S", true) && CancelServerpacket.get())
        {
            if(packet is SPacketEntityVelocity && (mc.world?.getEntityByID(packet.entityID) ?: return) == mc.player){return}
            event.cancelEvent()
            inBus.add(packet as Packet<INetHandlerPlayClient>)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player ?: return

        synchronized(positions) { positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY, player.posZ)) }
        if (pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
            blink()
            pulseTimer.reset()
        }
    }

    override val tag: String
        get() = packets.size.toString()

    private fun blink() {
        try {
            disableLogger = true
            while (!packets.isEmpty()) {
                mc.connection!!.networkManager.sendPacket(packets.take())
            }
            while (!inBus.isEmpty()) {
                inBus.poll()?.processPacket(mc!!.connection)
            }
            disableLogger = false
        } catch (e: Exception) {
            e.printStackTrace()
            disableLogger = false
        }
        synchronized(positions) { positions.clear() }
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val breadcrumbs = Pride.moduleManager.getModule(Breadcrumbs::class.java) as Breadcrumbs?
        val color = if (breadcrumbs!!.colorRainbow.get()) ColorUtils.rainbow() else Color(breadcrumbs.colorRedValue.get(), breadcrumbs.colorGreenValue.get(), breadcrumbs.colorBlueValue.get())
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(color)
            val renderPosX: Double = mc.renderManager.viewerPosX
            val renderPosY: Double = mc.renderManager.viewerPosY
            val renderPosZ: Double = mc.renderManager.viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }
}