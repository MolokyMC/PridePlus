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
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.block.BlockAir
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
class Fly : Module() {
    val modeValue = ListValue("Mode", arrayOf(
            "Vanilla",
            "SmoothVanilla",

            // NCP
            "NCP",
            "OldNCP",

            // AAC
            "AAC1.9.10",
            "AAC3.0.5",
            "AAC3.1.6-Gomme",
            "AAC3.3.12",
            "AAC3.3.12-Glide",
            "AAC3.3.13",

            // CubeCraft
            "CubeCraft",

            // Hypixel
            "Hypixel",
            "BoostHypixel",
            "FreeHypixel",

            // Rewinside
            "Rewinside",
            "TeleportRewinside",

            // Other server specific flys
            "Mineplex",
            "NeruxVace",
            "Minesucht",
            "Redesky",

            // Spartan
            "Spartan",
            "Spartan2",
            "BugSpartan",

            // Other anticheats
            "MineSecure",
            "HawkEye",
            "HAC",
            "WatchCat",

            // Other
            "Jetpack",
            "KeepAlive",
            "Flag"
    ), "Vanilla")
    private val vanillaSpeedValue = FloatValue("VanillaSpeed", 2f, 0f, 5f)
    private val vanillaKickBypassValue = BoolValue("VanillaKickBypass", false)
    private val ncpMotionValue = FloatValue("NCPMotion", 0f, 0f, 1f)

    // AAC
    private val aacSpeedValue = FloatValue("AAC1.9.10-Speed", 0.3f, 0f, 1f)
    private val aacFast = BoolValue("AAC3.0.5-Fast", true)
    private val aacMotion = FloatValue("AAC3.3.12-Motion", 10f, 0.1f, 10f)
    private val aacMotion2 = FloatValue("AAC3.3.13-Motion", 10f, 0.1f, 10f)

    // Hypixel
    private val hypixelBoost = BoolValue("Hypixel-Boost", true)
    private val hypixelBoostDelay = IntegerValue("Hypixel-BoostDelay", 1200, 0, 2000)
    private val hypixelBoostTimer = FloatValue("Hypixel-BoostTimer", 1f, 0f, 5f)
    private val mineplexSpeedValue = FloatValue("MineplexSpeed", 1f, 0.5f, 10f)
    private val neruxVaceTicks = IntegerValue("NeruxVace-Ticks", 6, 0, 20)
    private val redeskyHeight = FloatValue("Redesky-Height", 4f, 1f, 7f)

    // Visuals
    private val markValue = BoolValue("Mark", true)
    private var startY = 0.0
    private val flyTimer = MSTimer()
    private val groundTimer = MSTimer()
    private var noPacketModify = false
    private var aacJump = 0.0
    private var aac3delay = 0
    private var aac3glideDelay = 0
    private var noFlag = false
    private val mineSecureVClipTimer = MSTimer()
    private val spartanTimer = TickTimer()
    private var minesuchtTP: Long = 0
    private val mineplexTimer = MSTimer()
    private var wasDead = false
    private val hypixelTimer = TickTimer()
    private var boostHypixelState = 1
    private var moveSpeed = 0.0
    private var lastDistance = 0.0
    private var failedStart = false
    private val cubecraft2TickTimer = TickTimer()
    private val cubecraftTeleportTickTimer = TickTimer()
    private val freeHypixelTimer = TickTimer()
    private var freeHypixelYaw = 0f
    private var freeHypixelPitch = 0f

    override fun onEnable() {
        val player = mc.player ?: return

        flyTimer.reset()
        noPacketModify = true

        val x = player.posX
        val y = player.posY
        val z = player.posZ

        val mode = modeValue.get()

        run {
            when (mode.toLowerCase()) {
                "ncp" -> {
                    if (!player.onGround)
                        return@run

                    for (i in 0..64) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.049, z, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                    }

                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.1, z, true))

                    player.motionX *= 0.1
                    player.motionZ *= 0.1
                    player.swingArm(EnumHand.MAIN_HAND)
                }
                "oldncp" -> {
                    if (!player.onGround)
                        return@run

                    for (i in 0..3) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 1.01, z, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                    }

                    player.jump()
                    player.swingArm(EnumHand.MAIN_HAND)
                }
                "bugspartan" -> {
                    for (i in 0..64) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.049, z, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
                    }

                    mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.1, z, true))

                    player.motionX *= 0.1
                    player.motionZ *= 0.1
                    player.swingArm(EnumHand.MAIN_HAND)
                }
                "infinitycubecraft" -> ClientUtils.displayChatMessage("§8[§c§lCubeCraft-§a§lFly§8] §aPlace a block before landing.")
                "infinityvcubecraft" -> {
                    ClientUtils.displayChatMessage("§8[§c§lCubeCraft-§a§lFly§8] §aPlace a block before landing.")

                    player.setPosition(player.posX, player.posY + 2, player.posZ)
                }
                "boosthypixel" -> {
                    if (!player.onGround)
                        return@run

                    for (i in 0..9) {
                        //Imagine flagging to NCP.
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, true))
                    }

                    var fallDistance = 3.0125 //add 0.0125 to ensure we get the fall dmg

                    while (fallDistance > 0) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.0624986421, player.posZ, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.0625, player.posZ, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.0624986421, player.posZ, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.0000013579, player.posZ, false))
                        fallDistance -= 0.0624986421
                    }

                    mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, true))

                    player.jump()

                    player.posY += 0.42f // Visual
                    boostHypixelState = 1
                    moveSpeed = 0.1
                    lastDistance = 0.0
                    failedStart = false
                }
                "redesky" -> {
                    if(mc.player!!.onGround) {
                        redeskyVClip1(redeskyHeight.get())
                    }
                }
            }
        }

        startY = player.posY
        aacJump = -3.8
        noPacketModify = false

        if (mode.equals("freehypixel", ignoreCase = true)) {
            freeHypixelTimer.reset()
            player.setPositionAndUpdate(player.posX, player.posY + 0.42, player.posZ)
            freeHypixelYaw = player.rotationYaw
            freeHypixelPitch = player.rotationPitch
        }

        super.onEnable()
    }

    override fun onDisable() {
        wasDead = false
        redeskySpeed(0)

        val player = mc.player ?: return

        noFlag = false

        val mode = modeValue.get()

        if (!mode.toUpperCase().startsWith("AAC") && !mode.equals("Hypixel", ignoreCase = true) &&
                !mode.equals("CubeCraft", ignoreCase = true)) {
            player.motionX = 0.0
            player.motionY = 0.0
            player.motionZ = 0.0
        }
        if(mode.equals("Redesky", ignoreCase = true)) {
            redeskyHClip2(0.0)
        }

        player.capabilities.isFlying = false
        (mc.timer as IMixinTimer).timerSpeed = 1f
        player.speedInAir = 0.02f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val vanillaSpeed = vanillaSpeedValue.get()
        val player = mc.player!!

        run {
            when (modeValue.get().toLowerCase()) {
                "vanilla" -> {
                    player.capabilities.isFlying = false
                    player.motionY = 0.0
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += vanillaSpeed
                    if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vanillaSpeed
                    MovementUtils.strafe(vanillaSpeed)
                    handleVanillaKickBypass()
                }
                "smoothvanilla" -> {
                    player.capabilities.isFlying = true
                    handleVanillaKickBypass()
                }
                "cubecraft" -> {
                    (mc.timer as IMixinTimer).timerSpeed = 0.6f
                    cubecraftTeleportTickTimer.update()
                }
                "ncp" -> {
                    player.motionY = (-ncpMotionValue.get()).toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY = -0.5
                    MovementUtils.strafe()
                }
                "oldncp" -> {
                    if (startY > player.posY)
                        player.motionY = -0.000000000000000000000000000000001
                    if (mc.gameSettings.keyBindSneak.isKeyDown)
                        player.motionY = -0.2
                    if (mc.gameSettings.keyBindJump.isKeyDown && player.posY < startY - 0.1)
                        player.motionY = 0.2
                    MovementUtils.strafe()
                }
                "aac1.9.10" -> {
                    if (mc.gameSettings.keyBindJump.isKeyDown)
                        aacJump += 0.2
                    if (mc.gameSettings.keyBindSneak.isKeyDown)
                        aacJump -= 0.2

                    if (startY + aacJump > player.posY) {
                        mc.connection!!.sendPacket(CPacketPlayer(true))
                        player.motionY = 0.8
                        MovementUtils.strafe(aacSpeedValue.get())
                    }
                    MovementUtils.strafe()
                }
                "aac3.0.5" -> {
                    if (aac3delay == 2) player.motionY = 0.1 else if (aac3delay > 2)
                        aac3delay = 0
                    if (aacFast.get()) {
                        if (player.movementInput.moveStrafe == 0.0f)
                            player.jumpMovementFactor = 0.08f
                        else
                            player.jumpMovementFactor = 0f
                    }
                    aac3delay++
                }
                "aac3.1.6-gomme" -> {
                    player.capabilities.isFlying = true
                    if (aac3delay == 2) {
                        player.motionY += 0.05
                    } else if (aac3delay > 2) {
                        player.motionY -= 0.05
                        aac3delay = 0
                    }
                    aac3delay++
                    if (!noFlag) mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, player.onGround))
                    if (player.posY <= 0.0) noFlag = true
                }
                "flag" -> {
                    mc.connection!!.sendPacket(CPacketPlayer.PositionRotation(player.posX + player.motionX * 999, player.posY + (if (mc.gameSettings.keyBindJump.isKeyDown) 1.5624 else 0.00000001) - if (mc.gameSettings.keyBindSneak.isKeyDown) 0.0624 else 0.00000002, player.posZ + player.motionZ * 999, player.rotationYaw, player.rotationPitch, true))
                    mc.connection!!.sendPacket(CPacketPlayer.PositionRotation(player.posX + player.motionX * 999, player.posY - 6969, player.posZ + player.motionZ * 999, player.rotationYaw, player.rotationPitch, true))
                    player.setPosition(player.posX + player.motionX * 11, player.posY, player.posZ + player.motionZ * 11)
                    player.motionY = 0.0
                }
                "keepalive" -> {
                    mc.connection!!.sendPacket(CPacketKeepAlive())
                    player.capabilities.isFlying = false
                    player.motionY = 0.0
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += vanillaSpeed
                    if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vanillaSpeed
                    MovementUtils.strafe(vanillaSpeed)
                }
                "minesecure" -> {
                    player.capabilities.isFlying = false
                    if (!mc.gameSettings.keyBindSneak.isKeyDown)
                        player.motionY = -0.01

                    player.motionX = 0.0
                    player.motionZ = 0.0
                    MovementUtils.strafe(vanillaSpeed)
                    if (mineSecureVClipTimer.hasTimePassed(150) && mc.gameSettings.keyBindJump.isKeyDown) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 5, player.posZ, false))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(0.5, -1000.0, 0.5, false))
                        val yaw = Math.toRadians(player.rotationYaw.toDouble())
                        val x = -sin(yaw) * 0.4
                        val z = cos(yaw) * 0.4

                        player.setPosition(player.posX + x, player.posY, player.posZ + z)
                        mineSecureVClipTimer.reset()
                    }
                }
                "hac" -> {
                    player.motionX *= 0.8
                    player.motionZ *= 0.8
                    player.motionY = if (player.motionY <= -0.42) 0.42 else -0.42
                }
                "hawkeye" -> player.motionY = if (player.motionY <= -0.42) 0.42 else -0.42
                "teleportrewinside" -> {
                    val vectorStart = Vec3d(player.posX, player.posY, player.posZ)
                    val yaw = -player.rotationYaw
                    val pitch = -player.rotationPitch
                    val length = 9.9
                    val vectorEnd = Vec3d(
                            sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.x,
                            sin(Math.toRadians(pitch.toDouble())) * length + vectorStart.y,
                            cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.z
                    )
                    mc.connection!!.sendPacket(CPacketPlayer.Position(vectorEnd.x, player.posY + 2, vectorEnd.z, true))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(vectorStart.x, player.posY + 2, vectorStart.z, true))
                    player.motionY = 0.0
                }
                "minesucht" -> {
                    val posX = player.posX
                    val posY = player.posY
                    val posZ = player.posZ

                    if (!mc.gameSettings.keyBindForward.isKeyDown)
                        return@run

                    if (System.currentTimeMillis() - minesuchtTP > 99) {
                        val vec3: Vec3d = player.getPositionEyes(0.0f)
                        val vec31: Vec3d = mc.player!!.getLook(0.0f)
                        val vec32: Vec3d = vec3.addVector(vec31.x * 7, vec31.y * 7, vec31.z * 7)
                        if (player.fallDistance > 0.8) {
                            player.connection.sendPacket(CPacketPlayer.Position(posX, posY + 50, posZ, false))
                            mc.player!!.fall(100.0f, 100.0f)
                            player.fallDistance = 0.0f
                            player.connection.sendPacket(CPacketPlayer.Position(posX, posY + 20, posZ, true))
                        }
                        player.connection.sendPacket(CPacketPlayer.Position(vec32.x, player.posY + 50, vec32.z, true))
                        player.connection.sendPacket(CPacketPlayer.Position(posX, posY, posZ, false))
                        player.connection.sendPacket(CPacketPlayer.Position(vec32.x, posY, vec32.z, true))
                        player.connection.sendPacket(CPacketPlayer.Position(posX, posY, posZ, false))
                        minesuchtTP = System.currentTimeMillis()
                    } else {
                        player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, false))
                        player.connection.sendPacket(CPacketPlayer.Position(posX, posY, posZ, true))
                    }
                }
                "jetpack" -> if (mc.gameSettings.keyBindJump.isKeyDown) {
//                    mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.getParticleID(), player.posX, player.posY + 0.2, player.posZ, -player.motionX, -0.5, -player.motionZ)
                    player.motionY += 0.15
                    player.motionX *= 1.1
                    player.motionZ *= 1.1
                }
                "mineplex" -> if (player.inventory.getCurrentItem() == null) {
                    if (mc.gameSettings.keyBindJump.isKeyDown && mineplexTimer.hasTimePassed(100)) {
                        player.setPosition(player.posX, player.posY + 0.6, player.posZ)
                        mineplexTimer.reset()
                    }
                    if (mc.player!!.isSneaking && mineplexTimer.hasTimePassed(100)) {
                        player.setPosition(player.posX, player.posY - 0.6, player.posZ)
                        mineplexTimer.reset()
                    }
                    val blockPos = BlockPos(player.posX, mc.player!!.entityBoundingBox.minY - 1, player.posZ)
                    val vec: Vec3d = Vec3d(blockPos).addVector(0.4, 0.4, 0.4).add(Vec3d(EnumFacing.UP.directionVec))
                    mc.playerController.processRightClickBlock(player, mc.world!!, player.inventory.getCurrentItem()!!, EnumFacing.UP, Vec3d(vec.x * 0.4f, vec.y * 0.4f, vec.z * 0.4f), EnumHand.MAIN_HAND)
                    MovementUtils.strafe(0.27f)
                    (mc.timer as IMixinTimer).timerSpeed = 1 + mineplexSpeedValue.get()
                } else {
                    (mc.timer as IMixinTimer).timerSpeed = 1.0f
                    state = false
                    ClientUtils.displayChatMessage("§8[§c§lMineplex-§a§lFly§8] §aSelect an empty slot to fly.")
                }
                "aac3.3.12" -> {
                    if (player.posY < -70) player.motionY = aacMotion.get().toDouble()
                    (mc.timer as IMixinTimer).timerSpeed = 1f
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        (mc.timer as IMixinTimer).timerSpeed = 0.2f
                        mc.rightClickDelayTimer = 0
                    }
                }
                "aac3.3.12-glide" -> {
                    if (!player.onGround) aac3glideDelay++
                    if (aac3glideDelay == 2) (mc.timer as IMixinTimer).timerSpeed = 1f
                    if (aac3glideDelay == 12) (mc.timer as IMixinTimer).timerSpeed = 0.1f
                    if (aac3glideDelay >= 12 && !player.onGround) {
                        aac3glideDelay = 0
                        player.motionY = .015
                    }
                }
                "aac3.3.13" -> {
                    if (player.isDead) wasDead = true
                    if (wasDead || player.onGround) {
                        wasDead = false
                        player.motionY = aacMotion2.get().toDouble()
                        player.onGround = false
                    }
                    (mc.timer as IMixinTimer).timerSpeed = 1f
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        (mc.timer as IMixinTimer).timerSpeed = 0.2f
                        mc.rightClickDelayTimer = 0
                    }
                }
                "watchcat" -> {
                    MovementUtils.strafe(0.15f)
                    mc.player!!.isSprinting = true

                    if (player.posY < startY + 2) {
                        player.motionY = Math.random() * 0.5
                        return@run
                    }

                    if (startY > player.posY)
                        MovementUtils.strafe(0f)
                }
                "spartan" -> {
                    player.motionY = 0.0

                    spartanTimer.update()
                    if (spartanTimer.hasTimePassed(12)) {
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 8, player.posZ, true))
                        mc.connection!!.sendPacket(CPacketPlayer.Position(player.posX, player.posY - 8, player.posZ, true))
                        spartanTimer.reset()
                    }
                }
                "spartan2" -> {
                    MovementUtils.strafe(0.264f)
                    if (player.ticksExisted % 8 == 0) player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 10, player.posZ, true))
                }
                "neruxvace" -> {
                    if (!player.onGround) aac3glideDelay++
                    if (aac3glideDelay >= neruxVaceTicks.get() && !player.onGround) {
                        aac3glideDelay = 0
                        player.motionY = .015
                    }
                }
                "hypixel" -> {
                    val boostDelay = hypixelBoostDelay.get()
                    if (hypixelBoost.get() && !flyTimer.hasTimePassed(boostDelay.toLong())) {
                        (mc.timer as IMixinTimer).timerSpeed = 1f + hypixelBoostTimer.get() * (flyTimer.hasTimeLeft(boostDelay.toLong()).toFloat() / boostDelay.toFloat())
                    }
                    hypixelTimer.update()
                    if (hypixelTimer.hasTimePassed(2)) {
                        player.setPosition(player.posX, player.posY + 1.0E-5, player.posZ)
                        hypixelTimer.reset()
                    }
                }
                "freehypixel" -> {
                    if (freeHypixelTimer.hasTimePassed(10)) {
                        player.capabilities.isFlying = true
                        return@run
                    } else {
                        player.rotationYaw = freeHypixelYaw
                        player.rotationPitch = freeHypixelPitch
                        player.motionY = 0.0
                        player.motionZ = player.motionY
                        player.motionX = player.motionZ
                    }
                    if (startY == BigDecimal(player.posY).setScale(3, RoundingMode.HALF_DOWN).toDouble()) freeHypixelTimer.update()
                }
                "bugspartan" -> {
                    player.capabilities.isFlying = false
                    player.motionY = 0.0
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown)
                        player.motionY += vanillaSpeed
                    if (mc.gameSettings.keyBindSneak.isKeyDown)
                        player.motionY -= vanillaSpeed

                    MovementUtils.strafe(vanillaSpeed)
                }
                "redesky" -> {
                    (mc.timer as IMixinTimer).timerSpeed = 0.3f
                    redeskyHClip2(7.0)
                    redeskyVClip2(10.0)
                    redeskyVClip1(-0.5f)
                    redeskyHClip1(2.0)
                    redeskySpeed(1)
                    mc.player!!.motionY = -0.01
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (modeValue.get() == "GPT-GrimACTest") {
            if (!mc.player.capabilities.isFlying && !mc.player.isInWater && !mc.player.isInLava) {
                mc.player.capabilities.isFlying = true
                mc.player.sendPlayerAbilities()
            }
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.player.motionY += 1.0F
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown) {
                mc.player.motionY -= 1.0F
            }
            mc.player.jumpMovementFactor = 1.0F
            handleVanillaKickBypass()
        }
        if (modeValue.get().equals("boosthypixel", ignoreCase = true)) {
            when (event.eventState) {
                EventState.PRE -> {
                    hypixelTimer.update()
                    if (hypixelTimer.hasTimePassed(2)) {
                        mc.player!!.setPosition(mc.player!!.posX, mc.player!!.posY + 1.0E-5, mc.player!!.posZ)
                        hypixelTimer.reset()
                    }
                    if (!failedStart) mc.player!!.motionY = 0.0
                }
                EventState.POST -> {
                    val xDist = mc.player!!.posX - mc.player!!.prevPosX
                    val zDist = mc.player!!.posZ - mc.player!!.prevPosZ
                    lastDistance = sqrt(xDist * xDist + zDist * zDist)
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val mode = modeValue.get()
        if (!markValue.get() || mode.equals("Vanilla", ignoreCase = true) || mode.equals("SmoothVanilla", ignoreCase = true)) return
        val y = startY + 2.0
        RenderUtils.drawPlatform(y, if (mc.player!!.entityBoundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90), 1.0)
        when (mode.toLowerCase()) {
            "aac1.9.10" -> RenderUtils.drawPlatform(startY + aacJump, Color(0, 0, 255, 90), 1.0)
            "aac3.3.12" -> RenderUtils.drawPlatform(-70.0, Color(0, 0, 255, 90), 1.0)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (noPacketModify) return

        if (event.packet is CPacketPlayer) {
            val packetPlayer = event.packet

            val mode = modeValue.get()

            if (mode.equals("NCP", ignoreCase = true) || mode.equals("Rewinside", ignoreCase = true) ||
                    mode.equals("Mineplex", ignoreCase = true) && mc.player!!.inventory.getCurrentItem() == null) packetPlayer.onGround = true
            if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true)) packetPlayer.onGround = false
        }
        if (event.packet is SPacketPlayerPosLook) {
            val mode = modeValue.get()
            if (mode.equals("BoostHypixel", ignoreCase = true)) {
                failedStart = true
                ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.")
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        when (modeValue.get().toLowerCase()) {
            "cubecraft" -> {
                val yaw = Math.toRadians(mc.player!!.rotationYaw.toDouble())
                if (cubecraftTeleportTickTimer.hasTimePassed(2)) {
                    event.x = -sin(yaw) * 2.4
                    event.z = cos(yaw) * 2.4
                    cubecraftTeleportTickTimer.reset()
                } else {
                    event.x = -sin(yaw) * 0.2
                    event.z = cos(yaw) * 0.2
                }
            }
            "boosthypixel" -> {
                if (!MovementUtils.isMoving) {
                    event.x = 0.0
                    event.z = 0.0
                    return
                }
                if (failedStart)
                    return

                val amplifier = 1 + (if (mc.player!!.isPotionActive(MobEffects.SPEED)) 0.2 *
                        (mc.player!!.getActivePotionEffect(MobEffects.SPEED)!!.amplifier + 1.0) else 0.0)

                val baseSpeed = 0.29 * amplifier

                when (boostHypixelState) {
                    1 -> {
                        moveSpeed = (if (mc.player!!.isPotionActive(MobEffects.SPEED)) 1.56 else 2.034) * baseSpeed
                        boostHypixelState = 2
                    }
                    2 -> {
                        moveSpeed *= 2.16
                        boostHypixelState = 3
                    }
                    3 -> {
                        moveSpeed = lastDistance - (if (mc.player!!.ticksExisted % 2 == 0) 0.0103 else 0.0123) * (lastDistance - baseSpeed)
                        boostHypixelState = 4
                    }
                    else -> moveSpeed = lastDistance - lastDistance / 159.8
                }

                moveSpeed = max(moveSpeed, 0.3)

                val yaw = MovementUtils.direction

                event.x = -sin(yaw) * moveSpeed
                event.z = cos(yaw) * moveSpeed

                mc.player!!.motionX = event.x
                mc.player!!.motionZ = event.z
            }
            "freehypixel" -> if (!freeHypixelTimer.hasTimePassed(10)) event.zero()
        }
    }

    @EventTarget
    fun onBB(event: BlockBBEvent) {
        if (mc.player == null) return
        val mode = modeValue.get()
        if ((event.block is BlockAir) && (mode.equals("Hypixel", ignoreCase = true) ||
                        mode.equals("BoostHypixel", ignoreCase = true) || mode.equals("Rewinside", ignoreCase = true) ||
                        mode.equals("Mineplex", ignoreCase = true) && mc.player!!.inventory.getCurrentItem() == null) && event.y < mc.player!!.posY) event.boundingBox = AxisAlignedBB(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, mc.player!!.posY, event.z + 1.0)
    }

    @EventTarget
    fun onJump(e: JumpEvent) {
        val mode = modeValue.get()
        if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true) ||
                mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.player!!.inventory.getCurrentItem() == null) e.cancelEvent()
    }

    @EventTarget
    fun onStep(e: StepEvent) {
        val mode = modeValue.get()
        if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true) ||
                mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.player!!.inventory.getCurrentItem() == null) e.stepHeight = 0f
    }

    private fun handleVanillaKickBypass() {
        if (!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return
        val ground = calculateGround()
        run {
            var posY = mc.player!!.posY
            while (posY > ground) {
                mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, posY, mc.player!!.posZ, true))
                if (posY - 8.0 < ground) break // Prevent next step
                posY -= 8.0
            }
        }
        mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, ground, mc.player!!.posZ, true))
        var posY = ground
        while (posY < mc.player!!.posY) {
            mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, posY, mc.player!!.posZ, true))
            if (posY + 8.0 > mc.player!!.posY) break // Prevent next step
            posY += 8.0
        }
        mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, mc.player!!.posY, mc.player!!.posZ, true))
        groundTimer.reset()
    }

    private fun redeskyVClip1(vertical: Float) {
        mc.player!!.setPosition(mc.player!!.posX, mc.player!!.posY + vertical, mc.player!!.posZ)
    }

    private fun redeskyHClip1(horizontal: Double) {
        val playerYaw = Math.toRadians(mc.player!!.rotationYaw.toDouble())
        mc.player!!.setPosition(mc.player!!.posX + horizontal * -sin(playerYaw), mc.player!!.posY, mc.player!!.posZ + horizontal * cos(playerYaw))
    }

    private fun redeskyHClip2(horizontal: Double) {
        val playerYaw = Math.toRadians(mc.player!!.rotationYaw.toDouble())
        mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX + horizontal * -sin(playerYaw), mc.player!!.posY, mc.player!!.posZ + horizontal * cos(playerYaw), false))
    }

    private fun redeskyVClip2(vertical: Double) {
        mc.connection!!.sendPacket(CPacketPlayer.Position(mc.player!!.posX, mc.player!!.posY + vertical, mc.player!!.posZ, false))
    }

    private fun redeskySpeed(speed: Int) {
        val playerYaw = Math.toRadians(mc.player!!.rotationYaw.toDouble())
        mc.player!!.motionX = speed * -sin(playerYaw)
        mc.player!!.motionZ = speed * cos(playerYaw)
    }

    // TODO: Make better and faster calculation lol
    private fun calculateGround(): Double {
        val playerBoundingBox: AxisAlignedBB = mc.player!!.entityBoundingBox
        var blockHeight = 1.0
        var ground = mc.player!!.posY
        while (ground > 0.0) {
            val customBox = AxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ)
            if (mc.world!!.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }
            ground -= blockHeight
        }
        return 0.0
    }

    override val tag: String
        get() = modeValue.get()
}
