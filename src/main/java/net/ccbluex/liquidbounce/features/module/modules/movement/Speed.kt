/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement


import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aquavit.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.spartan.SpartanYPort
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanHop2
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanYPort
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import java.util.*

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT)
class Speed : Module() {
    private val speedModes = arrayOf(
        VulcanHop(),
        VulcanHop2(),
        VulcanYPort(),
        AAC4Hop(),
        AAC4SlowHop(),
        SpartanYPort(),
        SlowHop(),
        CustomSpeed()
    )

    val modeValue: ListValue = object : ListValue("Mode", modes, "NCPBHop") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state)
                onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state)
                onEnable()
        }
    }
    val customSpeedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f)
    val customYValue = FloatValue("CustomY", 0f, 0f, 4f)
    val customTimerValue = FloatValue("CustomTimer", 1f, 0.1f, 2f)
    val customStrafeValue = BoolValue("CustomStrafe", true)
    val resetXZValue = BoolValue("CustomResetXZ", false)
    val resetYValue = BoolValue("CustomResetY", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player ?: return

        if (player.isSneaking)
            return

        if (MovementUtils.isMoving) {
            player.isSprinting = true
        }

        mode?.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.player ?: return

        if (player.isSneaking || event.eventState != EventState.PRE)
            return

        if (MovementUtils.isMoving)
            player.isSprinting = true

        mode?.onMotion(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        if (mc.player!!.isSneaking)
            return
        mode?.onMove(event!!)
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (mc.player!!.isSneaking)
            return

        mode?.onTick()
    }

    override fun onEnable() {
        if (mc.player == null)
            return

        (mc.timer as IMixinTimer).timerSpeed = 1f

        mode?.onEnable()
    }

    override fun onDisable() {
        if (mc.player == null)
            return

        (mc.timer as IMixinTimer).timerSpeed = 1f

        mode?.onDisable()
    }

    override val tag: String
        get() = modeValue.get()

    private val mode: SpeedMode?
        get() {
            val mode = modeValue.get()

            for (speedMode in speedModes) if (speedMode.modeName.equals(mode, ignoreCase = true))
                return speedMode

            return null
        }

    private val modes: Array<String>
        get() {
            val list: MutableList<String> = ArrayList()
            for (speedMode in speedModes) list.add(speedMode.modeName)
            return list.toTypedArray()
        }
}