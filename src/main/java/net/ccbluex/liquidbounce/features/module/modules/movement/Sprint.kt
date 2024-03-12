/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.init.MobEffects;

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {

    val allDirectionsValue = BoolValue("AllDirections", true)
    val noPacketPatchValue = BoolValue("AllDir-NoPacketsPatch", true).displayable { allDirectionsValue.get() }
    val moveDirPatchValue = BoolValue("AllDir-MoveDirPatch", false).displayable { allDirectionsValue.get() }
    val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)

    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (allDirectionsValue.get() && noPacketPatchValue.get()) {
            if (packet is CPacketEntityAction && (packet.action == CPacketEntityAction.Action.STOP_SPRINTING || packet.action == CPacketEntityAction.Action.START_SPRINTING)) {
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val KillAura = Pride.moduleManager[KillAura::class.java] as KillAura

        if (!MovementUtils.isMoving || mc.player.isSneaking ||
            (blindnessValue.get() && mc.player.isPotionActive(MobEffects.BLINDNESS)) ||
            (foodValue.get() && !(mc.player.foodStats.foodLevel > 6.0F || mc.player.capabilities.allowFlying))
            || (checkServerSide.get() && (mc.player.onGround || !checkServerSideGround.get())
                    && !allDirectionsValue.get() && RotationUtils.targetRotation != null &&
                    RotationUtils.getRotationDifference(Rotation(mc.player.rotationYaw, mc.player.rotationPitch)) > 30F)) {
            mc.player.isSprinting = false
            return
        }

        if (allDirectionsValue.get() || mc.player.movementInput.moveForward >= 0.8F)
            mc.player.isSprinting = true

        if (allDirectionsValue.get() && moveDirPatchValue.get() && KillAura.target == null)
            RotationUtils.setTargetRotation(Rotation(MovementUtils.getRawDirection(), mc.player.rotationPitch))
    }
}