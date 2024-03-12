/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player


import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.createOpenInventoryPacket
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.utils.item.ArmorComparator
import net.ccbluex.liquidbounce.utils.item.ArmorPiece
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinItemStack
import net.ccbluex.liquidbounce.utils.extensions.toClickType
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.init.Enchantments.*
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumHand
import java.util.stream.Collectors
import java.util.stream.IntStream

@ModuleInfo(
    name = "InvManager",
    description = "Automatically equips the best armor in your inventory.",
    category = ModuleCategory.PLAYER
)
class InvManager : Module() {
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 600, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) set(minDelay)
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 400, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) set(maxDelay)
        }
    }

    private val invOpenValue = BoolValue("InvOpen", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)
    private val noMoveValue = BoolValue("NoMove", false)
    private val swingValue = BoolValue("SwingItem", false)
    private val ignoreChainArmorValue = BoolValue("IgnoreChainArmor", false)
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)
    private val randomSlotValue = BoolValue("RandomSlot", false)
    private val sortValue = BoolValue("Sort", true)
    private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000)

    private val items =
        arrayOf("None", "Ignore", "Sword", "Bow", "Pickaxe", "Axe", "Food", "Block", "Water", "Gapple", "Pearl")
    private val sortSlot1Value = ListValue("SortSlot-1", items, "Sword")
    private val sortSlot2Value = ListValue("SortSlot-2", items, "Pickaxe")
    private val sortSlot3Value = ListValue("SortSlot-3", items, "Gapple")
    private val sortSlot4Value = ListValue("SortSlot-4", items, "Block")
    private val sortSlot5Value = ListValue("SortSlot-5", items, "Food")
    private val sortSlot6Value = ListValue("SortSlot-6", items, "None")
    private val sortSlot7Value = ListValue("SortSlot-7", items, "Bow")
    private val sortSlot8Value = ListValue("SortSlot-8", items, "Axe")
    private val sortSlot9Value = ListValue("SortSlot-9", items, "Pearl")
    private var delay: Long = 0
    private var locked = false


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) || (mc.currentScreen !is GuiInventory) && invOpenValue.get() || noMoveValue.get() && isMoving || player.openContainer != null && player.openContainer!!.windowId != 0) return

        while (InventoryUtils.CLICK_TIMER.hasTimePassed(delay)) {

            // Find best armor
            val armorPieces = IntStream.range(0, 36).filter { i: Int ->
                val itemStack = mc.player!!.inventory.getStackInSlot(i)
                itemStack != null && (itemStack.item is ItemArmor && (!ignoreChainArmorValue.get() || !(itemStack.unlocalizedName == "item.helmetChain" || itemStack.unlocalizedName == "item.leggingsChain")))  && (i < 9 || System.currentTimeMillis() - (itemStack as IMixinItemStack).itemDelay >= itemDelayValue.get())
            }.mapToObj { i: Int ->
                ArmorPiece(
                    mc.player!!.inventory.getStackInSlot(
                        i
                    ), i
                )
            }.collect(Collectors.groupingBy<ArmorPiece?, Int> { obj: ArmorPiece? -> obj!!.armorType.slotIndex })
            val bestArmor = arrayOfNulls<ArmorPiece>(4)
            for ((key, value) in armorPieces) {
                bestArmor[key] = value.stream().max(ARMOR_COMPARATOR).orElse(null)
            }

            // Swap armor
            for (i in 0..3) {
                val armorPiece = bestArmor[i] ?: continue
                val armorSlot = 3 - i
                val oldArmor = ArmorPiece(mc.player!!.inventory.armorItemInSlot(armorSlot), -1)
                if (ItemUtils.isStackEmpty(oldArmor.itemStack) || (oldArmor.itemStack.item !is ItemArmor) || ARMOR_COMPARATOR.compare(
                        oldArmor,
                        armorPiece
                    ) < 0
                ) {
                    if (!ItemUtils.isStackEmpty(oldArmor.itemStack) && move(8 - (3 - armorSlot), true)) {
                        locked = true
                        return
                    }
                    if (ItemUtils.isStackEmpty(mc.player!!.inventory.armorItemInSlot(armorSlot)) && move(
                            armorPiece.slot,
                            false
                        )
                    ) {
                        locked = true
                        return
                    }
                }
            }
            locked = false

            if (sortValue.get()) sortHotbar()

            val garbageItems =
                items(9, 45).filter { !isUseful(it.value, it.key) }.keys.toMutableList()

            // Shuffle items
            if (randomSlotValue.get()) garbageItems.shuffle()

            val garbageItem = garbageItems.firstOrNull() ?: break

            // Drop all useless items
            val openInventory = (mc.currentScreen !is GuiInventory) && simulateInventory.get()

            if (openInventory) mc.connection!!.sendPacket(createOpenInventoryPacket())

            if (swingValue.get()) mc.player!!.swingArm(EnumHand.MAIN_HAND)

            mc.playerController.windowClick(player.openContainer!!.windowId, garbageItem, 1, 4.toClickType(), player)

            if (openInventory) mc.connection!!.sendPacket(CPacketCloseWindow())

            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    /**
     * Checks if the item is useful
     *
     * @param slot Slot id of the item. If the item isn't in the inventory -1
     * @return Returns true when the item is useful
     */
    fun isUseful(itemStack: ItemStack, slot: Int): Boolean {
        return try {
            val item = itemStack.item

            if (item is ItemSword || item is ItemTool) {
                val player = mc.player ?: return true

                if (slot >= 36 && findBetterItem(
                        slot - 36, player.inventory.getStackInSlot(slot - 36)
                    ) == slot - 36
                ) return true

                for (i in 0..8) {
                    if (type(i).equals("sword", true) && item is ItemSword || type(i).equals(
                            "pickaxe", true
                        ) && item is ItemPickaxe || type(i).equals(
                            "axe", true
                        ) && (item is ItemAxe)
                    ) {
                        if (findBetterItem(i, player.inventory.getStackInSlot(i)) == null) {
                            return true
                        }
                    }
                }

                val damage = (itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)["generic.attackDamage"].firstOrNull()?.amount
                    ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                    itemStack, SHARPNESS
                )

                items(0, 45).none { (_, stack) ->
                    stack != itemStack && stack.javaClass == itemStack.javaClass && damage < (stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                        stack, SHARPNESS
                    )
                }
            } else if (item is ItemArmor && (ignoreChainArmorValue.get() && (itemStack.unlocalizedName == "item.helmetChain" || itemStack.unlocalizedName == "item.leggingsChain")))
                false
            else if (item is ItemBow) {
                val currPower =
                    ItemUtils.getEnchantment(itemStack, POWER)

                items().none { (_, stack) ->
                    itemStack != stack && (stack.item is ItemBow) && currPower < ItemUtils.getEnchantment(
                        stack, POWER
                    )
                }
            } else if (item is ItemArmor) {
                val currArmor = ArmorPiece(itemStack, slot)

                items().none { (slot, stack) ->
                    if (stack != itemStack && stack.item is ItemArmor) {
                        val armor = ArmorPiece(stack, slot)

                        if (armor.armorType != currArmor.armorType) false
                        else ARMOR_COMPARATOR.compare(currArmor, armor) <= 0
                    } else false
                }
            } else if (itemStack.unlocalizedName == "item.compass") {
                items(0, 45).none { (_, stack) -> itemStack != stack && stack.unlocalizedName == "item.compass" }
            } else
                    (item is ItemFood && item is ItemAppleGold) || itemStack.unlocalizedName == "item.arrow" || itemStack.unlocalizedName == "item.slimeball" ||
                            (item is ItemBlock && !InventoryUtils.BLOCK_BLACKLIST.contains(item.block)) ||
                            item is ItemBed || item is ItemPotion || item is ItemEnderPearl || item is ItemBucket || itemStack.unlocalizedName == "item.stick" ||
                            ignoreVehiclesValue.get() && (item is ItemBoat || item is ItemMinecart)

        } catch (ex: Exception) {
            ClientUtils.getLogger().error("(InvManager) Failed to check item: ${itemStack.unlocalizedName}.", ex)
            true
        }
    }

    /**
     * INVENTORY SORTER
     */

    /**
     * Sort hotbar
     */
    private fun sortHotbar() {
        for (index in 0..8) {
            val player = mc.player ?: return

            val bestItem = findBetterItem(index, player.inventory.getStackInSlot(index)) ?: continue

            if (bestItem != index) {
                val openInventory = mc.currentScreen !is GuiInventory && simulateInventory.get()

                if (openInventory) mc.connection!!.sendPacket(createOpenInventoryPacket())

                mc.playerController.windowClick(
                    0, if (bestItem < 9) bestItem + 36 else bestItem, index, 2.toClickType(), player
                )

                if (openInventory) mc.connection!!.sendPacket(CPacketCloseWindow())

                delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                break
            }
        }
    }

    private fun findBetterItem(targetSlot: Int, slotStack: ItemStack?): Int? {
        val type = type(targetSlot)

        val player = mc.player ?: return null

        when (type.toLowerCase()) {
            "sword", "pickaxe", "axe" -> {
                val currentTypeChecker: ((Item?) -> Boolean) = when {
                    type.equals("Sword", ignoreCase = true) -> { item: Item? -> item is ItemSword }
                    type.equals("Pickaxe", ignoreCase = true) -> { obj: Item? -> obj is ItemPickaxe }
                    type.equals("Axe", ignoreCase = true) -> { obj: Item? -> obj is ItemAxe }
                    else -> return null
                }

                var bestWeapon = if (currentTypeChecker(slotStack?.item)) targetSlot
                else -1

                player.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack != null && currentTypeChecker(itemStack.item) && !type(index).equals(
                            type, ignoreCase = true
                        )
                    ) {
                        if (bestWeapon == -1) {
                            bestWeapon = index
                        } else {
                            val currDamage =
                                (itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)["generic.attackDamage"].firstOrNull()?.amount
                                    ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                                    itemStack, SHARPNESS
                                )

                            val bestStack = player.inventory.getStackInSlot(bestWeapon) ?: return@forEachIndexed
                            val bestDamage =
                                (bestStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)["generic.attackDamage"].firstOrNull()?.amount
                                    ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                                    bestStack, SHARPNESS
                                )

                            if (bestDamage < currDamage) bestWeapon = index
                        }
                    }
                }

                return if (bestWeapon != -1 || bestWeapon == targetSlot) bestWeapon else null
            }

            "bow" -> {
                var bestBow = if (slotStack?.item is ItemBow) targetSlot else -1
                var bestPower = if (bestBow != -1) ItemUtils.getEnchantment(
                    slotStack,POWER
                )
                else 0

                player.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (slotStack?.item is ItemBow && !type(index).equals(type, ignoreCase = true)) {
                        if (bestBow == -1) {
                            bestBow = index
                        } else {
                            val power = ItemUtils.getEnchantment(
                                itemStack, POWER
                            )

                            if (ItemUtils.getEnchantment(
                                    itemStack, POWER
                                ) > bestPower
                            ) {
                                bestBow = index
                                bestPower = power
                            }
                        }
                    }
                }

                return if (bestBow != -1) bestBow else null
            }

            "food" -> {
                player.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item

                        if (item is ItemFood && item !is ItemAppleGold && !type(index).equals(
                                "Food", ignoreCase = true
                            )
                        ) {
                            val replaceCurr = ItemUtils.isStackEmpty(slotStack) || item !is ItemFood

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "block" -> {
                player.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item!!

                        if (item is ItemBlock && !InventoryUtils.BLOCK_BLACKLIST.contains(item.block) && !type(
                                index
                            ).equals("Block", ignoreCase = true)
                        ) {
                            val replaceCurr = ItemUtils.isStackEmpty(slotStack) || item !is ItemBlock

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "water" -> {
                player.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item

                        if (item is ItemBucket && item ==
                                Blocks.FLOWING_WATER
                             && !type(index).equals("Water", ignoreCase = true)
                        ) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || (slotStack?.item !is ItemBucket) || (item !=
                                        Blocks.FLOWING_WATER)


                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "gapple" -> {
                player.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item

                        if (item is ItemAppleGold && !type(index).equals("Gapple", ignoreCase = true)) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || slotStack?.item is ItemAppleGold

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "pearl" -> {
                player.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item

                        if (item is ItemEnderPearl && !type(index).equals("Pearl", ignoreCase = true)) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || slotStack?.item !is ItemEnderPearl

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * Get items in inventory
     */
    private fun items(start: Int = 0, end: Int = 45): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in end - 1 downTo start) {
            val itemStack = mc.player?.inventoryContainer?.getSlot(i)?.stack ?: continue

            if (ItemUtils.isStackEmpty(itemStack)) continue

            if (i in 36..44 && type(i).equals("Ignore", ignoreCase = true)) continue

            if (System.currentTimeMillis() - (itemStack as IMixinItemStack).itemDelay >= itemDelayValue.get()) items[i] = itemStack
        }

        return items
    }

    /**
     * Get type of [targetSlot]
     */
    private fun type(targetSlot: Int) = when (targetSlot) {
        0 -> sortSlot1Value.get()
        1 -> sortSlot2Value.get()
        2 -> sortSlot3Value.get()
        3 -> sortSlot4Value.get()
        4 -> sortSlot5Value.get()
        5 -> sortSlot6Value.get()
        6 -> sortSlot7Value.get()
        7 -> sortSlot8Value.get()
        8 -> sortSlot9Value.get()
        else -> ""
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) || mc.player == null || mc.player!!.openContainer != null && mc.player!!.openContainer!!.windowId != 0) return

    }


    fun isLocked(): Boolean {
        return state && locked
    }

    /**
     * Shift+Left clicks the specified item
     *
     * @param item        Slot of the item to click
     * @param isArmorSlot
     * @return True if it is unable to move the item
     */
    private fun move(item: Int, isArmorSlot: Boolean): Boolean {
        if (!isArmorSlot && item < 9 && mc.currentScreen !is GuiInventory) {
            mc.connection!!.sendPacket(CPacketHeldItemChange(item))
            mc.connection!!.sendPacket(
                createUseItemPacket(
                    mc.player!!.inventoryContainer.getSlot(item).stack,
                    EnumHand.MAIN_HAND
                )
            )
            mc.connection!!.sendPacket(
                CPacketHeldItemChange(
                    mc.player!!.inventory.currentItem
                )
            )
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            return true
        } else if (!(noMoveValue.get() && isMoving) && (!invOpenValue.get() || mc.currentScreen is GuiInventory) && item != -1) {
            val openInventory = simulateInventory.get() && mc.currentScreen !is GuiInventory
            if (openInventory) mc.connection!!.sendPacket(createOpenInventoryPacket())
            var full = isArmorSlot
            if (full) {
                for (ItemStack in mc.player!!.inventory.mainInventory) {
                    if (ItemUtils.isStackEmpty(ItemStack)) {
                        full = false
                        break
                    }
                }
            }
            if (full) {
                mc.playerController.windowClick(
                    mc.player!!.inventoryContainer.windowId, item, 1, 4.toClickType(),
                    mc.player!!
                )
            } else {
                mc.playerController.windowClick(
                    mc.player!!.inventoryContainer.windowId,
                    if (isArmorSlot) item else if (item < 9) item + 36 else item, 0, 1.toClickType(),
                    mc.player!!
                )
            }
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (openInventory) mc.connection!!.sendPacket(CPacketCloseWindow())
            return true
        }
        return false

    }

    companion object {
        val ARMOR_COMPARATOR = ArmorComparator()
    }
}

