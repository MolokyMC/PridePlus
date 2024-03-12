/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.block.BlockAir
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import java.awt.Color

@ModuleInfo(name = "CivBreak", description = "Allows you to break blocks instantly.", category = ModuleCategory.WORLD)
class CivBreak : Module() {

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null

    private val modeValue = ListValue("Mode", arrayOf("GrimAC", "Normal"), "GrimAC")

    private val range = FloatValue("Range", 5F, 1F, 6F)
    private val rotationsValue = BoolValue("Rotations", true)
    private val visualSwingValue = BoolValue("VisualSwing", true)

    private val airResetValue = BoolValue("Air-Reset", true)
    private val rangeResetValue = BoolValue("Range-Reset", true)

    // GrimAC
    private var breaking = false
    private var breakPercent = 0f
    private var canBreak = false


    @EventTarget
    fun onBlockClick(event: ClickBlockEvent) {
        if (event.clickedBlock?.let { BlockUtils.getBlock(it) } == Blocks.BEDROCK)
            return

        blockPos = event.clickedBlock ?: return
        enumFacing = event.WEnumFacing ?: return

        when(modeValue.get().toLowerCase()){
            "normal" -> {
                // Break
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos!!, enumFacing!!))
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos!!, enumFacing!!))
            }
        }

    }

    @EventTarget
    fun onUpdate(event: MotionEvent) {
        when(modeValue.get().toLowerCase()){
            "grimac" -> {
                if (blockPos == null || enumFacing == null){
                    return
                }

                canBreak = if (breakPercent * 50 >= 100){
                    BlockUtils.getCenterDistance(blockPos!!) < range.get()
                } else {
                    false
                }

                if (canBreak){
                    mc.connection!!.networkManager.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockPos!!, enumFacing!!))
                    blockPos = null
                    enumFacing = null
                    breaking = false
                    breakPercent = 0f
                }

                if (breaking){
                    breakPercent += mc.world.getBlockState(blockPos!!).getPlayerRelativeBlockHardness(mc.player,mc.world,blockPos!!)
                }
            }
            "normal" -> {
                val pos = blockPos ?: return

                if (airResetValue.get() && BlockUtils.getBlock(pos) is BlockAir ||
                    rangeResetValue.get() && BlockUtils.getCenterDistance(pos) > range.get()) {
                    blockPos = null
                    return
                }

                if (BlockUtils.getBlock(pos) is BlockAir || BlockUtils.getCenterDistance(pos) > range.get())
                    return

                when (event.eventState) {
                    PRE -> if (rotationsValue.get())
                        RotationUtils.setTargetRotation((RotationUtils.faceBlock(pos) ?: return).rotation)

                    POST -> {
                        if (visualSwingValue.get())
                            mc.player!!.swingArm(EnumHand.MAIN_HAND)
                        else
                            mc.connection!!.sendPacket(CPacketAnimation())

                        // Break
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                            blockPos!!, enumFacing!!))
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            blockPos!!, enumFacing!!))
                        mc.playerController.clickBlock(blockPos!!, enumFacing!!)
                    }
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        RenderUtils.drawBlockBox(blockPos ?: return, Color.RED, true)
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (modeValue.get() != "GrimAC") return
        when (event.eventState) {
            POST -> {
                if (breaking) {
                    mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0, 0, true))
                    mc.player!!.swingArm(EnumHand.MAIN_HAND)
                }
            }
        }
    }
}