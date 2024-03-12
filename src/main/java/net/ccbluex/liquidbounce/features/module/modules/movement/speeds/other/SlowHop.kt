/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class SlowHop : SpeedMode("SlowHop") {
    override fun onMotion(event: MotionEvent) {
        if (mc.player!!.isInWater) return
        if (MovementUtils.isMoving) {
            if (mc.player!!.onGround) mc.player!!.jump() else MovementUtils.strafe(MovementUtils.speed * 1.011f)
        } else {
            mc.player!!.motionX = 0.0
            mc.player!!.motionZ = 0.0
        }
    }

    override fun onUpdate() {}
    override fun onMove(event: MoveEvent) {}
}