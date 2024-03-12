package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import kotlin.math.*

@ModuleInfo(name = "Strafe", description = "Allows you to freely move in mid air.", category = ModuleCategory.MOVEMENT)
class Strafe : Module() {

    private var strengthValue= FloatValue("Strength", 0.5F, 0F, 1F)
    private var noMoveStopValue = BoolValue("NoMoveStop", false)
    private var onGroundStrafeValue = BoolValue("OnGroundStrafe", false)
    private var allDirectionsJumpValue = BoolValue("AllDirectionsJump", false)

    private var wasDown: Boolean = false
    private var jump: Boolean = false

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (jump) {
            event.cancelEvent()
        }
    }

    override fun onEnable() {
        wasDown = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.player!!.onGround && mc.gameSettings.keyBindJump.isKeyDown && allDirectionsJumpValue.get() && (mc.player!!.movementInput.moveForward != 0F || mc.player!!.movementInput.moveStrafe != 0F) && !(mc.player!!.isInWater || mc.player!!.isInLava || mc.player!!.isOnLadder || mc.player!!.isInWeb)) {
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.gameSettings.keyBindJump.pressed = false
                wasDown = true
            }
            val yaw = mc.player!!.rotationYaw
            mc.player!!.rotationYaw = getMoveYaw()
            mc.player!!.jump()
            mc.player!!.rotationYaw = yaw
            jump = true
            if (wasDown) {
                mc.gameSettings.keyBindJump.pressed = true
                wasDown = false
            }
        } else {
            jump = false
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val shotSpeed = sqrt((mc.player!!.motionX * mc.player!!.motionX) + (mc.player!!.motionZ * mc.player!!.motionZ))
        val speed = (shotSpeed * strengthValue.get())
        val motionX = (mc.player!!.motionX * (1 - strengthValue.get()))
        val motionZ = (mc.player!!.motionZ * (1 - strengthValue.get()))
        if (!(mc.player!!.movementInput.moveForward != 0F || mc.player!!.movementInput.moveStrafe != 0F)) {
            if (noMoveStopValue.get()) {
                mc.player!!.motionX = 0.0
                mc.player!!.motionZ = 0.0
            }
            return
        }
        if (!mc.player!!.onGround || onGroundStrafeValue.get()) {
            val yaw = getMoveYaw()
            mc.player!!.motionX = (((-sin(Math.toRadians(yaw.toDouble())) * speed) + motionX))
            mc.player!!.motionZ = (((cos(Math.toRadians(yaw.toDouble())) * speed) + motionZ))
        }
    }


    private fun getMoveYaw(): Float {
        var moveYaw = mc.player!!.rotationYaw
        if (mc.player!!.moveForward != 0F && mc.player!!.moveStrafing == 0F) {
            moveYaw += if(mc.player!!.moveForward > 0) 0 else 180
        } else if (mc.player!!.moveForward != 0F && mc.player!!.moveStrafing != 0F) {
            if (mc.player!!.moveForward > 0) {
                moveYaw += if (mc.player!!.moveStrafing > 0) -45 else 45
            } else {
                moveYaw -= if (mc.player!!.moveStrafing > 0) -45 else 45
            }
            moveYaw += if(mc.player!!.moveForward > 0) 0 else 180
        } else if (mc.player!!.moveStrafing != 0F && mc.player!!.moveForward == 0F) {
            moveYaw += if(mc.player!!.moveStrafing > 0) -90 else 90
        }
        return moveYaw
    }
}
