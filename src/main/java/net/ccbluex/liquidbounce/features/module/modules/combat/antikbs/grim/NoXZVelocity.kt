package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.grim

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.EnumHand

class NoXZVelocity : AntiKBMode("NoXZ") {
    /**
     * VALUES
     */
    private var grimNoAntiKB = 0
    private var cancel = false

    private var velocityTimer = MSTimer()
    private var velocityInput = false
    private var press = false
    private var grimReduce = 0

    var x = 0.0
    var y = -0.1
    var z = 0.0

    override fun onDisable() {
        cancel = false
        grimNoAntiKB = 0
        mc.player?.speedInAir = 0.02F
        grimReduce = 0
        velocityInput = false
        (mc.timer as IMixinTimer).timerSpeed = 1f
        press = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if (mc.player.isInWater || mc.player.isInLava || mc.player.isInWeb)
            return
        if (mc.player == null)
            return

        if (velocityInput) {
            if (mc.player.hurtTime == 0) velocityInput = false
        }
    }

    override fun onTick(event: TickEvent) {
        if (velocityInput) {
            val reach = 3.0
            val currentRotation = RotationUtils.serverRotation!!

            val raycastedEntity = RaycastUtils.raycastEntity(reach, currentRotation.yaw, currentRotation.pitch,
                object : RaycastUtils.EntityFilter {
                    override fun canRaycast(entity: Entity?): Boolean {
                        return true
                    }

                })

            if (raycastedEntity != null && raycastedEntity != mc.player) {
                repeat(5) {
                    Pride.eventManager.callEvent(AttackEvent(raycastedEntity))
                    mc.connection!!.sendPacket(CPacketUseEntity(raycastedEntity))
                    mc.player!!.swingArm(EnumHand.MAIN_HAND)
                }
                velocityInput = false
            }
        }

    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is SPacketEntityVelocity) {
            if (mc.player == null || (mc.world?.getEntityByID(packet.entityID) ?: return) != mc.player) return

            velocityTimer.reset()

            x = mc.player.posX
            y = mc.player.posY
            z = mc.player.posZ

            velocityInput = true
        }
        if (packet is SPacketExplosion) {
            // TODO: Support velocity for explosions
            event.cancelEvent()
        }
    }
    override fun onAttack(event: AttackEvent) {
        if (velocityInput) {
            if (!mc.player.isSprinting) {
                val player = mc.player ?: return
                if (player.isSprinting) mc.connection!!.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING))
                mc.connection!!.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING))

                player.isSprinting = true
                player.serverSprintState = true
            }

            mc.player.motionX *= 0.6
            mc.player.motionZ *= 0.6
        }
    }
}
