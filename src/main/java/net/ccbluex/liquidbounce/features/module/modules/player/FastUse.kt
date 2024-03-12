/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "FastUse", description = "Allows you to use items faster.", category = ModuleCategory.PLAYER)
class FastUse : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Instant", "NCP", "AAC", "Custom"), "NCP")

    private val noMoveValue = BoolValue("NoMove", false)

    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300)
    private val customSpeedValue = IntegerValue("CustomSpeed", 2, 1, 35)
    private val customTimer = FloatValue("CustomTimer", 1.1f, 0.5f, 2f)

    private val msTimer = MSTimer()
    private var usedTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (usedTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1F
            usedTimer = false
        }

        if (!player.isHandActive) {
            msTimer.reset()
            return
        }

        val usingItem = player.activeItemStack.item

        if ((usingItem is ItemFood) || (usingItem is ItemBucketMilk) || (usingItem is ItemPotion)) {
            when (modeValue.get().toLowerCase()) {
                "instant" -> {
                    repeat(35) {
                        mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(player)
                }

                "ncp" -> if (player.itemInUseMaxCount > 14) {
                    repeat(20) {
                        mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(player)
                }

                "aac" -> {
                    (mc.timer as IMixinTimer).timerSpeed = 1.22F
                    usedTimer = true
                }
                
                "custom" -> {
                    (mc.timer as IMixinTimer).timerSpeed = customTimer.get()
                    usedTimer = true

                    if (!msTimer.hasTimePassed(delayValue.get().toLong()))
                        return

                    repeat(customSpeedValue.get()) {
                        mc.connection!!.sendPacket(CPacketPlayer(player.onGround))
                    }

                    msTimer.reset()
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        val player = mc.player

        if (player == null || event == null)
            return
        if (!state || !player.isHandActive || !noMoveValue.get())
            return

        val usingItem = player.activeItemStack.item

        if ((usingItem is ItemFood) || (usingItem is ItemBucketMilk) || (usingItem is ItemPotion))
            event.zero()
    }

    override fun onDisable() {
        if (usedTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag: String?
        get() = modeValue.get()
}
