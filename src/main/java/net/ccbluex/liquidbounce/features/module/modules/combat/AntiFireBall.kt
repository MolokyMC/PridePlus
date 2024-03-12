/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand

@ModuleInfo(name = "AntiFireBall", category = ModuleCategory.COMBAT, description = "Fuck")
class AntiFireBall : Module() {

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val rotationValue = BoolValue("Rotation", true)

    private val timer = MSTimer()

    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        for (entity in mc.world!!.loadedEntityList) {
            if (entity is EntityFireball && mc.player!!.getDistanceToEntityBox(entity) < 5.5 && timer.hasTimePassed(300)) {
                if (rotationValue.get()) {
                    RotationUtils.setTargetRotation(RotationUtils.getRotationsNonLivingEntity(entity))
                }

                mc.connection!!.sendPacket(CPacketUseEntity(entity, EnumHand.MAIN_HAND))

                if (swingValue.equals("Normal")) {
                    mc.player!!.swingArm(EnumHand.MAIN_HAND)
                } else if (swingValue.equals("Packet")) {
                    mc.connection!!.sendPacket(CPacketAnimation())
                }

                timer.reset()
                break
            }
        }
    }
}
