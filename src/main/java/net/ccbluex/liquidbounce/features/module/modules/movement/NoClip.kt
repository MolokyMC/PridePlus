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

@ModuleInfo(name = "NoClip", description = "Allows you to freely move through walls (A sandblock has to fall on your head).", category = ModuleCategory.MOVEMENT)
class NoClip : Module() {

    override fun onDisable() {
        mc.player?.noClip = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        player.noClip = true
        player.fallDistance = 0f
        player.onGround = false

        player.capabilities.isFlying = false
        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0

        val speed = 0.32f

        player.jumpMovementFactor = speed

        if (mc.gameSettings.keyBindJump.isKeyDown)
            player.motionY += speed.toDouble()

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            player.motionY -= speed.toDouble()
    }
}
