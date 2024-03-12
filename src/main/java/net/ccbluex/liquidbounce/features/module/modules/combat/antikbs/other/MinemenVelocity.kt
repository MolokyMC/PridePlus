package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.SPacketEntityVelocity

class MinemenVelocity : AntiKBMode("Minemen") {

    // created by dg636 $$
    
    private var ticks = 0
    private var lastCancel = false
    private var canCancel = false
    
    override fun onUpdate(event: UpdateEvent) {
        ticks ++
        if (ticks > 23) {
            canCancel = true
        }
        if (ticks >= 2 && ticks <= 4 && !lastCancel) {
            mc.player!!.motionX *= 0.99
            mc.player!!.motionZ *= 0.99
        } else if (ticks == 5 && !lastCancel) {
            MovementUtils.strafe()
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is SPacketEntityVelocity) {
            if (mc.player == null || (mc.world?.getEntityByID(packet.entityID) ?: return) != mc.player) return
            ticks = 0
            if (canCancel) {
                event.cancelEvent()
                lastCancel = true
                canCancel = false
            } else {
                mc.player!!.jump()
                lastCancel = false
            }
        }
    }
}
