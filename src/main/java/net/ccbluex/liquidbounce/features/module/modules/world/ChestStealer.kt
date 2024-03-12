/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinGuiContainer
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.extensions.toClickType
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketWindowItems
import net.minecraft.util.ResourceLocation
import kotlin.random.Random

@ModuleInfo(name = "ChestStealer", description = "Automatically steals all items from a chest.", category = ModuleCategory.WORLD)
class ChestStealer : Module() {

    /**
     * OPTIONS
     */
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 20, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(minDelayValue.get(), get())
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 5, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(get(), maxDelayValue.get())
        }
    }
    private val delayOnFirstValue = BoolValue("DelayOnFirst", false).displayable { !instantValue.get() }

    private val takeRandomizedValue = BoolValue("TakeRandomized", false).displayable { !instantValue.get() }
    private val onlyItemsValue = BoolValue("OnlyItems", false).displayable { !instantValue.get() }
    private val noCompassValue = BoolValue("NoCompass", false).displayable { !instantValue.get() }
    private val noMoveValue = BoolValue("NoMove", false).displayable { !instantValue.get() }
    private val noAirValue = BoolValue("NoAir", false).displayable { !instantValue.get() }
    private val combatCloseValue = BoolValue("DamageClose", false)
    private val instantValue = BoolValue("Instant", false)
    private val normalMoveMode = ListValue("NormalStealMode", arrayOf("Packet","Normal"),"Normal").displayable { !instantValue.get() }

    private var stealing = false

    private val silentValue = BoolValue("Silent", false)
    private val chestTitleValue = BoolValue("ChestTitle", false).displayable { silentValue.get() }


    private val autoCloseValue = BoolValue("AutoClose", false).displayable { !instantValue.get() }
    private val autoCloseMaxDelayValue: IntegerValue = object : IntegerValue("AutoCloseMaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMinDelayValue.get()
            if (i > newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), this.get())
        }
    }.displayable { autoCloseValue.get() && !instantValue.get() } as IntegerValue

    private val autoCloseMinDelayValue: IntegerValue = object : IntegerValue("AutoCloseMinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMaxDelayValue.get()
            if (i < newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(this.get(), autoCloseMaxDelayValue.get())
        }
    }.displayable { autoCloseValue.get() && !instantValue.get() } as IntegerValue

    private val closeOnFullValue = BoolValue("CloseOnFull", true).displayable { !instantValue.get() }


    /**
     * VALUES
     */

    private val delayTimer = MSTimer()
    private var nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

    private val autoCloseTimer = MSTimer()
    private var nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

    private var contentReceived = 0

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val player = mc.player!!

        if (mc.currentScreen is GuiChest || mc.currentScreen == null) {
            if (delayOnFirstValue.get())
                delayTimer.reset()
            autoCloseTimer.reset()
            stealing = false
            return
        }

        if (combatCloseValue.get() && mc.player.hurtTime != 0) {
            player.closeScreen()
            stealing = false
            return
        }


        if (!delayTimer.hasTimePassed(nextDelay)) {
            autoCloseTimer.reset()
            return
        }

        val screen = mc.currentScreen as GuiChest

        // No Compass
        if (noCompassValue.get() && player.inventory.getCurrentItem().item.unlocalizedName == "item.compass")
            return

        // Chest title
        if (chestTitleValue.get() && (screen.lowerChestInventory == null || !screen.lowerChestInventory!!.name.contains(
                ItemStack(Item.REGISTRY.getObject(ResourceLocation("minecraft:chest"))!!).displayName
            ))
        )
            return

        // inventory cleaner
        val invManager = Pride.moduleManager[InvManager::class.java] as InvManager

        // Is empty?
        if (!isEmpty(screen) && (!closeOnFullValue.get() || !fullInventory)) {

            stealing = true

            autoCloseTimer.reset()

            if ((noMoveValue.get() && MovementUtils.isMoving) || (noAirValue.get() && !mc.player.onGround)) return

            // Randomized
            if (takeRandomizedValue.get()) {
                do {
                    val items = mutableListOf<Slot>()

                    for (slotIndex in 0 until screen.inventoryRows * 9) {
                        val slot = screen.inventorySlots!!.getSlot(slotIndex)

                        val stack = slot.stack

                        if (stack != null && (!onlyItemsValue.get() || stack.item !is ItemBlock) && (invManager.isUseful(
                                stack,
                                -1
                            ))
                        )
                            items.add(slot)
                    }

                    val randomSlot = Random.nextInt(items.size)
                    val slot = items[randomSlot]

                    move(screen, slot)
                } while (delayTimer.hasTimePassed(nextDelay) && items.isNotEmpty())
                return
            }

            // Non randomized
            for (slotIndex in 0 until screen.inventoryRows * 9) {
                val slot = screen.inventorySlots!!.getSlot(slotIndex)

                val stack = slot.stack

                if (delayTimer.hasTimePassed(nextDelay) && shouldTake(stack, invManager)) {
                    move(screen, slot)
                }
            }

            //AutoClose
        } else if (screen.inventorySlots!!.windowId == contentReceived && autoCloseTimer.hasTimePassed(nextCloseDelay) && autoCloseValue.get()) {
            player.closeScreen()
            if (silentValue.get()) {
                Pride.hud.addNotification(Notification(this.name, "Closed Chest.", NotifyType.INFO))
            }
            stealing = false
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())
        }
    }

    override fun onEnable() {
        stealing = false
    }

    override fun onDisable() {
        stealing = false
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is SPacketWindowItems)
            contentReceived = packet.windowId

    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (instantValue.get()) {
            if (mc.currentScreen is GuiChest) {
                val chest = mc.currentScreen as GuiChest
                val rows = chest.inventoryRows * 9
                for (i in 0 until rows) {
                    val slot = chest.inventorySlots?.getSlot(i)
                    if (slot!!.hasStack) {
                        mc.connection?.sendPacket(
                            CPacketClickWindow(
                                chest.inventorySlots?.windowId!!,
                                i,
                                0,
                                ClickType.QUICK_MOVE,
                                slot.stack,
                                1.toShort()
                            )
                        )
                        mc.connection!!.sendPacket(
                            CPacketConfirmTransaction(
                                chest.inventorySlots!!.windowId,
                                1.toShort(),
                                true
                            )
                        )
                    }
                }
                mc.player?.closeScreen()
            }
        }
    }

    private fun shouldTake(stack: ItemStack?, invManager: InvManager): Boolean {
        return stack != null && !ItemUtils.isStackEmpty(stack) && (!onlyItemsValue.get() ||
            stack.item !is ItemBlock
        ) && (invManager.isUseful(stack, -1))
    }


    private fun move(screen: GuiChest, slot: Slot) {
        when (normalMoveMode.get().toLowerCase()) {
            "packet" -> {
                mc.connection?.sendPacket(
                    CPacketClickWindow(
                        screen.inventorySlots?.windowId!!,
                        slot.slotNumber,
                        0,
                        ClickType.QUICK_MOVE,
                        slot.stack,
                        1.toShort()
                    )
                )
                mc.connection!!.sendPacket(CPacketConfirmTransaction(screen.inventorySlots!!.windowId, 1.toShort(), true))
            }
            "normal" -> {
                (screen as IMixinGuiContainer).publicHandleMouseClick(slot, slot.slotNumber, 0, 1.toClickType())
            }
        }
        delayTimer.reset()
        nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    private fun isEmpty(chest: GuiChest): Boolean {
        val invManager = Pride.moduleManager[InvManager::class.java] as InvManager

        for (i in 0 until chest.inventoryRows * 9) {
            val slot = chest.inventorySlots!!.getSlot(i)

            val stack = slot.stack

            if (shouldTake(stack, invManager))
                return false
        }

        return true
    }

    private val fullInventory: Boolean
        get() = mc.player?.inventory?.mainInventory?.none(ItemUtils::isStackEmpty) ?: false
}