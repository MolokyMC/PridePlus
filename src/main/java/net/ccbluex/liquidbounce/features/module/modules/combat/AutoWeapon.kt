/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.init.Enchantments
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketUseEntity

@ModuleInfo(name = "AutoWeapon", description = "Automatically selects the best weapon in your hotbar.", category = ModuleCategory.COMBAT)
class AutoWeapon : Module() {

    private val silentValue = BoolValue("SpoofItem", false)
    private val ticksValue = IntegerValue("SpoofTicks", 10, 1, 20)
    private var attackEnemy = false

    private var spoofedSlot = 0

    @EventTarget
    fun onAttack(event: AttackEvent) {
        attackEnemy = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet !is CPacketUseEntity)
            return

        val player = mc.player ?: return

        val packet = event.packet

        if (packet.action == CPacketUseEntity.Action.ATTACK
                && attackEnemy) {
            attackEnemy = false

            // Find best weapon in hotbar (#Kotlin Style)
            val (slot, _) = (0..8)
                    .map { Pair(it, player.inventory.getStackInSlot(it)) }
                    .filter { it.second != null && ((it.second.item is ItemSword) || (it.second.item is ItemTool)) }
                    .maxBy {
                        it.second.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)["generic.attackDamage"].first().amount + 1.25 * ItemUtils.getEnchantment(it.second, Enchantments.SHARPNESS)
                    } ?: return

            if (slot == player.inventory.currentItem) // If in hand no need to swap
                return

            // Switch to best weapon
            if (silentValue.get()) {
                mc.connection!!.sendPacket(CPacketHeldItemChange(slot))
                spoofedSlot = ticksValue.get()
            } else {
                player.inventory.currentItem = slot
                mc.playerController.updateController()
            }

            // Resend attack packet
            mc.connection!!.sendPacket(packet)
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(update: UpdateEvent) {
        // Switch back to old item after some time
        if (spoofedSlot > 0) {
            if (spoofedSlot == 1)
                mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
            spoofedSlot--
        }
    }
}