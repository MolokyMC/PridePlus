/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.VecRotation
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucket
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import kotlin.math.ceil
import kotlin.math.sqrt

@ModuleInfo(name = "NoFall", description = "Prevents you from taking fall damage.", category = ModuleCategory.PLAYER)
class NoFall : Module() {
    @JvmField
    val modeValue = ListValue("Mode", arrayOf("GrimAC","SpoofGround", "NoGround", "Packet", "MLG", "AAC", "LAAC", "AAC3.3.11", "AAC3.3.15", "Spartan", "CubeCraft", "Hypixel"), "SpoofGround")
    private val minFallDistance = FloatValue("MinMLGHeight", 5f, 2f, 50f)
    private val spartanTimer = TickTimer()
    private val mlgTimer = TickTimer()
    private var currentState = 0
    private var jumped = false
    private var currentMlgRotation: VecRotation? = null
    private var currentMlgItemIndex = 0
    private var currentMlgBlock: BlockPos? = null

    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent?) {
        if (mc.player!!.onGround)
            jumped = false

        if (mc.player!!.motionY > 0)
            jumped = true

        if (!state || Pride.moduleManager.getModule(FreeCam::class.java)!!.state)
            return

        if (collideBlock(mc.player.entityBoundingBox) { it is BlockLiquid } || collideBlock(
                AxisAlignedBB(mc.player.entityBoundingBox.maxX, mc.player.entityBoundingBox.maxY, mc.player.entityBoundingBox.maxZ, mc.player.entityBoundingBox.minX, mc.player.entityBoundingBox.minY - 0.01, mc.player.entityBoundingBox.minZ)
            ) { it is BlockLiquid }) {
            return
        }

        when (modeValue.get().toLowerCase()) {
            "packet" -> {
                if (mc.player!!.fallDistance > 2f) {
                    mc.connection!!.sendPacket(CPacketPlayer(true))
                }
            }
            "cubecraft" -> if (mc.player!!.fallDistance > 2f) {
                mc.player!!.onGround = false
                mc.connection!!.sendPacket(CPacketPlayer(true))
            }
            "aac" -> {
                if (mc.player!!.fallDistance > 2f) {
                    mc.connection!!.sendPacket(CPacketPlayer(true))
                    currentState = 2
                } else if (currentState == 2 && mc.player!!.fallDistance < 2) {
                    mc.player!!.motionY = 0.1
                    currentState = 3
                    return
                }
                when (currentState) {
                    3 -> {
                        mc.player!!.motionY = 0.1
                        currentState = 4
                    }
                    4 -> {
                        mc.player!!.motionY = 0.1
                        currentState = 5
                    }
                    5 -> {
                        mc.player!!.motionY = 0.1
                        currentState = 1
                    }
                }
            }
            "laac" -> if (!jumped && mc.player!!.onGround && !mc.player!!.isOnLadder && !mc.player!!.isInWater
                    && !mc.player!!.isInWeb) mc.player!!.motionY = (-6).toDouble()
            "aac3.3.11" -> if (mc.player!!.fallDistance > 2) {
                mc.player!!.motionZ = 0.0
                mc.player!!.motionX = mc.player!!.motionZ
                mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX,
                        mc.player!!.posY - 10E-4, mc.player!!.posZ, mc.player!!.onGround))
                mc.connection!!.sendPacket(CPacketPlayer(true))
            }
            "aac3.3.15" -> if (mc.player!!.fallDistance > 2) {
                if (!mc.isIntegratedServerRunning) mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, Double.NaN, mc.player!!.posZ, false))
                mc.player!!.fallDistance = (-9999).toFloat()
            }
            "spartan" -> {
                spartanTimer.update()
                if (mc.player!!.fallDistance > 1.5 && spartanTimer.hasTimePassed(10)) {
                    mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX,
                            mc.player!!.posY + 10, mc.player!!.posZ, true))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX,
                            mc.player!!.posY - 10, mc.player!!.posZ, true))
                    spartanTimer.reset()
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val mode = modeValue.get()
        if (packet is CPacketPlayer) {
            val playerPacket = packet
            if (mode.equals("SpoofGround", ignoreCase = true)) playerPacket.onGround = true
            if (mode.equals("NoGround", ignoreCase = true)) playerPacket.onGround = false
            if (mode.equals("Hypixel", ignoreCase = true)
                    && mc.player != null && mc.player!!.fallDistance > 1.5) playerPacket.onGround = mc.player!!.ticksExisted % 2 == 0
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (collideBlock(mc.player!!.entityBoundingBox) { it is BlockLiquid } || collideBlock(AxisAlignedBB(mc.player!!.entityBoundingBox.maxX, mc.player!!.entityBoundingBox.maxY, mc.player!!.entityBoundingBox.maxZ, mc.player!!.entityBoundingBox.minX, mc.player!!.entityBoundingBox.minY - 0.01, mc.player!!.entityBoundingBox.minZ)) { it is BlockLiquid })
            return

        if (modeValue.get().equals("laac", ignoreCase = true)) {
            if (!jumped && !mc.player!!.onGround && !mc.player!!.isOnLadder && !mc.player!!.isInWater && !mc.player!!.isInWeb && mc.player!!.motionY < 0.0) {
                event.x = 0.0
                event.z = 0.0
            }
        }
    }

    @EventTarget
    private fun onMotionUpdate(event: MotionEvent) {
        if (!(modeValue.get().equals("MLG", ignoreCase = true) || modeValue.get().equals("GrimAC", ignoreCase = true)))
            return

        if (event.eventState == EventState.PRE) {
            currentMlgRotation = null

            mlgTimer.update()

            if (!mlgTimer.hasTimePassed(10))
                return

            if (mc.player!!.fallDistance > minFallDistance.get()) {
                val fallingPlayer = FallingPlayer(
                        mc.player!!.posX,
                        mc.player!!.posY,
                        mc.player!!.posZ,
                        mc.player!!.motionX,
                        mc.player!!.motionY,
                        mc.player!!.motionZ,
                        mc.player!!.rotationYaw,
                        mc.player!!.moveStrafing,
                        mc.player!!.moveForward
                )

                val maxDist: Double = mc.playerController.blockReachDistance + 1.5

                val collision = fallingPlayer.findCollision(ceil(1.0 / mc.player!!.motionY * -maxDist).toInt())
                        ?: return

                var ok: Boolean = Vec3d(mc.player!!.posX, mc.player!!.posY + mc.player!!.eyeHeight, mc.player!!.posZ).distanceTo(Vec3d(collision.pos).addVector(0.5, 0.5, 0.5)) < mc.playerController.blockReachDistance + sqrt(0.75)

                if (mc.player!!.motionY < collision.pos.y + 1 - mc.player!!.posY) {
                    ok = true
                }

                if (!ok)
                    return

                var index = -1

                for (i in 36..44) {
                    val itemStack = mc.player!!.inventoryContainer.getSlot(i).stack

                    if (itemStack != null && (itemStack.item == Items.WATER_BUCKET || itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.WEB)) {
                        index = i - 36

                        if (mc.player!!.inventory.currentItem == index)
                            break
                    }
                }
                if (index == -1)
                    return

                currentMlgItemIndex = index
                currentMlgBlock = collision.pos

                if (mc.player!!.inventory.currentItem != index) {
                    mc.connection!!.sendPacket(CPacketHeldItemChange(index))
                }

                currentMlgRotation = RotationUtils.faceBlock(collision.pos)
                currentMlgRotation!!.rotation.toPlayer(mc.player!!)
            }
        } else if (currentMlgRotation != null) {
            val stack = mc.player!!.inventory.getStackInSlot(currentMlgItemIndex + 36)

            if (stack.item is ItemBucket) {
                mc.playerController.sendUseItem(mc.playerController, mc.player!!, mc.world!!, stack)
            } else {
                val dirVec: Vec3i = EnumFacing.UP.directionVec

                if (mc.playerController.sendUseItem(mc.playerController, mc.player!!, mc.world!!, stack)) {
                    mlgTimer.reset()
                }
            }
            if (mc.player!!.inventory.currentItem != currentMlgItemIndex)
                mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent?) {
        jumped = true
    }

    override val tag: String
        get() = modeValue.get()
}