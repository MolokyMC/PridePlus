package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.SPacketEntityVelocity

class AAC4ReduceVelocity : AntiKBMode("AAC4Reduce") {
    val mc: Minecraft = Minecraft.getMinecraft()
    override fun onVelocity(event: UpdateEvent) {
        if (mc.player.hurtTime> 0 && !mc.player.onGround && velocity.velocityInput && velocity.velocityTimer.hasTimePassed(80L)) {
            mc.player.motionX *= 0.62
            mc.player.motionZ *= 0.62
        }
        if (velocity.velocityInput && (mc.player.hurtTime <4 || mc.player.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is SPacketEntityVelocity) {
            velocity.velocityInput = true
            packet.motionX = (packet.motionX * 0.6).toInt()
            packet.motionZ = (packet.motionZ * 0.6).toInt()
        }
    }
}