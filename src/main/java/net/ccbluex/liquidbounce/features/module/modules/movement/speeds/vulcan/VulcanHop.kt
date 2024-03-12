package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanHop : SpeedMode("VulcanHop") {
	
    private var wasTimer = false
    override fun onTick() {}
    override fun onMotion(event: MotionEvent) {}
    override fun onMove(event: MoveEvent) {}

    override fun onUpdate() {
        if (wasTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1.00f
            wasTimer = false
        }
        if (Math.abs(mc.player!!.movementInput.moveStrafe) < 0.1f) {
            mc.player!!.jumpMovementFactor = 0.026499f
        }else {
            mc.player!!.jumpMovementFactor = 0.0244f
        }
        mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown

        if (MovementUtils.speed < 0.215f && !mc.player!!.onGround) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.player!!.onGround && MovementUtils.isMoving) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.player!!.jump()
	    if (!mc.player!!.isAirBorne) {
                return //Prevent flag with Fly
            }
            (mc.timer as IMixinTimer).timerSpeed = 1.25f
	    wasTimer = true
	    MovementUtils.strafe()
	    if(MovementUtils.speed < 0.5f) {
	        MovementUtils.strafe(0.4849f)
	    }
        }else if (!MovementUtils.isMoving) {
            (mc.timer as IMixinTimer).timerSpeed = 1.00f
            mc.player!!.motionX = 0.0
            mc.player!!.motionZ = 0.0
        }
    }
}
