/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        mc.gameSettings.keyBindSneak.pressed = mc.world!!.getBlockState(BlockPos(player.posX, player.posY - 1.0, player.posZ)).block == Blocks.AIR
    }

    override fun onDisable() {
        if (mc.player == null)
            return

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.gameSettings.keyBindSneak.pressed = false
    }
}
