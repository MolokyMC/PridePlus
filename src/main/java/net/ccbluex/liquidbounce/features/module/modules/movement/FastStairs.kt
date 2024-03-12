/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.minecraft.block.BlockStairs
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "FastStairs", description = "Allows you to climb up stairs faster.", category = ModuleCategory.MOVEMENT)
class FastStairs : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Step", "NCP", "AAC3.1.0", "AAC3.3.6", "AAC3.3.13"), "NCP")
    private val longJumpValue = BoolValue("LongJump", false)

    private var canJump = false

    private var walkingDown = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (!MovementUtils.isMoving || Pride.moduleManager[Speed::class.java].state)
            return

        if (player.fallDistance > 0 && !walkingDown)
            walkingDown = true
        else if (player.posY > player.prevChasingPosY)
            walkingDown = false

        val mode = modeValue.get()

        if (!player.onGround)
            return

        val blockPos = BlockPos(player.posX, player.entityBoundingBox.minY, player.posZ)

        if ((getBlock(blockPos) is BlockStairs) && !walkingDown) {
            player.setPosition(player.posX, player.posY + 0.5, player.posZ)

            val motion = when {
                mode.equals("NCP", ignoreCase = true) -> 1.4
                mode.equals("AAC3.1.0", ignoreCase = true) -> 1.5
                mode.equals("AAC3.3.13", ignoreCase = true) -> 1.2
                else -> 1.0
            }

            player.motionX *= motion
            player.motionZ *= motion
        }

        if (getBlock(blockPos.down()) is BlockStairs) {
            if (walkingDown) {
                when {
                    mode.equals("NCP", ignoreCase = true) ->
                        player.motionY = -1.0
                    mode.equals("AAC3.3.13", ignoreCase = true) ->
                        player.motionY -= 0.014
                }

                return
            }

            val motion = when {
                mode.equals("NCP", ignoreCase = true) -> 1.3
                mode.equals("AAC3.1.0", ignoreCase = true) -> 1.3
                mode.equals("AAC3.3.6", ignoreCase = true) -> 1.48
                mode.equals("AAC3.3.13", ignoreCase = true) -> 1.52
                else -> 1.3
            }

            player.motionX *= motion
            player.motionZ *= motion
            canJump = true
        } else if (mode.startsWith("AAC", ignoreCase = true) && canJump) {
            if (longJumpValue.get()) {
                player.jump()
                player.motionX *= 1.35
                player.motionZ *= 1.35
            }

            canJump = false
        }
    }

    override val tag: String
        get() = modeValue.get()
}