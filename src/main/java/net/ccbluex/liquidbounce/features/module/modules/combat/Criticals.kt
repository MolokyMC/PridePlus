/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("GrimAC","Packet", "NcpPacket", "NoGround", "Hop", "TPHop", "Jump","WhenJump", "LowJump", "Visual"), "Packet")
    val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    val msTimer = MSTimer()
    var attacks = 0

    override fun onEnable() {
        if (modeValue.get().equals("NoGround", ignoreCase = true))
            mc.player!!.jump()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val player = mc.player ?: return
            val entity = event.targetEntity

            if (!player.onGround || player.isOnLadder || player.isInWeb || player.isInWater ||
                player.isInLava || player.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                Pride.moduleManager[Fly::class.java].state || !msTimer.hasTimePassed(delayValue.get().toLong()))
                return

            val x = player.posX
            val y = player.posY
            val z = player.posZ

            when (modeValue.get().toLowerCase()) {
                "whenjump" -> {
                    if (player.isAirBorne && !player.onGround) player.onCriticalHit(entity)
                }
                "grimac" -> {
                    attacks++
                    if (attacks > 6) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x,y+0.013256,z,false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x,y+0.009856,z,false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                        attacks = 0
                    }
                }
                "packet" -> {
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.0625, z, true))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 1.1E-5, z, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                    player.onCriticalHit(entity)
                }

                "ncppacket" -> {
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.11, z, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.1100013579, z, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.0000013579, z, false))
                    player.onCriticalHit(entity)
                }

                "hop" -> {
                    player.motionY = 0.1
                    player.fallDistance = 0.1f
                    player.onGround = false
                }

                "tphop" -> {
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.02, z, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.01, z, false))
                    player.setPosition(x, y + 0.01, z)
                }
                "jump" -> player.motionY = 0.42
                "lowjump" -> player.motionY = 0.3425
                "visual" -> player.onCriticalHit(entity)
            }

            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if ((packet is CPacketPlayer) && modeValue.get().equals("NoGround", ignoreCase = true))
            packet.onGround = false
    }

    override val tag: String?
        get() = modeValue.get()
}
