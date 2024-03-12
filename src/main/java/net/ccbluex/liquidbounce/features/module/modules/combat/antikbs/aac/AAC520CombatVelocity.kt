package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityVelocity

class AAC520CombatVelocity : AntiKBMode("AAC5.2.0Combat") {
    val mc: Minecraft = Minecraft.getMinecraft()

    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0
    override fun onEnable() {
        templateX = 0
        templateY = 0
        templateZ = 0
    }

    override fun onVelocity(event: UpdateEvent) {
        if (mc.player.hurtTime> 0 && velocity.velocityInput) {
            velocity.velocityInput = false
            mc.player.motionX = 0.0
            mc.player.motionZ = 0.0
            mc.player.motionY = 0.0
            mc.player.jumpMovementFactor = -0.002f
            mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player.posX, 1.7976931348623157E+308, mc.player.posZ, true))
        }
        if (velocity.velocityTimer.hasTimePassed(80L) && velocity.velocityInput) {
            velocity.velocityInput = false
            mc.player.motionX = templateX / 8000.0
            mc.player.motionZ = templateZ / 8000.0
            mc.player.motionY = templateY / 8000.0
            mc.player.jumpMovementFactor = -0.002f
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is SPacketEntityVelocity) {
            event.cancelEvent()
            velocity.velocityInput = true
            templateX = packet.motionX
            templateZ = packet.motionZ
            templateY = packet.motionY
        }
    }
}