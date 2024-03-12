/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoWeb", description = "Prevents you from getting slowed down in webs.", category = ModuleCategory.MOVEMENT)
class NoWeb : Module() {

    private val modeValue = ListValue("Mode", arrayOf("None", "GrimAC", "AAC", "LAAC", "Rewi", "Matrix", "Spartan", "AAC5"), "None")
    private var usedTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (usedTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1F
            usedTimer = false
        }

        if (!player.isInWeb)
            return


        when (modeValue.get().toLowerCase()) {


            "none" -> player.isInWeb = false
            "grimac" -> {
                val searchBlocks = BlockUtils.searchBlocks(4)

                for (block in searchBlocks){
                    val blockpos = block.key
                    val blocks = block.value
                    if(blocks is BlockWeb){
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                        mc.player.isInWeb = false


                    }
                    if(blocks is BlockLiquid){
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                        mc.player.inWater = false
                    }
                }

                if (mc.player.isOnLadder && mc.gameSettings.keyBindJump.isKeyDown) {
                    if (mc.player.motionY >= 0.0) {
                        mc.player.motionY = 0.1786
                    }
                }
            }
            "aac" -> {
                player.jumpMovementFactor = 0.59f

                if (!mc.gameSettings.keyBindSneak.isKeyDown)
                    player.motionY = 0.0
            }
            "laac" -> {
                player.jumpMovementFactor = if (player.movementInput.moveStrafe != 0f) 1.0f else 1.21f

                if (!mc.gameSettings.keyBindSneak.isKeyDown)
                    player.motionY = 0.0

                if (player.onGround)
                    player.jump()
            }
            "rewi" -> {
                player.jumpMovementFactor = 0.42f

                if (player.onGround)
                    player.jump()
            }
            "spartan" -> {
                MovementUtils.strafe(0.27F)
                (mc.timer as IMixinTimer).timerSpeed = 3.7F
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.player!!.motionY = 0.0
                }
                if (mc.player!!.ticksExisted % 2 == 0) {
                    (mc.timer as IMixinTimer).timerSpeed = 1.7F
                }
                if (mc.player!!.ticksExisted % 40 == 0) {
                    (mc.timer as IMixinTimer).timerSpeed = 3F
                }
                usedTimer = true
            }
            "matrix" -> {
                mc.player!!.jumpMovementFactor = 0.12425f
                mc.player!!.motionY = -0.0125
                if (mc.gameSettings.keyBindSneak.isKeyDown) mc.player!!.motionY = -0.1625

                if (mc.player!!.ticksExisted % 40 == 0) {
                    (mc.timer as IMixinTimer).timerSpeed = 3.0F
                    usedTimer = true
                }
            }
            "aac5" -> {
                mc.player!!.jumpMovementFactor = 0.42f

                if (mc.player!!.onGround) {
                    mc.player!!.jump()
                }
            }
        }
    }

    override val tag: String?
        get() = modeValue.get()
}
