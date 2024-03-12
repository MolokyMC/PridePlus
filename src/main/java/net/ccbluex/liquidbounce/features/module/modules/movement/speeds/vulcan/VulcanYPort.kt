package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanYPort : SpeedMode("VulcanYPort") {
	
    private var wasTimer = false
    private var ticks = 0
    override fun onTick() {}
    override fun onMotion(event: MotionEvent) {}
    override fun onMove(event: MoveEvent) {}

    override fun onUpdate() {
         ticks++
         if (wasTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1.00f
            wasTimer = false
        }
        mc.player!!.jumpMovementFactor = 0.0245f
        if (!mc.player!!.onGround && ticks > 3 && mc.player!!.motionY > 0) {
            mc.player!!.motionY = -0.27
        }

        mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
        if (MovementUtils.speed < 0.215f && !mc.player!!.onGround) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.player!!.onGround && MovementUtils.isMoving) {
            ticks = 0
            mc.gameSettings.keyBindJump.pressed = false
            mc.player!!.jump()
	    if (!mc.player!!.isAirBorne) {
                return //Prevent flag with Fly
            }
            (mc.timer as IMixinTimer).timerSpeed = 1.4f
            wasTimer = true
            if(MovementUtils.speed < 0.48f) {
                MovementUtils.strafe(0.48f)
            }else{
                MovementUtils.strafe((MovementUtils.speed*0.985).toFloat())
            }
        }else if (!MovementUtils.isMoving) {
            (mc.timer as IMixinTimer).timerSpeed = 1.00f
            mc.player!!.motionX = 0.0
            mc.player!!.motionZ = 0.0
        }
    }
}
