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
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "Zoot", description = "Removes all bad potion effects/fire.", category = ModuleCategory.PLAYER)
class Zoot : Module() {

    private val badEffectsValue = BoolValue("BadEffects", true)
    private val fireValue = BoolValue("Fire", true)
    private val noAirValue = BoolValue("NoAir", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (noAirValue.get() && !player.onGround)
            return

        if (badEffectsValue.get()) {
            val effect = player.activePotionEffects.maxBy { it.duration }

            if (effect != null) {
                repeat(effect.duration / 20) {
                    mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                }
            }
        }


        if (fireValue.get() && !player.capabilities.isCreativeMode && player.isBurning) {
            repeat(9) {
                mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
            }
        }
    }

    // TODO: Check current potion
    private fun hasBadEffect(): Boolean {
        val player = mc.player ?: return false

        return player.isPotionActive(MobEffects.HUNGER) || player.isPotionActive(MobEffects.MINING_FATIGUE) ||
                player.isPotionActive(MobEffects.SLOWNESS) || player.isPotionActive(MobEffects.INSTANT_DAMAGE) ||
                player.isPotionActive(MobEffects.NAUSEA) || player.isPotionActive(MobEffects.BLINDNESS) ||
                player.isPotionActive(MobEffects.WEAKNESS) || player.isPotionActive(MobEffects.WITHER) || player.isPotionActive(MobEffects.POISON)
    }

}