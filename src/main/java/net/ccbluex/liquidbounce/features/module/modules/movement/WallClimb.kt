/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "WallClimb", description = "Allows you to climb up walls like a spider.", category = ModuleCategory.MOVEMENT)
class WallClimb : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Simple", "CheckerClimb", "Clip", "AAC3.3.12", "AACGlide"), "Simple")
    private val clipMode = ListValue("ClipMode", arrayOf("Jump", "Fast"), "Fast")
    private val checkerClimbMotionValue = FloatValue("CheckerClimbMotion", 0f, 0f, 1f)

    private var glitch = false
    private var waited = 0

    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.player ?: return

        if (!player.collidedHorizontally || player.isOnLadder || player.isInWater || player.isInLava)
            return

        if ("simple".equals(modeValue.get(), ignoreCase = true)) {
            event.y = 0.2
            player.motionY = 0.0
        }
    }

    @EventTarget
    fun onUpdate(event: MotionEvent) {
        val player = mc.player

        if (event.eventState != EventState.POST || player == null)
            return


        when (modeValue.get().toLowerCase()) {
            "clip" -> {
                if (player.motionY < 0)
                    glitch = true
                if (player.collidedHorizontally) {
                    when (clipMode.get().toLowerCase()) {
                        "jump" -> if (player.onGround)
                            player.jump()
                        "fast" -> if (player.onGround)
                            player.motionY = 0.42
                        else if (player.motionY < 0)
                            player.motionY = -0.3
                    }
                }
            }
            "checkerclimb" -> {
                val isInsideBlock = collideBlockIntersects(player.entityBoundingBox) {
                    it !is BlockAir
                }
                val motion = checkerClimbMotionValue.get()

                if (isInsideBlock && motion != 0f)
                    player.motionY = motion.toDouble()
            }
            "aac3.3.12" -> if (player.collidedHorizontally && !player.isOnLadder) {
                waited++
                if (waited == 1)
                    player.motionY = 0.43
                if (waited == 12)
                    player.motionY = 0.43
                if (waited == 23)
                    player.motionY = 0.43
                if (waited == 29)
                    player.setPosition(player.posX, player.posY + 0.5, player.posZ)
                if (waited >= 30)
                    waited = 0
            } else if (player.onGround) waited = 0
            "aacglide" -> {
                if (!player.collidedHorizontally || player.isOnLadder) return
                player.motionY = -0.19
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is CPacketPlayer) {
            val packetPlayer = event.packet

            if (glitch) {
                val yaw = MovementUtils.direction.toFloat()
                packetPlayer.x = packetPlayer.x - sin(yaw) * 0.00000001
                packetPlayer.z = packetPlayer.z + cos(yaw) * 0.00000001
                glitch = false
            }
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        val player = mc.player ?: return

        val mode = modeValue.get()

        when (mode.toLowerCase()) {
            "checkerclimb" -> if (event.y > player.posY) event.boundingBox = null
            "clip" -> if (event.block != null && mc.player != null && (event.block is BlockAir) && event.y < player.posY && player.collidedHorizontally && !player.isOnLadder && !player.isInWater && !player.isInLava) event.boundingBox = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(player.posX, player.posY.toInt() - 1.0, player.posZ)
        }
    }
}