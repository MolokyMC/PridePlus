/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.spartan

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer

class SpartanYPort : SpeedMode("SpartanYPort") {
    private var airMoves = 0
    override fun onMotion(event: MotionEvent) {
        if (mc.gameSettings.keyBindForward.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown) {
            if (mc.player!!.onGround) {
                mc.player!!.jump()
                airMoves = 0
            } else {
                (mc.timer as IMixinTimer).timerSpeed = 1.08f
                if (airMoves >= 3) mc.player!!.jumpMovementFactor = 0.0275f
                if (airMoves >= 4 && airMoves % 2.toDouble() == 0.0) {
                    mc.player!!.motionY = -0.32f - 0.009 * Math.random()
                    mc.player!!.jumpMovementFactor = 0.0238f
                }
                airMoves++
            }
        }
    }

    override fun onUpdate() {}
    override fun onMove(event: MoveEvent) {}
}