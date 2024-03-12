/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.createOpenInventoryPacket
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.extensions.toClickType
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "AutoSoup", description = "Makes you automatically eat soup whenever your health is low.", category = ModuleCategory.COMBAT)
class AutoSoup : Module() {

    private val healthValue = FloatValue("Health", 15f, 0f, 20f)
    private val delayValue = IntegerValue("Delay", 150, 0, 500)
    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventoryValue = BoolValue("SimulateInventory", true)
    private val bowlValue = ListValue("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")

    private val timer = MSTimer()

    override val tag: String
        get() = healthValue.get().toString()

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!timer.hasTimePassed(delayValue.get().toLong()))
            return

        val player = mc.player ?: return

        val soupInHotbar = InventoryUtils.findItem(36, 45, Items.MUSHROOM_STEW)

        if (player.health <= healthValue.get() && soupInHotbar != -1) {
            mc.connection!!.sendPacket(CPacketHeldItemChange(soupInHotbar - 36))
            mc.connection!!.sendPacket(createUseItemPacket(player.inventory.getStackInSlot(soupInHotbar), EnumHand.MAIN_HAND))

            if (bowlValue.get().equals("Drop", true))
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))

            mc.connection!!.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
            timer.reset()
            return
        }

        val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.BOWL)
        if (bowlValue.get().equals("Move", true) && bowlInHotbar != -1) {
            if (openInventoryValue.get() && (mc.currentScreen !is GuiInventory))
                return

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = player.inventory.getStackInSlot(i)

                if (itemStack == null) {
                    bowlMovable = true
                    break
                } else if (itemStack.item == (Items.BOWL) && itemStack.stackSize < 64) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {
                val openInventory = (mc.currentScreen !is GuiInventory) && simulateInventoryValue.get()

                if (openInventory)
                    mc.connection!!.sendPacket(createOpenInventoryPacket())

                mc.playerController.windowClick(0, bowlInHotbar, 0, 1.toClickType(), player)
            }
        }

        val soupInInventory = InventoryUtils.findItem(9, 36, (Items.MUSHROOM_STEW))

        if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
            if (openInventoryValue.get() && (mc.currentScreen !is GuiInventory))
                return

            val openInventory = (mc.currentScreen !is GuiInventory) && simulateInventoryValue.get()
            if (openInventory)
                mc.connection!!.sendPacket(createOpenInventoryPacket())

            mc.playerController.windowClick(0, soupInInventory, 0, 1.toClickType(), player)

            if (openInventory)
                mc.connection!!.sendPacket(CPacketCloseWindow())

            timer.reset()
        }
    }

}