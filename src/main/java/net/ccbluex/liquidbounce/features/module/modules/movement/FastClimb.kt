/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockVine

@ModuleInfo(name = "FastClimb", description = "Allows you to climb up ladders and vines faster.", category = ModuleCategory.MOVEMENT)
class FastClimb : Module() {

    val modeValue = ListValue("Mode",
            arrayOf("Vanilla", "AAC3.0.5", "SAAC3.1.2", "AAC3.1.2"), "Vanilla")
    private val speedValue = FloatValue("Speed", 0.2872F, 0.01F, 5F)

    @EventTarget
    fun onMove(event: MoveEvent) {
        val mode = modeValue.get()

        val player = mc.player ?: return

        when {
            mode.equals("Vanilla", ignoreCase = true) && player.collidedHorizontally &&
                    player.isOnLadder -> {
                event.y = speedValue.get().toDouble()
                player.motionY = 0.0
            }

            mode.equals("AAC3.0.5", ignoreCase = true) && mc.gameSettings.keyBindForward.isKeyDown &&
                    collideBlockIntersects(player.entityBoundingBox) {
                        (it is BlockLadder) || (it is BlockVine)
                    } -> {
                event.x = 0.0
                event.y = 0.5
                event.z = 0.0

                player.motionX = 0.0
                player.motionY = 0.0
                player.motionZ = 0.0
            }

            mode.equals("SAAC3.1.2", ignoreCase = true) && player.collidedHorizontally &&
                    player.isOnLadder -> {
                event.y = 0.1649
                player.motionY = 0.0
            }

            mode.equals("AAC3.1.2", ignoreCase = true) && player.collidedHorizontally &&
                    player.isOnLadder -> {
                event.y = 0.1699
                player.motionY = 0.0
            }
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.player != null && ((event.block is BlockLadder) || (event.block is BlockVine)) &&
                modeValue.get().equals("AAC3.0.5", ignoreCase = true) && mc.player!!.isOnLadder)
            event.boundingBox = null
    }

    override val tag: String
        get() = modeValue.get()
}