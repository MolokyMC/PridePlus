/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo(name = "LongJump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
class LongJump : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "AACv1", "AACv2", "Mineplex", "Mineplex3", "Redesky"), "NCP")
    private val ncpBoostValue = FloatValue("NCPBoost", 4.25f, 1f, 10f)
    private val autoJumpValue = BoolValue("AutoJump", false)
    private var jumped = false
    private var canBoost = false
    private var teleported = false
    private var canMineplexBoost = false

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (LadderJump.jumped)
            MovementUtils.strafe(MovementUtils.speed * 1.08f)

        val player = mc.player ?: return

        if (jumped) {
            val mode = modeValue.get()

            if (player.onGround || player.capabilities.isFlying) {
                jumped = false
                canMineplexBoost = false

                if (mode.equals("NCP", ignoreCase = true)) {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
                return
            }
            run {
                when (mode.toLowerCase()) {
                    "ncp" -> {
                        MovementUtils.strafe(MovementUtils.speed * if (canBoost) ncpBoostValue.get() else 1f)
                        canBoost = false
                    }
                    "aacv1" -> {
                        player.motionY += 0.05999
                        MovementUtils.strafe(MovementUtils.speed * 1.08f)
                    }
                    "aacv2", "mineplex3" -> {
                        player.jumpMovementFactor = 0.09f
                        player.motionY += 0.0132099999999999999999999999999
                        player.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "mineplex" -> {
                        player.motionY += 0.0132099999999999999999999999999
                        player.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "redesky" -> {
                        player.jumpMovementFactor = 0.15f
                        player.motionY += 0.05f
                    }
                }
            }
        }
        if (autoJumpValue.get() && player.onGround && MovementUtils.isMoving) {
            jumped = true
            player.jump()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.player ?: return
        val mode = modeValue.get()

        if (mode.equals("mineplex3", ignoreCase = true)) {
            if (player.fallDistance != 0.0f)
                player.motionY += 0.037
        } else if (mode.equals("ncp", ignoreCase = true) && !MovementUtils.isMoving && jumped) {
            player.motionX = 0.0
            player.motionZ = 0.0
            event.zeroXZ()
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        canBoost = true
        teleported = false

        if (state) {
            when (modeValue.get().toLowerCase()) {
                "mineplex" -> event.motion = event.motion * 4.08f
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
