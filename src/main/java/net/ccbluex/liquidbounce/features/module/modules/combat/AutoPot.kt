/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.isSplash
import net.ccbluex.liquidbounce.utils.extensions.toClickType
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.MoverType
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.potion.PotionType
import net.minecraft.potion.PotionUtils
import net.minecraft.util.EnumHand

@ModuleInfo(name = "AutoPot", description = "Automatically throws healing potions.", category = ModuleCategory.COMBAT)
class AutoPot : Module() {

    private val healthValue = FloatValue("Health", 15F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 500, 500, 1000)

    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)

    private val groundDistanceValue = FloatValue("GroundDistance", 2F, 0F, 5F)
    private val modeValue = ListValue("Mode", arrayOf("Normal", "Jump", "Port"), "Normal")

    private val msTimer = MSTimer()
    private var potion = -1

    @EventTarget
    fun onMotion(motionEvent: MotionEvent) {
        if (!msTimer.hasTimePassed(delayValue.get().toLong()) || mc.playerController.isInCreativeMode)
            return

        val player = mc.player ?: return

        when (motionEvent.eventState) {
            PRE -> {
                // Hotbar Potion
                val potionInHotbar = findPotion(36, 45)

                if (player.health <= healthValue.get() && potionInHotbar != -1) {
                    if (player.onGround) {
                        when (modeValue.get().toLowerCase()) {
                            "jump" -> player.jump()
                            "port" -> player.move(MoverType.PLAYER, 0.0, 0.42, 0.0)
                        }
                    }

                    // Prevent throwing potions into the void
                    val fallingPlayer = FallingPlayer(
                            player.posX,
                            player.posY,
                            player.posZ,
                            player.motionX,
                            player.motionY,
                            player.motionZ,
                            player.rotationYaw,
                            player.moveStrafing,
                            player.moveForward
                    )

                    val collisionBlock = fallingPlayer.findCollision(20)?.pos

                    if (player.posY - (collisionBlock?.y ?: 0) >= groundDistanceValue.get())
                        return

                    potion = potionInHotbar
                    mc.connection!!.sendPacket(CPacketHeldItemChange(potion - 36))

                    if (player.rotationPitch <= 80F) {
                        RotationUtils.setTargetRotation(Rotation(player.rotationYaw, RandomUtils.nextFloat(80F, 90F)))
                    }
                    return
                }

                // Inventory Potion -> Hotbar Potion
                val potionInInventory = findPotion(9, 36)
                if (potionInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
                    if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                        return

                    val openInventory = (mc.currentScreen !is GuiInventory) && simulateInventory.get()

                    if (openInventory)
                        mc.connection!!.sendPacket(createOpenInventoryPacket())

                    mc.playerController.windowClick(0, potionInInventory, 0, 1.toClickType(), player)

                    if (openInventory)
                        mc.connection!!.sendPacket(CPacketCloseWindow())

                    msTimer.reset()
                }
            }
            POST -> {
                if (potion >= 0 && RotationUtils.serverRotation.pitch >= 75F) {
                    val itemStack = player.inventory.getStackInSlot(potion)

                    if (itemStack != null) {
                        mc.connection!!.sendPacket(createUseItemPacket(itemStack, EnumHand.MAIN_HAND))
                        mc.connection!!.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))

                        msTimer.reset()
                    }

                    potion = -1
                }
            }
        }
    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int {
        val player = mc.player!!

        for (i in startSlot until endSlot) {
            val stack = player.inventoryContainer.getSlot(i).stack

            if (stack == null || (stack.item !is ItemPotion) || !stack.isSplash(stack))
                continue

            //val itemPotion = stack.item as ItemPotion

            for (potionEffect in PotionUtils.getEffectsFromStack(stack))
                if (potionEffect.potion == MobEffects.INSTANT_HEALTH)
                    return i

            if (!player.isPotionActive(MobEffects.REGENERATION))
                for (potionEffect in PotionUtils.getEffectsFromStack(stack))
                    if (potionEffect.potion == MobEffects.REGENERATION)
                        return i
        }

        return -1
    }

    override val tag: String?
        get() = healthValue.get().toString()

}