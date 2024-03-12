/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getMaterial
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "IceSpeed", description = "Allows you to walk faster on ice.", category = ModuleCategory.MOVEMENT)
class IceSpeed : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "AAC", "Spartan"), "NCP")
    override fun onEnable() {
        if (modeValue.get().equals("NCP", ignoreCase = true)) {
            Blocks.ICE.slipperiness = 0.39f
            Blocks.PACKED_ICE.slipperiness = 0.39f
        }
        super.onEnable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val mode = modeValue.get()
        if (mode.equals("NCP", ignoreCase = true)) {
            Blocks.ICE.slipperiness = 0.39f
            Blocks.PACKED_ICE.slipperiness = 0.39f
        } else {
            Blocks.ICE.slipperiness = 0.98f
            Blocks.PACKED_ICE.slipperiness = 0.98f
        }

        val player = mc.player ?: return

        if (player.onGround && !player.isOnLadder && !player.isSneaking && player.isSprinting && player.movementInput.moveForward > 0.0) {
            if (mode.equals("AAC", ignoreCase = true)) {
                getMaterial(player.position.down()).let {
                    if (it == Blocks.ICE || it == Blocks.PACKED_ICE) {
                        player.motionX *= 1.342
                        player.motionZ *= 1.342
                        Blocks.ICE.slipperiness = 0.6f
                        Blocks.PACKED_ICE.slipperiness = 0.6f
                    }
                }
            }
            if (mode.equals("Spartan", ignoreCase = true)) {
                getMaterial(player.position.down()).let {
                    if (it == Blocks.ICE || it == Blocks.PACKED_ICE) {
                        val upBlock: Block = getBlock(BlockPos(player.posX, player.posY + 2.0, player.posZ))

                        if (upBlock !is BlockAir) {
                            player.motionX *= 1.342
                            player.motionZ *= 1.342
                        } else {
                            player.motionX *= 1.18
                            player.motionZ *= 1.18
                        }

                        Blocks.ICE.slipperiness = 0.6f
                        Blocks.PACKED_ICE.slipperiness = 0.6f
                    }
                }
            }
        }
    }

    override fun onDisable() {
        Blocks.ICE.slipperiness = 0.98f
        Blocks.PACKED_ICE.slipperiness = 0.98f
        super.onDisable()
    }
}