/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MinecraftInstance

abstract class SpeedMode(val modeName: String) : MinecraftInstance() {
    val isActive: Boolean
        get() {
            val speed = Pride.moduleManager.getModule(Speed::class.java) as Speed?
            return speed != null && !mc.player!!.isSneaking && speed.state && speed.modeValue.get() == modeName
        }

    abstract fun onMotion(event: MotionEvent)
    abstract fun onUpdate()
    abstract fun onMove(event: MoveEvent)
    open fun onTick() {}
    open fun onEnable() {}
    open fun onDisable() {}

}