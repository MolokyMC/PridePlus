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
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockVine
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "AirLadder", description = "Allows you to climb up ladders/vines without touching them.", category = ModuleCategory.MOVEMENT)
class AirLadder : Module() {
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if ((BlockUtils.getBlock(BlockPos(player.posX, player.posY + 1, player.posZ)) is BlockLadder) && player.collidedHorizontally ||
                (BlockUtils.getBlock(BlockPos(player.posX, player.posY, player.posZ)) is BlockVine) ||
                (BlockUtils.getBlock(BlockPos(player.posX, player.posY + 1, player.posZ)) is BlockVine)) {
            player.motionY = 0.15
            player.motionX = 0.0
            player.motionZ = 0.0
        }
    }
}