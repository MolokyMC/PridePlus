/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "AutoTool", description = "Automatically selects the best tool in your inventory to mine a block.", category = ModuleCategory.PLAYER)
class AutoTool : Module() {

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        switchSlot(event.clickedBlock ?: return)
    }

    fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1F
        var bestSlot = -1

        val blockState = mc.world!!.getBlockState(blockPos)

        for (i in 0..8) {
            val item = mc.player!!.inventory.getStackInSlot(i) ?: continue
            val speed = item.getDestroySpeed(blockState)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

        if (bestSlot != -1)
            mc.player!!.inventory.currentItem = bestSlot
    }

}