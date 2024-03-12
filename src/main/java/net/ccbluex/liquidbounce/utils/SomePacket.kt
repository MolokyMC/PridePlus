package net.ccbluex.liquidbounce.utils

import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand

fun createUseItemPacket(itemStack: ItemStack?, hand: EnumHand): Packet<*> {
    return CPacketPlayerTryUseItem(hand)
}
fun createOpenInventoryPacket(): Packet<*> {
    return CPacketEntityAction(MinecraftInstance.mc.player, CPacketEntityAction.Action.OPEN_INVENTORY)
}

// CrossVersionUtils
