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

@ModuleInfo(name = "AutoWalk", description = "Automatically makes you walk.", category = ModuleCategory.MOVEMENT)
class AutoWalk : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindForward.pressed = true
    }

    override fun onDisable() {
        if (!mc.gameSettings.keyBindForward.isKeyDown)
            mc.gameSettings.keyBindForward.pressed = false
    }
}
