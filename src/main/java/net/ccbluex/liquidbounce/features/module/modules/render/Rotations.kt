/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.RENDER)
class Rotations : Module() {

    private val bodyValue = BoolValue("Body", true)

    private var playerYaw: Float? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (RotationUtils.serverRotation != null && !bodyValue.get())
            mc.player?.rotationYawHead = RotationUtils.serverRotation.yaw
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.player

        if (!bodyValue.get() || player == null)
            return

        val packet = event.packet

        if (packet is CPacketPlayer.PositionRotation || packet is CPacketPlayer.Rotation) {
            val packetPlayer = packet as CPacketPlayer

            playerYaw = packetPlayer.yaw

            player.renderYawOffset = packetPlayer.yaw
            player.rotationYawHead = packetPlayer.yaw
        } else {
            if (playerYaw != null)
                player.renderYawOffset = this.playerYaw!!

            player.rotationYawHead = player.renderYawOffset
        }
    }

    private fun getState(module: Class<*>) = Pride.moduleManager[module]!!.state
}
