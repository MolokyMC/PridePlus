package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.client.Minecraft

class AAC5ReduceVelocity : AntiKBMode("AAC5Reduce") {

    val mc: Minecraft = Minecraft.getMinecraft()
    override fun onVelocity(event: UpdateEvent) {
        if (mc.player.hurtTime> 1 && velocity.velocityInput) {
            mc.player.motionX *= 0.81
            mc.player.motionZ *= 0.81
        }
        if (velocity.velocityInput && (mc.player.hurtTime <5 || mc.player.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }
}