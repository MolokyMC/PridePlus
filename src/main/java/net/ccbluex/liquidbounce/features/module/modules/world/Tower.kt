/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.BlockOverlay
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MathUtils.wrapAngleTo180_float
import net.ccbluex.liquidbounce.utils.PlaceRotation
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBush
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.stats.StatList
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.truncate

@ModuleInfo(
    name = "Tower",
    description = "Automatically builds a tower beneath you.",
    category = ModuleCategory.WORLD,
    keyBind = Keyboard.KEY_O
)
class Tower : Module() {
    /**
     * OPTIONS
     */
    private val modeValue = ListValue(
        "Mode",
        arrayOf("Jump", "Motion", "ConstantMotion", "MotionTP", "Packet", "Teleport", "AAC3.3.9", "AAC3.6.4"),
        "Motion"
    )
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
    private val swingValue = BoolValue("Swing", true)
    private val stopWhenBlockAbove = BoolValue("StopWhenBlockAbove", false)
    private val rotationsValue = BoolValue("Rotations", true)
    private val keepRotationValue = BoolValue("KeepRotation", false)
    private val onJumpValue = BoolValue("OnJump", false)
    private val matrixValue = BoolValue("Matrix", false)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post"), "Post")
    private val timerValue = FloatValue("Timer", 1f, 0.01f, 10f)

    // Jump mode
    private val jumpMotionValue = FloatValue("JumpMotion", 0.42f, 0.3681289f, 0.79f)
    private val jumpDelayValue = IntegerValue("JumpDelay", 0, 0, 20)

    // ConstantMotion
    private val constantMotionValue = FloatValue("ConstantMotion", 0.42f, 0.1f, 1f)
    private val constantMotionJumpGroundValue = FloatValue("ConstantMotionJumpGround", 0.79f, 0.76f, 1f)

    // Teleport
    private val teleportHeightValue = FloatValue("TeleportHeight", 1.15f, 0.1f, 5f)
    private val teleportDelayValue = IntegerValue("TeleportDelay", 0, 0, 20)
    private val teleportGroundValue = BoolValue("TeleportGround", true)
    private val teleportNoMotionValue = BoolValue("TeleportNoMotion", false)

    // Render
    private val counterDisplayValue = BoolValue("Counter", true)

    /**
     * MODULE
     */
    // Target block
    private var placeInfo: PlaceInfo? = null

    // Rotation lock
    private var lockRotation: Rotation? = null

    // Mode stuff
    private val timer = TickTimer()
    private var jumpGround = 0.0

    // AutoBlock
    private var slot = 0

    override fun onEnable() {
        val player = mc.player ?: return

        slot = player.inventory.currentItem

    }

    override fun onDisable() {
        val player = mc.player ?: return

        (mc.timer as IMixinTimer).timerSpeed = 1f
        lockRotation = null

        if (slot != player.inventory.currentItem) {
            mc.connection!!.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (onJumpValue.get() && !mc.gameSettings.keyBindJump.isKeyDown) return
        val player = mc.player ?: return

        // Lock Rotation
        if (rotationsValue.get() && keepRotationValue.get() && lockRotation != null) {
            RotationUtils.setTargetRotation(lockRotation)
        }

        (mc.timer as IMixinTimer).timerSpeed = timerValue.get()
        val eventState = event.eventState

        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) {
            place()
        }

        if (eventState == EventState.PRE) {
            placeInfo = null
            timer.update()

            val update = if (!autoBlockValue.get().equals("Off", ignoreCase = true)) {
                InventoryUtils.findAutoBlockBlock() != -1 || player.heldItemMainhand != null &&
                    player.heldItemMainhand.item is ItemBlock
            } else {
                player.heldItemMainhand != null && player.heldItemMainhand.item is ItemBlock
            }

            if (update) {
                if (!stopWhenBlockAbove.get() ||
                    getBlock(
                        BlockPos(
                            player.posX,
                            player.posY + 2,
                            player.posZ
                        )
                    ) is BlockAir
                ) {
                    move()
                }
                val blockPos = BlockPos(player.posX, player.posY - 1.0, player.posZ)
                if (mc.world!!.getBlockState(blockPos).block is BlockAir) {
                    if (search(blockPos) && rotationsValue.get()) {
                        val vecRotation = RotationUtils.faceBlock(blockPos)
                        if (vecRotation != null) {
                            RotationUtils.setTargetRotation(vecRotation.rotation)
                            placeInfo!!.vec3 = vecRotation.vec
                        }
                    }
                }
            }
        }
    }

    //Send jump packets, bypasses Hypixel.
    private fun fakeJump() {
        mc.player!!.isAirBorne = true
        mc.player!!.addStat(StatList.JUMP)
    }

    /**
     * Move player
     */
    private fun move() {
        val player = mc.player ?: return

        when (modeValue.get().toLowerCase()) {
            "jump" -> if (player.onGround && timer.hasTimePassed(jumpDelayValue.get())) {
                fakeJump()
                player.motionY = jumpMotionValue.get().toDouble()
                timer.reset()
            }
            "motion" -> if (player.onGround) {
                fakeJump()
                player.motionY = 0.42
            } else if (player.motionY < 0.1) {
                player.motionY = -0.3
            }
            "motiontp" -> if (player.onGround) {
                fakeJump()
                player.motionY = 0.42
            } else if (player.motionY < 0.23) {
                player.setPosition(player.posX, truncate(player.posY), player.posZ)
            }
            "packet" -> if (player.onGround && timer.hasTimePassed(2)) {
                fakeJump()
                mc.connection!!.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY + 0.42, player.posZ, false
                    )
                )
                mc.connection!!.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY + 0.753, player.posZ, false
                    )
                )
                player.setPosition(player.posX, player.posY + 1.0, player.posZ)
                timer.reset()
            }
            "teleport" -> {
                if (teleportNoMotionValue.get()) {
                    player.motionY = 0.0
                }
                if ((player.onGround || !teleportGroundValue.get()) && timer.hasTimePassed(teleportDelayValue.get())) {
                    fakeJump()
                    player.setPositionAndUpdate(
                        player.posX,
                        player.posY + teleportHeightValue.get(),
                        player.posZ
                    )
                    timer.reset()
                }
            }
            "constantmotion" -> {
                if (player.onGround) {
                    fakeJump()
                    jumpGround = player.posY
                    player.motionY = constantMotionValue.get().toDouble()
                }
                if (player.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump()
                    player.setPosition(
                        player.posX,
                        truncate(player.posY),
                        player.posZ
                    ) // TODO: toInt() required?
                    player.motionY = constantMotionValue.get().toDouble()
                    jumpGround = player.posY
                }
            }
            "aac3.3.9" -> {
                if (player.onGround) {
                    fakeJump()
                    player.motionY = 0.4001
                }
                (mc.timer as IMixinTimer).timerSpeed = 1f
                if (player.motionY < 0) {
                    player.motionY -= 0.00000945
                    (mc.timer as IMixinTimer).timerSpeed = 1.6f
                }
            }
            "aac3.6.4" -> if (player.ticksExisted % 4 == 1) {
                player.motionY = 0.4195464
                player.setPosition(player.posX - 0.035, player.posY, player.posZ)
            } else if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.5
                player.setPosition(player.posX + 0.035, player.posY, player.posZ)
            }
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (placeInfo == null) return
        val player = mc.player ?: return

        // AutoBlock
        var itemStack = player.heldItemMainhand
        if (itemStack == null || itemStack.item is ItemBlock || (itemStack.item as ItemBlock).block is BlockBush) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            if (blockSlot == -1)
                return

            when (autoBlockValue.get()) {
                "Off" -> return
                "Pick" -> {
                    mc.player!!.inventory.currentItem = blockSlot - 36
                    mc.playerController.updateController()
                }
                "Spoof" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection!!.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
                "Switch" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection!!.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
            }
            itemStack = player.inventoryContainer.getSlot(blockSlot).stack
        }

        // Place block
        if (mc.playerController.processRightClickBlock(
                player,
                mc.world!!,
                placeInfo!!.blockPos,
                placeInfo!!.enumFacing,
                placeInfo!!.vec3,
                EnumHand.MAIN_HAND
            ) == EnumActionResult.SUCCESS
        ) {
            if (swingValue.get()) {
                player.swingArm(EnumHand.MAIN_HAND)
            } else {
                mc.connection!!.sendPacket(CPacketAnimation())
            }
        }
        if (autoBlockValue.get().equals("Switch", true)) {
            if (slot != mc.player!!.inventory.currentItem)
                mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
        }
        placeInfo = null
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @return
     */
    private fun search(blockPosition: BlockPos): Boolean {
        val player = mc.player ?: return false
        if (!isReplaceable(blockPosition)) return false

        val eyesPos = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        var placeRotation: PlaceRotation? = null
        for (facingType in EnumFacing.values()) {
            val side = facingType
            val neighbor = blockPosition.offset(side)

            if (!canBeClicked(neighbor))
                continue

            val dirVec = Vec3d(side.directionVec)

            val matrix = matrixValue.get()
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val posVec = Vec3d(blockPosition).addVector(
                            if (matrix) 0.5 else xSearch,
                            if (matrix) 0.5 else ySearch,
                            if (matrix) 0.5 else zSearch
                        )

                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3d(dirVec.x * 0.5, dirVec.y * 0.5, dirVec.z * 0.5))
                        if (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.world!!.rayTraceBlocks(
                                eyesPos, hitVec, false,
                                true, false
                            ) != null
                        ) {
                            zSearch += 0.1
                            continue
                        }

                        // face block
                        val diffX = hitVec.x - eyesPos.x
                        val diffY = hitVec.y - eyesPos.y
                        val diffZ = hitVec.z - eyesPos.z
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

                        val rotation = Rotation(
                            wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.x * distanceSqPosVec,
                            rotationVector.y * distanceSqPosVec,
                            rotationVector.z * distanceSqPosVec
                        )
                        val obj = mc.world!!.rayTraceBlocks(
                            eyesPos, vector, false,
                            false, true
                        )
                        if (!(obj!!.typeOfHit == RayTraceResult.Type.BLOCK && obj.blockPos == neighbor)) {
                            zSearch += 0.1
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) <
                            RotationUtils.getRotationDifference(placeRotation.rotation)
                        ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }
        if (placeRotation == null) return false
        if (rotationsValue.get()) {
            RotationUtils.setTargetRotation(placeRotation.rotation, 0)
            lockRotation = placeRotation.rotation
        }
        placeInfo = placeRotation.placeInfo
        return true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.player == null) return
        val packet = event.packet
        if (packet is CPacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    /**
     * Tower visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (counterDisplayValue.get()) {
            GL11.glPushMatrix()
            val blockOverlay = Pride.moduleManager.getModule(BlockOverlay::class.java) as BlockOverlay
            if (blockOverlay.state && blockOverlay.infoValue.get() && blockOverlay.currentBlock != null) {
                GL11.glTranslatef(0f, 15f, 0f)
            }
            val info = "Blocks: §7$blocksAmount"
            val scaledResolution = ScaledResolution(mc)

            RenderUtils.drawBorderedRect(
                scaledResolution.scaledWidth / 2 - 2.toFloat(),
                scaledResolution.scaledHeight / 2 + 5.toFloat(),
                scaledResolution.scaledWidth / 2 + Fonts.font40.getStringWidth(info) + 2.toFloat(),
                scaledResolution.scaledHeight / 2 + 16.toFloat(), 3f, Color.BLACK.rgb, Color.BLACK.rgb
            )

            GlStateManager.resetColor()

            Fonts.font40.drawString(
                info, scaledResolution.scaledWidth / 2.toFloat(),
                scaledResolution.scaledHeight / 2 + 7.toFloat(), Color.WHITE.rgb
            )
            GL11.glPopMatrix()
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (onJumpValue.get()) event.cancelEvent()
    }

    /**
     * @return hotbar blocks amount
     */
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.player!!.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    val block = (itemStack.item as ItemBlock).block
                    if (mc.player!!.heldItemMainhand == itemStack || !InventoryUtils.BLOCK_BLACKLIST.contains(block)) {
                        amount += itemStack.stackSize
                    }
                }
            }
            return amount
        }

    override val tag: String
        get() = modeValue.get()
}