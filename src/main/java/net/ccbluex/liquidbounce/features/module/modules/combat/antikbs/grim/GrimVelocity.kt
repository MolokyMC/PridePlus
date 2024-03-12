package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.grim

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.AntiKnockback
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class GrimVelocity : AntiKBMode("GrimAC") {
    private var isVelocity = false
    private var isTeleport = false
    override fun onEnable() {
        isVelocity = false
        isTeleport = false
    }
    override fun onTick(event: TickEvent){
        val connection: NetHandlerPlayClient = mc.connection ?: return

        if (!isVelocity) return

        isVelocity = false

        val systemTime = Minecraft.getSystemTime()

        if ((Pride.moduleManager[AntiKnockback::class.java] as AntiKnockback).sendC03Value.get()) {
            connection.sendPacket(CPacketPlayer(mc.player.onGround))
            mc.timer.lastSyncSysClock = if (mc.player.onGround)
                mc.timer.lastSyncSysClock + 50L
            else
                systemTime
        }

        val pos = BlockPos(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ)
        connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))

        if ((Pride.moduleManager[AntiKnockback::class.java] as AntiKnockback).c0fTestValue.get()){
            connection.sendPacket(CPacketConfirmTransaction())
        }

        if ((Pride.moduleManager[AntiKnockback::class.java] as AntiKnockback).breakValue.get()) {
            mc.world.setBlockToAir(pos)
        }

        mc.timer.lastSyncSysClock = systemTime
    }
    override fun onPacket(event: PacketEvent) {
        val packet: Packet<*> = event.packet
        if (packet is SPacketPlayerPosLook) {
            isTeleport = true
            return
        }
        if ((!isTeleport && packet is SPacketEntityVelocity && packet.entityID == mc.player.entityId) ||
            (packet is SPacketExplosion && (packet.motionX != 0f || packet.motionY != 0f || packet.motionZ != 0f))) {
            isVelocity = true
            event.cancelEvent()
            return
        }
        if (packet.javaClass.getName().startsWith("net.minecraft.network.play.server.SPacket")) isTeleport = false
    }
}
