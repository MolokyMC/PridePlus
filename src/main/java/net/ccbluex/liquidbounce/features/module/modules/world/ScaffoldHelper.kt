/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils

/**
 *
 * By WaWa
 * @Date on 2023/5/27
 *
 */
@ModuleInfo(name = "TellyHelper", description = "Toggle Scaffold. For GrimAC",category = ModuleCategory.WORLD)
class ScaffoldHelper : Module() {
    private val modeValue = ListValue("Mode", arrayOf("State"), "State")

    private val scaffoldModeValue = ListValue("ScaffoldMode", arrayOf("Scaffold"/*, "Scaffold3","Scaffold2"*/), "Scaffold")

    private val jumpModeValue = ListValue("JumpMode", arrayOf("mc", "NoEvent", "Key", "Parkour", "Off"), "Off")

    private val timerValue = BoolValue("OnGroundTimer", true)
    private val timerSpeed = FloatValue("TimerSpeed", 0.8F, 0.1F, 10F).displayable { timerValue.get() }

    private val rotationValue = BoolValue("SmartRotation", true)
    private val customPitchValue = FloatValue("CustomPitch",26.5F,0F,90F).displayable { rotationValue.get() }

//    val Scaffold = LiquidBounce.moduleManager[Scaffold::class.java]
//    val LBScaffold = LiquidBounce.moduleManager[LBScaffold::class.java]
//    val ScaffoldNew = LiquidBounce.moduleManager[ScaffoldNew::class.java]
//    val Timer = LiquidBounce.moduleManager[Timer::class.java]
//    val Parkour = LiquidBounce.moduleManager[Parkour::class.java]

    fun jump(){
        if (mc.player.onGround || !mc.player.isAirBorne) {
            when (jumpModeValue.get().toLowerCase()) {
                "mc" -> mc.player!!.jump()
                "noevent" -> mc.player!!.motionY = 0.42
                "key" -> {
                    mc.gameSettings.keyBindJump.pressed = true
                }
            }
        }
    }

    override fun onDisable() {
        when (scaffoldModeValue.get().toLowerCase()) {
            "scaffold" -> Pride.moduleManager[Scaffold::class.java].state = false
        }

        (mc.timer as IMixinTimer).timerSpeed = 1F

        if(!mc.gameSettings.keyBindJump.isKeyDown) mc.gameSettings.keyBindJump.pressed = false
        super.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(jumpModeValue.get() == "Parkour") {
            val player = mc.player ?: return

            if (MovementUtils.isMoving && player.onGround && !player.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown &&
                mc.world!!.getCollisionBoxes(player, player.entityBoundingBox
                    .offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty())
                player.jump()
        }else{
            jump()
        }

        if (rotationValue.get()){
            setYaw()
            mc.player!!.rotationPitch = customPitchValue.get()
        }

        if (mc.player!!.onGround){
            if(timerValue.get())
                (mc.timer as IMixinTimer).timerSpeed = timerSpeed.get()

            if (modeValue.get().toLowerCase() == "state") {
                when (scaffoldModeValue.get().toLowerCase()) {
                    "scaffold" -> Pride.moduleManager[Scaffold::class.java].state = false
                }
            }
        }else {
            if (timerValue.get())
                (mc.timer as IMixinTimer).timerSpeed = 1F
            if (modeValue.get().toLowerCase() == "state") {
                when (scaffoldModeValue.get().toLowerCase()) {
                    "scaffold" -> Pride.moduleManager[Scaffold::class.java].state = true
                }
            }
        }
    }

    private fun setYaw() {
        val player = mc.player!!
        val x = java.lang.Double.valueOf(player.motionX)
        val y = java.lang.Double.valueOf(player.motionZ)
        if (mc.gameSettings.keyBindForward.isKeyDown) {
            if (y != null &&
                y.toDouble() > 0.1
            ) {
                player.rotationYaw = 0.0f
            }
            if (y != null &&
                y.toDouble() < -0.1
            ) {
                player.rotationYaw = 180.0f
            }
            if (x != null &&
                x.toDouble() > 0.1
            ) {
                player.rotationYaw = -90.0f
            }
            if (x != null &&
                x.toDouble() < -0.1
            ) {
                player.rotationYaw = 90.0f
            }
        }

    }
}
