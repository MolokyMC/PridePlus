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
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "Regen", description = "Regenerates your health much faster.", category = ModuleCategory.PLAYER)
class Regen : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Spartan", "NewSpartan"), "Vanilla")
    private val healthValue = IntegerValue("Health", 18, 0, 20)
    private val foodValue = IntegerValue("Food", 18, 0, 20)
    private val speedValue = IntegerValue("Speed", 100, 1, 100)
    private val noAirValue = BoolValue("NoAir", false)
    private val potionEffectValue = BoolValue("PotionEffect", false)

    private var resetTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer)
            (mc.timer as IMixinTimer).timerSpeed = 1F
        resetTimer = false

        val player = mc.player ?: return

        if ((!noAirValue.get() || player.onGround) && !player.capabilities.isCreativeMode &&
                player.foodStats.foodLevel > foodValue.get() && player.isEntityAlive && player.health < healthValue.get()) {
            if (potionEffectValue.get() && !player.isPotionActive(MobEffects.REGENERATION))
                return

            when (modeValue.get().toLowerCase()) {
                "newspartan" -> {
                    if (mc.player!!.ticksExisted % 5 == 0)
                        resetTimer = true
                    (mc.timer as IMixinTimer).timerSpeed = 0.98F
                    mc.connection!!.sendPacket(CPacketPlayer(true))

                }
                "vanilla" -> {
                    repeat(speedValue.get()) {
                        mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                    }
                }

                "spartan" -> {
                    if (MovementUtils.isMoving || !player.onGround)
                        return

                    repeat(9) {
                        mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                    }

                    (mc.timer as IMixinTimer).timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }
    }
}
