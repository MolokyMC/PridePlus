package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityVelocity

class AAC520Velocity : AntiKBMode("AAC5.2.0") {
    val mc: Minecraft = Minecraft.getMinecraft()
    override fun onVelocityPacket(event: PacketEvent) {
        if(event.packet is SPacketEntityVelocity) {
            event.cancelEvent()
            mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player.posX, 1.7976931348623157E+308, mc.player.posZ, true))
        }
    }
}