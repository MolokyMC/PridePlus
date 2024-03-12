package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aquavit

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving

class AAC4SlowHop : SpeedMode("AAC4SlowHop") {
    override fun onDisable() {
        (mc.timer as IMixinTimer).timerSpeed = 1f
        mc.player!!.speedInAir = 0.02f
    }
    override fun onTick() {}
    override fun onMotion(event: MotionEvent) {}
    override fun onUpdate() {
        if (mc.player!!.isInWater) return

        if (isMoving) {
            if (mc.player!!.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.player!!.jump()
            }
            if (!mc.player!!.onGround && mc.player!!.fallDistance <= 0.1) {
                mc.player!!.speedInAir = 0.02f
                (mc.timer as IMixinTimer).timerSpeed = 1.4f
            }
            if (mc.player!!.fallDistance > 0.1 && mc.player!!.fallDistance < 1.3) {
                mc.player!!.speedInAir = 0.0205f
                (mc.timer as IMixinTimer).timerSpeed = 0.65f
            }
            if (mc.player!!.fallDistance >= 1.3) {
                (mc.timer as IMixinTimer).timerSpeed = 1f
                mc.player!!.speedInAir = 0.02f
            }
        } else {
            mc.player!!.motionX = 0.0
            mc.player!!.motionZ = 0.0
        }
    }
    override fun onMove(event: MoveEvent) {}
    override fun onEnable() {}
}