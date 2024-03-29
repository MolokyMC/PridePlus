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
import net.ccbluex.liquidbounce.utils.MovementUtils

@ModuleInfo(name = "Parkour", description = "Automatically jumps when reaching the edge of a block.", category = ModuleCategory.MOVEMENT)
class Parkour : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (MovementUtils.isMoving && player.onGround && !player.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown &&
                mc.world!!.getCollisionBoxes(player, player.entityBoundingBox
                        .offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty())
            player.jump()
    }
}
