
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand


@ModuleInfo(name = "Gapple", description = "Eat Gapples.", category = ModuleCategory.PLAYER)
class Gapple : Module() {
    val modeValue = ListValue("Mode", arrayOf("Auto", "LegitAuto", "Once", "Head"), "Once")
    // Auto Mode
    private val healthValue = FloatValue("Health", 10F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 150, 0, 1000)
    private val noAbsorption = BoolValue("NoAbsorption",true)
    private val timer = MSTimer()

    private var eating = -1

    override fun onEnable() {
        eating = -1
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        when(modeValue.get().toLowerCase()){
            "once" -> {
                doEat(true)
                state = false
            }
            "auto" -> {
                if (!timer.hasTimePassed(delayValue.get().toLong()))
                    return
                if (mc.player!!.health <= healthValue.get()){
                    doEat(false)
                    timer.reset()
                }
            }
            "legitauto" -> {
                if (eating == -1) {
                    val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.GOLDEN_APPLE)
                    if(gappleInHotbar == -1) return
                    mc.connection!!.sendPacket(CPacketHeldItemChange(gappleInHotbar - 36))
                    mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                    eating = 0
                } else if (eating > 35) {
                    mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
                    timer.reset()
                }
            }
            "head" -> {
                if (!timer.hasTimePassed(delayValue.get().toLong()))
                    return
                if (mc.player!!.health <= healthValue.get()){
                    val headInHotbar = InventoryUtils.findItem2(36, 45, Items.SKULL)
                    if(headInHotbar != -1) {
                        mc.connection!!.sendPacket(CPacketHeldItemChange(headInHotbar - 36))
                        mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                        mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
                        timer.reset()
                    }
                }
            }
        }
    }

    private fun doEat(warn: Boolean){
        if(noAbsorption.get()&&!warn){
                return
        }

        val gappleInHotbar = InventoryUtils.findItem2(36, 45, Items.GOLDEN_APPLE)
        if(gappleInHotbar != -1){
            mc.connection!!.sendPacket(CPacketHeldItemChange(gappleInHotbar - 36))
            mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
            repeat(35) {
                mc.connection!!.sendPacket(CPacketPlayer(mc.player.onGround))
            }
            mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
        }else if(warn){
        }
    }

    override val tag: String
        get() = modeValue.get()
}