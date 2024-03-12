/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.minecraft.block.BlockPane
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "HighJump", description = "Allows you to jump higher.", category = ModuleCategory.MOVEMENT)
class HighJump : Module() {
    private val heightValue = FloatValue("Height", 2f, 1.1f, 5f)
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Damage", "AACv3", "DAC", "Mineplex"), "Vanilla")
    private val glassValue = BoolValue("OnlyGlassPane", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player!!

        if (glassValue.get() && getBlock(BlockPos(player.posX, player.posY, player.posZ)) !is BlockPane)
            return

        when (modeValue.get().toLowerCase()) {
            "damage" -> if (player.hurtTime > 0 && player.onGround) player.motionY += 0.42f * heightValue.get()
            "aacv3" -> if (!player.onGround) player.motionY += 0.059
            "dac" -> if (!player.onGround) player.motionY += 0.049999
            "mineplex" -> if (!player.onGround) MovementUtils.strafe(0.35f)
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        val player = mc.player ?: return

        if (glassValue.get() && getBlock(BlockPos(player.posX, player.posY, player.posZ)) !is BlockPane)
            return
        if (!player.onGround) {
            if ("mineplex" == modeValue.get().toLowerCase()) {
                player.motionY += if (player.fallDistance == 0.0f) 0.0499 else 0.05
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val player = mc.player ?: return

        if (glassValue.get() && getBlock(BlockPos(player.posX, player.posY, player.posZ)) !is BlockPane)
            return
        when (modeValue.get().toLowerCase()) {
            "vanilla" -> event.motion *= heightValue.get()
            "mineplex" -> event.motion = 0.47f
        }
    }

    override val tag: String
        get() = modeValue.get()
}