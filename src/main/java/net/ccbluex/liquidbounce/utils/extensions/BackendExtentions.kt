/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.GameType
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.event.ForgeEventFactory

inline fun Int.toEntityEquipmentSlot(): EntityEquipmentSlot {
    return when (this) {
        0 -> EntityEquipmentSlot.FEET
        1 -> EntityEquipmentSlot.LEGS
        2 -> EntityEquipmentSlot.CHEST
        3 -> EntityEquipmentSlot.HEAD
        4 -> EntityEquipmentSlot.MAINHAND
        5 -> EntityEquipmentSlot.OFFHAND
        else -> throw IllegalArgumentException("Invalid armorType $this")
    }
}

inline fun Int.toClickType(): ClickType {
    return when (this) {
        0 -> ClickType.PICKUP
        1 -> ClickType.QUICK_MOVE
        2 -> ClickType.SWAP
        3 -> ClickType.CLONE
        4 -> ClickType.THROW
        5 -> ClickType.QUICK_CRAFT
        6 -> ClickType.PICKUP_ALL
        else -> throw IllegalArgumentException("Invalid mode $this")
    }
}

inline fun ClickType.toInt(): Int {
    return when (this) {
        ClickType.PICKUP -> 0
        ClickType.QUICK_MOVE -> 1
        ClickType.SWAP -> 2
        ClickType.CLONE -> 3
        ClickType.THROW -> 4
        ClickType.QUICK_CRAFT -> 5
        ClickType.PICKUP_ALL -> 6
        else -> throw IllegalArgumentException("Invalid mode $this")
    }
}

inline fun ItemStack.isSplash(item: ItemStack): Boolean {
    return item.item == Items.SPLASH_POTION
}

// This method is not present in 1.12.2 like it was in 1.8.9
inline fun PlayerControllerMP.sendUseItem(playerController: PlayerControllerMP, wPlayer: EntityPlayer, wWorld: World, wItemStack: ItemStack): Boolean {
    val player = wPlayer
    val world = wWorld
    val itemStack = wItemStack

    if (playerController.currentGameType == GameType.SPECTATOR) {
        return false
    } else {
        playerController.syncCurrentPlayItem()

        Minecraft.getMinecraft().connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))

        if (player.cooldownTracker.hasCooldown(itemStack.item)) {
            return false
        } else {
            val cancelResult = ForgeHooks.onItemRightClick(player, EnumHand.MAIN_HAND)

            if (cancelResult != null)
                return cancelResult == EnumActionResult.SUCCESS

            val i = itemStack.count

            val result = itemStack.useItemRightClick(world, player, EnumHand.MAIN_HAND)

            val resultStack = result.result

            if (resultStack != itemStack || resultStack.count != i) {
                player.setHeldItem(EnumHand.MAIN_HAND, resultStack)

                if (resultStack.isEmpty) {
                    ForgeEventFactory.onPlayerDestroyItem(player, itemStack, EnumHand.MAIN_HAND)
                }
            }

            return result.type == EnumActionResult.SUCCESS
        }
    }
}
