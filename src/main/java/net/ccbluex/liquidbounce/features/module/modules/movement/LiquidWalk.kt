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
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "LiquidWalk", description = "Allows you to walk on water.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_J)
class LiquidWalk : Module() {
    val modeValue = ListValue("Mode", arrayOf("AAC", "AAC3.3.11", "AACFly", "Spartan", "Dolphin"), "NCP")
    private val noJumpValue = BoolValue("NoJump", false)
    private val aacFlyValue = FloatValue("AACFlyMotion", 0.5f, 0.1f, 1f)

    private var nextTick = false

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player

        if (player == null || player.isSneaking) return

        when (modeValue.get().toLowerCase()) {
            "aac" -> {
                val blockPos = player.position.down()
                if (!player.onGround && getBlock(blockPos) == Blocks.WATER || player.isInWater) {
                    if (!player.isSprinting) {
                        player.motionX *= 0.99999
                        player.motionY *= 0.0
                        player.motionZ *= 0.99999
                    } else {
                        player.motionX *= 0.99999
                        player.motionY *= 0.0
                        player.motionZ *= 0.99999
                    }
                    if (player.fallDistance >= 4) player.motionY = -0.004 else if (player.isInWater) player.motionY = 0.09
                }
                if (player.hurtTime != 0) player.onGround = false
            }
            "spartan" -> if (player.isInWater) {
                val block = getBlock(BlockPos(player.posX, player.posY + 1, player.posZ))
                val blockUp = getBlock(BlockPos(player.posX, player.posY + 1.1, player.posZ))

                if (blockUp is BlockLiquid) {
                    player.motionY = 0.1
                } else if (block is BlockLiquid) {
                    player.motionY = 0.0
                }

                player.onGround = true
                player.motionX *= 1.085
                player.motionZ *= 1.085
            }
            "aac3.3.11" -> if (player.isInWater) {
                player.motionX *= 1.17
                player.motionZ *= 1.17
                if (mc.world!!.getBlockState(BlockPos(player.posX, player.posY + 1.0, player.posZ)).block != Blocks.AIR) player.motionY += 0.04
            }
            "dolphin" -> if (player.isInWater) player.motionY += 0.03999999910593033
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if ("aacfly" == modeValue.get().toLowerCase() && mc.player!!.isInWater) {
            event.y = aacFlyValue.get().toDouble()
            mc.player!!.motionY = aacFlyValue.get().toDouble()
        }
    }


    @EventTarget
    fun onJump(event: JumpEvent) {
        val player = mc.player ?: return

        val block = getBlock(BlockPos(player.posX, player.posY - 0.01, player.posZ))

        if (noJumpValue.get() && (block is BlockLiquid))
            event.cancelEvent()
    }

    override val tag: String
        get() = modeValue.get()
}