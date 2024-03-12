/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils

class CustomSpeed : SpeedMode("Custom") {
    override fun onMotion(event: MotionEvent) {
        if (MovementUtils.isMoving) {
            val speed = Pride.moduleManager.getModule(Speed::class.java) as Speed? ?: return
            (mc.timer as IMixinTimer).timerSpeed = speed.customTimerValue.get()
            when {
                mc.player!!.onGround -> {
                    MovementUtils.strafe(speed.customSpeedValue.get())
                    mc.player!!.motionY = speed.customYValue.get().toDouble()
                }
                speed.customStrafeValue.get() -> MovementUtils.strafe(speed.customSpeedValue.get())
                else -> MovementUtils.strafe()
            }
        } else {
            mc.player!!.motionZ = 0.0
            mc.player!!.motionX = mc.player!!.motionZ
        }
    }

    override fun onEnable() {
        val speed = Pride.moduleManager.getModule(Speed::class.java) as Speed? ?: return
        if (speed.resetXZValue.get()) {
            mc.player!!.motionZ = 0.0
            mc.player!!.motionX = mc.player!!.motionZ
        }
        if (speed.resetYValue.get()) mc.player!!.motionY = 0.0
        super.onEnable()
    }

    override fun onDisable() {
        (mc.timer as IMixinTimer).timerSpeed = 1f
        super.onDisable()
    }

    override fun onUpdate() {}
    override fun onMove(event: MoveEvent) {}
}