/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import me.utils.player.PlayerUtil.isMoving
import me.utils.render.GLUtils
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.*
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.GameType
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import java.awt.Robot
import java.awt.event.InputEvent
import java.util.*
import kotlin.math.*


@ModuleInfo(name = "KillAura", description = "Automatically attacks targets around you.",
    category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val cooldownValue = FloatValue("Cooldown", 1f, 0f, 1f)

    // Change Range
    val airBypass = BoolValue("AirChangeRange",true)
    // Range
    private val rangeValue = FloatValue(if(airBypass.get()) "Range" else "GroundRange", 3.7f, 1f, 8f)
    private val airRangeValue = FloatValue("AirRange", 3.3f, 1f, 8f).displayable { airBypass.get() }
    private val throughWallsRangeValue = FloatValue("ThroughWallsRange", 3f, 0f, 8f)
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction", "LivingTime", "HurtResitanTime"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    // Bypass
    val keepSprintValue = BoolValue("KeepSprint", true)
    private val stopSprintAir = BoolValue("StopSprintOnAir",true)


    // AutoBlock

    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Right", "Range", "Off"),"Range")
    private val BlockRangeValue = FloatValue("BlockRange", 3f, 0f, 8f)

    private val autoBlockPacketValue = ListValue("AutoBlockPacket", arrayOf("Packet", "Fake", "Mouse", "GameSettings", "UseItem"),"Packet")
    private  val vanillamode =  ListValue("VanillaMode", arrayOf("TryUseItem", "UseItem", "CPacketPlayerBlockPlacement"), "TryUseItem")
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true)
    private val delayedBlockValue = BoolValue("AutoBlock-AfterTck", false)
    private val afterAttackValue = BoolValue("AutoBlock-AfterAttack", false)
    private val autoBlockFacing = BoolValue("AutoBlockFacing",false)

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false).displayable { raycastValue.get() }
    private val livingRaycastValue = BoolValue("LivingRayCast", true).displayable { raycastValue.get() }

    // Bypass
    private val aacValue = BoolValue("AAC", false)

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }

    // Lighting
    private val lightingValue = BoolValue("Lighting", false)
    private val lightingModeValue = ListValue("Lighting-Mode", arrayOf("Dead", "Attack"), "Dead")
    private val lightingSoundValue = BoolValue("Lighting-Sound", true)
    private val randomCenterValue = BoolValue("RandomCenter", true)
    private val rotations = ListValue("RotationMode", arrayOf("None", "New", "Liquidbounce","BackTrack", "Test","Test1", "Test2", "HytRotation","GrimCenter","Down"), "New")
    private val outborderValue = BoolValue("Outborder", false)
    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent", "Smart"), "Off")
    val fovValue = FloatValue("FOV", 180f, 0f, 180f)
    private val hitableValue = BoolValue("AlwaysHitable",true)
    // Predict
    private val switchDelayValue = IntegerValue("SwitchDelay",300 ,1, 2000)
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500)
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50)

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid","FDP","Block","Jello", "Plat", "Red", "Sims", "None"),"FDP")
    private val colorModeValue =
        ListValue("JelloColor", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Health", "Gident"), "Custom")
    private val colorRedValue = IntegerValue("JelloRed", 255, 0, 255)
    private val colorGreenValue = IntegerValue("JelloGreen", 255, 0, 255)
    private val colorBlueValue = IntegerValue("JelloBlue", 255, 0, 255)

    private val colorAlphaValue = IntegerValue("JelloAlpha", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)

    private val colorTeam = BoolValue("JelloTeam", false)

    private val jelloAlphaValue =
        FloatValue("JelloEndAlphaPercent", 0.4f, 0f, 1f)
    private val jelloWidthValue =
        FloatValue("JelloCircleWidth", 3f, 0.01f, 5f)
    private val jelloGradientHeightValue =
        FloatValue("JelloGradientHeight", 3f, 1f, 8f)
    private val jelloFadeSpeedValue =
        FloatValue("JelloFadeSpeed", 0.1f, 0.01f, 0.5f)
    private val fakeSharpValue = BoolValue("FakeSharp", true)
    private val circleValue= BoolValue("Circle",true)
    private val circleRed = IntegerValue("CircleRed", 255, 0, 255)
    private val circleGreen = IntegerValue("CircleGreen", 255, 0, 255)
    private val circleBlue = IntegerValue("CircleBlue", 255, 0, 255)
    private val circleAlpha = IntegerValue("CircleAlpha", 255, 0, 255)
    private val circleAccuracy = IntegerValue("CircleAccuracy", 15, 0, 60)
    /**
     * MODULE
     */

    private val switchTimer = MSTimer()
    // Target
    var target: EntityLivingBase? = null
    var currentTarget: EntityLivingBase? = null
    private var hitable = false

    private val prevTargetEntities = mutableListOf<Int>()
    private var lastTarget: EntityLivingBase? = null
    private var direction = 1.0
    private var yPos = 0.0
    private var progress: Double = 0.0
    private var lastMS = System.currentTimeMillis()
    private var lastDeltaMS = 0L
    private var al = 0f
    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L
    // Fake block status
    private var bb: AxisAlignedBB? = null
    private var entity: EntityLivingBase? = null
    var blockingStatus = false
    private var espAnimation = 0.0
    private var syncEntity: EntityLivingBase? = null

    var range = 3.7F

    companion object {
        @JvmStatic
        var killCounts = 0
    }

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.player ?: return
        mc.world ?: return

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        lastTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (this.stopSprintAir.get()) {
            if (mc.player!!.onGround) {
                this.keepSprintValue.set(true)
            } else {
                this.keepSprintValue.set(false)
            }
        }
        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlock
            if (autoBlockValue.get().equals("Right", true))
                Robot().mousePress(InputEvent.BUTTON3_DOWN_MASK)
            else if (!autoBlockValue.get().equals("off",true) && delayedBlockValue.get()  && canBlock())
                startBlocking(currentTarget!!, interactAutoBlockValue.get())

            return

        }
        if (event.eventState == EventState.PRE) {
            update()
            val strafeFix = Pride.moduleManager[StrafeFix::class.java] as StrafeFix
            strafeFix.applyForceStrafe(
                rotationStrafeValue.get() == "Silent",
                rotationStrafeValue.get() != "Off" && rotations.get() != "None"
            )

        }

        if (rotationStrafeValue.get().equals("Off", true))
            update()
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.get().equals("Off", true))
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().toLowerCase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = sqrt(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = cos((yaw * Math.PI / 180F).toFloat())

                        val player = mc.player!!

                        player.motionX += strafe * yawCos - forward * yawSin
                        player.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
                "silent" -> {
                    update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
                "smart" ->{
                    if (RotationUtils.getRotationDifference(target) > 40.0) {
                        val (yaw) = RotationUtils.targetRotation ?: return
                        var strafe = event.strafe
                        var forward = event.forward
                        val friction = event.friction

                        var f = strafe * strafe + forward * forward

                        if (f >= 1.0E-4F) {
                            f = sqrt(f)

                            if (f < 1.0F)
                                f = 1.0F

                            f = friction / f
                            strafe *= f
                            forward *= f

                            val yawSin = sin((yaw * Math.PI / 180F).toFloat())
                            val yawCos = cos((yaw * Math.PI / 180F).toFloat())

                            val player = mc.player!!

                            player.motionX += strafe * yawCos - forward * yawSin
                            player.motionZ += forward * yawCos + strafe * yawSin
                        }
                        if (!mc.player.onGround){
                            mc.player.isSprinting = false
                        }
                        event.cancelEvent()
                    } else {
                        update()

                        RotationUtils.targetRotation.applyStrafeToPlayer(event)
                        event.cancelEvent()
                    }
                }
            }
        }
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && ((mc.currentScreen is GuiContainer) ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())))
            return
        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }



        // Target
        currentTarget = target

        if (!targetModeValue.get().equals("Switch", ignoreCase = true) && isEnemy(currentTarget))
            target = currentTarget
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (markValue.get().equals("jello", ignoreCase = true)
        ) al = AnimationUtils.changer(
            al,
            if (target != null) jelloFadeSpeedValue.get() else -jelloFadeSpeedValue.get(),
            0f,
            colorAlphaValue.get() / 255.0f
        )
    }
    @EventTarget
    fun onMove(event: MoveEvent){
        if (airBypass.get()){
            if (mc.player!!.onGround){
                if (range != rangeValue.get()) range = rangeValue.get()
            } else {
                if (range != airRangeValue.get()) range = airRangeValue.get()
            }
        }
    }
    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (airBypass.get() && target != null){
            if (target!!.posY.toInt() > mc.player.posY.toInt()) {
                if(rotations.get() != "Down") rotationStrafeValue.set("Down")
            } else {
                if(rotations.get() != "Test") rotationStrafeValue.set("Test")
            }
        }

        if (!airBypass.get()) range = rangeValue.get()

        if (lightingValue.get()) {
            when (lightingModeValue.get().toLowerCase()) {
                "dead" -> {
                    if (target != null) {
                        lastTarget = if (lastTarget == null) {
                            target
                        } else {
                            if (lastTarget!!.health <= 0) {
                                mc.connection!!.handleSpawnGlobalEntity(SPacketSpawnGlobalEntity(EntityLightningBolt(mc.world,
                                    lastTarget!!.posX, lastTarget!!.posY, lastTarget!!.posZ, true)))
                                if (lightingSoundValue.get()) mc    .soundHandler.playSound(
                                    PositionedSoundRecord.getRecord(
                                        SoundEvent(ResourceLocation("entity.lightning.impact")), 0.5f, 1.0f))
                            } //ambient.weather.thunder
                            target
                        }
                    } else {
                        if (lastTarget != null && lastTarget!!.health <= 0) {
                            mc.connection!!.handleSpawnGlobalEntity(SPacketSpawnGlobalEntity(EntityLightningBolt(mc.world,
                                lastTarget!!.posX, lastTarget!!.posY, lastTarget!!.posZ, true)))
                            if (lightingSoundValue.get()) mc.soundHandler.playSound(
                                PositionedSoundRecord.getRecord(
                                    SoundEvent(ResourceLocation("entity.lightning.impact")), 0.5f, 1.0f))
                            lastTarget = target
                        }
                    }
                }

                "attack" -> {
                    mc.connection!!.handleSpawnGlobalEntity(SPacketSpawnGlobalEntity(EntityLightningBolt(mc.world,
                        target!!.posX, target!!.posY, target!!.posZ, true)))
                    if (lightingSoundValue.get()) mc.soundHandler.playSound(
                        PositionedSoundRecord.getRecord(
                            SoundEvent(ResourceLocation("entity.lightning.impact")), 0.5f, 1.0f))
                }
            }
        }

        if (syncEntity != null && syncEntity!!.isDead) {
            ++killCounts
            syncEntity = null
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && ((mc.currentScreen is GuiContainer) ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }


        if (target != null && currentTarget != null && (mc.player!!.getCooledAttackStrength(0.0F) >= cooldownValue.get())) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }



    private fun esp(entity : EntityLivingBase, partialTicks : Float, radius : Float) {
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        GLUtils.startSmooth()
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glLineWidth(1.0F)
        GL11.glBegin(3)
        val x: Double = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y: Double = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z: Double = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
        for (i in 0..360) {
            val rainbow = Color(Color.HSBtoRGB((mc.player!!.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
            GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
            GL11.glVertex3d(x + radius * cos(i * 6.283185307179586 / 45.0), y + espAnimation, z + radius * sin(i * 6.283185307179586 / 45.0))
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        GLUtils.endSmooth()
        GL11.glEnable(3553)
        GL11.glPopMatrix()
    }

    private fun drawESP(entity: EntityLivingBase, color: Int, e: Render3DEvent) {
        val x: Double =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.partialTicks.toDouble() - mc.renderManager.renderPosX
        val y: Double =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.partialTicks.toDouble() - mc.renderManager.renderPosY
        val z: Double =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.partialTicks.toDouble() - mc.renderManager.renderPosZ
        val radius = 0.15f
        val side = 4
        GL11.glPushMatrix()
        GL11.glTranslated(x, y + 2, z)
        GL11.glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)
        RenderUtils.glColor1(color)
        RenderUtils.enableSmoothLine(1.5F)
        val c = Cylinder()
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
        c.drawStyle = 100012
        RenderUtils.glColor(Color(80,255,80,200))
        c.draw(0F, radius, 0.3f, side, 1)
        c.drawStyle = 100012
        GL11.glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0f, 0.3f, side, 1)
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
        c.drawStyle = 100011
        GL11.glTranslated(0.0, 0.0, -0.3)
        RenderUtils.glColor1(color)
        c.draw(0F, radius, 0.3f, side, 1)
        c.drawStyle = 100011
        GL11.glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0F, 0.3f, side, 1)
        RenderUtils.disableSmoothLine()
        GL11.glPopMatrix()
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        fun post3D() {
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1f, 1f, 1f, 1f)
        }

        fun drawCircle(
            x: Double,
            y: Double,
            z: Double,
            width: Float,
            radius: Double,
            red: Float,
            green: Float,
            blue: Float,
            alp: Float
        ) {
            GL11.glLineWidth(width)
            GL11.glBegin(GL11.GL_LINE_LOOP)
            GL11.glColor4f(red, green, blue, alp)
            var i = 0
            while (i <= 360) {
                val posX = x - Math.sin(i * Math.PI / 180) * radius
                val posZ = z + Math.cos(i * Math.PI / 180) * radius
                GL11.glVertex3d(posX, y, posZ)
                i += 1
            }
            GL11.glEnd()
        }
        fun pre3D() {
            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glDisable(2884)
        }
        fun getColor(ent: EntityLivingBase?): Color? {
            val counter = intArrayOf(0)
            if (ent is EntityLivingBase) {
                val entityLivingBase = ent

                if (colorModeValue.get().equals("Health", ignoreCase = true)) return BlendUtils.getHealthColor(
                    entityLivingBase.health,
                    entityLivingBase.maxHealth
                )
                if (colorTeam.get()) {

                    val chars = entityLivingBase.displayName!!.formattedText.toCharArray()
                    var color = Int.MAX_VALUE
                    for (i in chars.indices) {
                        if (chars[i] != '§' || i + 1 >= chars.size) continue
                        val index = getColorIndex(chars[i + 1])
                        if (index < 0 || index > 15) continue
                        color = ColorUtils.hexColors[index]
                        break
                    }
                    return Color(color)
                }
            }
            return when (colorModeValue.get()) {

                "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                "Rainbow" -> ColorUtils.hslRainbow(counter[0] * 100 + 1, indexOffset = 100 * 1500)

                "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
                "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
                else -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            }
        }
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.player!!.lastTickPosX + (mc.player!!.posX - mc.player!!.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.player!!.lastTickPosY + (mc.player!!.posY - mc.player!!.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.player!!.lastTickPosZ + (mc.player!!.posZ - mc.player!!.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(1F)
            GL11.glColor4f(circleRed.get().toFloat() / 255.0F, circleGreen.get().toFloat() / 255.0F, circleBlue.get().toFloat() / 255.0F, circleAlpha.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 61 - circleAccuracy.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * range, (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }
            GL11.glVertex2f(cos(360 * Math.PI / 180.0).toFloat() * range, (sin(360 * Math.PI / 180.0).toFloat() * rangeValue.get()))

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && ((mc.currentScreen is GuiContainer) ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        when (markValue.get().toLowerCase()) {
            "liquid" -> {
                RenderUtils.drawPlatform(target!!, if (target!!.hurtTime <= 0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170))
            }
            "plat" -> RenderUtils.drawPlatform(
                target!!,
                if (hitable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70)
            )
            "block" -> {
                val bb = target!!.entityBoundingBox
                target!!.entityBoundingBox = bb.expand(0.2, 0.2, 0.2)
                RenderUtils.drawEntityBox(target!!, if (target!!.hurtTime <= 0) Color.GREEN else Color.RED, true)
                target!!.entityBoundingBox = bb
            }
            "red" -> {
                RenderUtils.drawPlatform(target!!, if (target!!.hurtTime <= 0) Color(255, 255, 255, 255) else Color(124, 215, 255, 255))
            }
            "sims" -> {
                val radius = 0.15f
                val side = 4
                GL11.glPushMatrix()
                GL11.glTranslated(
                    target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX,
                    (target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + target!!.height * 1.1,
                    target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                )
                GL11.glRotatef(-target!!.width, 0.0f, 1.0f, 0.0f)
                GL11.glRotatef((mc.player!!.ticksExisted + mc.timer.renderPartialTicks) * 5, 0f, 1f, 0f)
                RenderUtils.glColor(if (target!!.hurtTime <= 0) Color(80, 255, 80) else Color(255, 0, 0))
                RenderUtils.enableSmoothLine(1.5F)
                val c = Cylinder()
                GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
                c.draw(0F, radius, 0.3f, side, 1)
                c.drawStyle = 100012
                GL11.glTranslated(0.0, 0.0, 0.3)
                c.draw(radius, 0f, 0.3f, side, 1)
                GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
                GL11.glTranslated(0.0, 0.0, -0.3)
                c.draw(0F, radius, 0.3f, side, 1)
                GL11.glTranslated(0.0, 0.0, 0.3)
                c.draw(radius, 0F, 0.3f, side, 1)
                RenderUtils.disableSmoothLine()
                GL11.glPopMatrix()
            }
            "fdp" -> {
                val drawTime = (System.currentTimeMillis() % 1500).toInt()
                val drawMode = drawTime > 750
                var drawPercent = drawTime / 750.0
                //true when goes up
                if (!drawMode) {
                    drawPercent = 1 - drawPercent
                } else {
                    drawPercent -= 1
                }
                drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                GL11.glEnable(2848)
                GL11.glEnable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3042)
                GL11.glBlendFunc(770, 771)
                GL11.glHint(3154, 4354)
                GL11.glHint(3155, 4354)
                GL11.glHint(3153, 4354)
                GL11.glDisable(2929)
                GL11.glDepthMask(false)

                val bb = target!!.entityBoundingBox
                val radius = (bb.maxX - bb.minX) + 0.3
                val height = bb.maxY - bb.minY
                val x = target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y = (target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                val z = target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                GL11.glLineWidth((radius * 5f).toFloat())
                GL11.glBegin(3)
                for (i in 0..360) {
                    val rainbow = Color(Color.HSBtoRGB((mc.player!!.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    GL11.glVertex3d(x + radius * cos(i * 6.283185307179586 / 45.0), y, z + radius * sin(i * 6.283185307179586 / 45.0))
                }
                GL11.glEnd()

                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                GL11.glDisable(2848)
                GL11.glDisable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            }
            "jello" -> {
                val lastY: Double = yPos
                fun easeInOutQuart(x: Double): Double {
                    return if (x < 0.5) 8 * x * x * x * x else 1 - Math.pow(-2 * x + 2, 4.0) / 2
                }
                if (al > 0f) {
                    if (System.currentTimeMillis() - lastMS >= 1000L) {
                        direction = -direction
                        lastMS = System.currentTimeMillis()
                    }
                    val weird: Long =
                        if (direction > 0) System.currentTimeMillis() - lastMS else 1000L - (System.currentTimeMillis() - lastMS)
                    progress = weird.toDouble() / 1000.0
                    lastDeltaMS = System.currentTimeMillis() - lastMS
                } else { // keep the progress
                    lastMS = System.currentTimeMillis() - lastDeltaMS
                }

                if (target != null) {
                    entity = target
                    bb = entity!!.entityBoundingBox
                }

                if (bb == null || entity == null) return

                val radius: Double = bb!!.maxX - bb!!.minX
                val height: Double = bb!!.maxY - bb!!.minY
                val posX: Double =
                    entity!!.lastTickPosX + (entity!!.posX - entity!!.lastTickPosX) * mc.timer.renderPartialTicks
                val posY: Double =
                    entity!!.lastTickPosY + (entity!!.posY - entity!!.lastTickPosY) * mc.timer.renderPartialTicks
                val posZ: Double =
                    entity!!.lastTickPosZ + (entity!!.posZ - entity!!.lastTickPosZ) * mc.timer.renderPartialTicks

                yPos = easeInOutQuart(progress) * height

                val deltaY: Double =
                    (if (direction > 0) yPos - lastY else lastY - yPos) * -direction * jelloGradientHeightValue.get()

                if (al <= 0 && entity != null) {
                    entity = null
                    return
                }

                val colour: Color? = getColor(entity)
                val r = colour!!.red / 255.0f
                val g = colour!!.green / 255.0f
                val b = colour!!.blue / 255.0f

                pre3D()
                //post circles
                //post circles
                GL11.glTranslated(
                    -mc.renderManager.viewerPosX,
                    -mc.renderManager.viewerPosY,
                    -mc.renderManager.viewerPosZ
                )

                GL11.glBegin(GL11.GL_QUAD_STRIP)

                for (i in 0..360) {
                    val calc = i * Math.PI / 180
                    val posX2 = posX - Math.sin(calc) * radius
                    val posZ2 = posZ + Math.cos(calc) * radius
                    GL11.glColor4f(r, g, b, 0f)
                    GL11.glVertex3d(posX2, posY + yPos + deltaY, posZ2)
                    GL11.glColor4f(r, g, b, al * jelloAlphaValue.get())
                    GL11.glVertex3d(posX2, posY + yPos, posZ2)
                }

                GL11.glEnd()

                drawCircle(posX, posY + yPos, posZ, jelloWidthValue.get(), radius, r, g, b, al)

                post3D()
//                val drawTime = (System.currentTimeMillis() % 2000).toInt()
//                val drawMode = drawTime > 1000
//                var drawPercent = drawTime / 1000.0
//
//                //true when goes up
//                if (!drawMode) {
//                    drawPercent = 1 - drawPercent
//                } else {
//                    drawPercent -= 1
//                }
//                drawPercent = EaseUtils.easeInOutQuad(drawPercent)
//                val points = mutableListOf<WVec3>()
//                val bb = target!!.entityBoundingBox
//                val radius = bb.maxX - bb.minX
//                val height = bb.maxY - bb.minY
//                val posX = target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * mc.timer.renderPartialTicks
//                var posY = target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * mc.timer.renderPartialTicks
//
//                if (drawMode) {
//                    posY -= 0.5
//                } else {
//                    posY += 0.5
//                }
//                val posZ = target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * mc.timer.renderPartialTicks
//                for (i in 0..360 step 7) {
//                    points.add(WVec3(posX - sin(i * Math.PI / 180F) * radius, posY + height * drawPercent, posZ + cos(i * Math.PI / 180F) * radius))
//                }
//                points.add(points[0])
//                //draw
//                mc.entityRenderer.disableLightmap()
//                GL11.glPushMatrix()
//                GL11.glDisable(GL11.GL_TEXTURE_2D)
//                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
//                GL11.glEnable(GL11.GL_LINE_SMOOTH)
//                GL11.glEnable(GL11.GL_BLEND)
//                GL11.glDisable(GL11.GL_DEPTH_TEST)
//                GL11.glBegin(GL11.GL_LINE_STRIP)
//                val baseMove = (if (drawPercent > 0.5) {
//                    1 - drawPercent
//                } else {
//                    drawPercent
//                }) * 2
//                val min = (height / 60) * 20 * (1 - baseMove) * (if (drawMode) {
//                    -1
//                } else {
//                    1
//                })
//                for (i in 0..20) {
//                    var moveFace = (height / 60F) * i * baseMove
//                    if (drawMode) {
//                        moveFace = -moveFace
//                    }
//                    val firstPoint = points[0]
//                    GL11.glVertex3d(firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY, firstPoint.zCoord - mc.renderManager.viewerPosZ)
//                    GL11.glColor4f(1F, 1F, 1F, 0.7F * (i / 20F))
//                    for (vec3 in points) {
//                        GL11.glVertex3d(
//                            vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
//                            vec3.zCoord - mc.renderManager.viewerPosZ
//                        )
//                    }
//                    GL11.glColor4f(0F, 0F, 0F, 0F)
//                }
//                GL11.glEnd()
//                GL11.glEnable(GL11.GL_DEPTH_TEST)
//                GL11.glDisable(GL11.GL_LINE_SMOOTH)
//                GL11.glDisable(GL11.GL_BLEND)
//                GL11.glEnable(GL11.GL_TEXTURE_2D)
//                GL11.glPopMatrix()
            }
        }

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
            currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
        }
    }
    /**
     * Handle entity move
     */
    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget)
            return

        updateHitable()
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return
        val player = mc.player ?: return
        val world = mc.world ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = true
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)
        val openInventory = aacValue.get() && mc.currentScreen is GuiContainer
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.connection!!.sendPacket(CPacketCloseWindow())

        // Check is not hitable or check failrate

        if (!hitable || failHit) {
            if (swing && (fakeSwingValue.get() || failHit))
                player.swingArm(EnumHand.MAIN_HAND)
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in world.loadedEntityList) {
                    val distance = player.getDistanceToEntityBox(entity)

                    if ((entity is EntityLivingBase) && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity)

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            if(targetModeValue.get().equals("Switch", true)){
                if(switchTimer.hasTimePassed(switchDelayValue.get().toLong())){
                    prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                    switchTimer.reset()
                }
            }else{
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
            }

            if (target == currentTarget)
                target = null
        }

        // Open inventory
        if (openInventory)
            mc.connection!!.sendPacket(createOpenInventoryPacket())
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        target = null

        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.get().equals("Switch", ignoreCase = true)

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        val world = mc.world!!
        val player = mc.player!!

        for (entity in world.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId)))
                continue

            val distance = player.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                targets.add(entity)
        }

        // Sort targets by priority
        when (priorityValue.get().toLowerCase()) {
            "distance" -> targets.sortBy { player.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "HurtResitanTime" -> targets.sortBy { it.hurtResistantTime } // Sort by armor
        }

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            return
        }

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        if ((entity is EntityLivingBase) && entity != null && (EntityUtils.targetDead || isAlive(entity)) && entity != mc.player) {
            if (!EntityUtils.targetInvisible && entity.isInvisible)
                return false

            if (EntityUtils.targetPlayer && (entity is EntityPlayer)) {
                val player = entity

                if (player.isSpectator || AntiBot.isBot(player))
                    return false

                if (player.isClientFriend() && !Pride.moduleManager[NoFriends::class.java].state)
                    return false

                val teams = Pride.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return EntityUtils.targetMobs && entity.isMob() || EntityUtils.targetAnimals && entity.isAnimal()
        }

        return false
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {
        // Stop blocking
        val player = mc.player!!

        if (!autoBlockPacketValue.get().equals("Packet",true)&&(mc.player!!.isActiveItemStackBlocking || blockingStatus)) {
            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM,
                BlockPos.ORIGIN, EnumFacing.DOWN))
            if (afterAttackValue.get()) blockingStatus = false
        }

        // Call attack event
        Pride.eventManager.callEvent(AttackEvent(entity))

        // Attack target
        mc.connection!!.sendPacket(CPacketUseEntity(entity))
        player.swingArm(EnumHand.MAIN_HAND)

        if (keepSprintValue.get()) {
            // Critical Effect
            if (player.fallDistance > 0F && !player.onGround && !player.isOnLadder &&
                !player.isInWater && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding)
                player.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(player.getHeldItem(EnumHand.MAIN_HAND), entity.creatureAttribute) > 0F)
                player.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != GameType.SPECTATOR)
                player.attackTargetEntityWithCurrentItem(entity)
        }

        // Extra critical effects
        val criticals = Pride.moduleManager[Criticals::class.java] as Criticals

        for (i in 0..2) {
            // Critical Effect
            if (player.fallDistance > 0F && !player.onGround && !player.isOnLadder && !player.isInWater && !player.isPotionActive(MobEffects.BLINDNESS) && player.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !player.isInWater && !player.isInLava && !player.isInWeb)
                player.onCriticalHit(target!!)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(player.getHeldItem(EnumHand.MAIN_HAND), target!!.creatureAttribute) > 0.0f || fakeSharpValue.get())
                player.onEnchantmentCritical(target!!)
        }

        // Start blocking after attack
        if (mc.player!!.isActiveItemStackBlocking || (!autoBlockValue.get().equals("off",true) && canBlock())) {
            if (delayedBlockValue.get())
                return


            startBlocking(entity, interactAutoBlockValue.get())
        }
        player.resetCooldown()
    }

    /**
     * Update killaura rotations to enemy
     */

    private fun updateRotations(entity: Entity): Boolean {
        var boundingBox = entity.entityBoundingBox
        if (rotations.get().equals("Test", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )
            val (_, rotation) = RotationUtils.lockView(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false
            //debug
            // ClientUtils.displayChatMessage((mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get()).toString())
            val limitedRotation = RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                rotation,
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat()
            )

            if (silentRotationValue.get()) {
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            } else {
                limitedRotation.toPlayer(mc.player!!)
                return true
            }
        }
        if (rotations.get().equals("test1", ignoreCase = true)){
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation =  RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.getNCPRotations(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }

        if (rotations.get().equals("test2", ignoreCase = true)){
            //用这个test2看看
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation =   RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }
        if (rotations.get().equals("HytRotation", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )
            val (_, rotation) = RotationUtils.lockView(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                rotation,
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }

        if (rotations.get().equals("Down", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )
            val (_, rotation) = RotationUtils.lockView2(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                rotation,
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }

        if (rotations.get().equals("New", ignoreCase = true)){
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation =  RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.getNewRotations(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }
        if (rotations.get().equals("LiquidBounce", ignoreCase = true)){
            //九用这个吧
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.player!!)

            return true
        }
        if (rotations.get().equals("GrimCenter", ignoreCase = true)) {

            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX - (mc.player!!.posX - mc.player!!.prevPosX)) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY - (mc.player!!.posY - mc.player!!.prevPosY)) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ - (mc.player!!.posZ - mc.player!!.prevPosZ)) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )
            val distance = if (mc.player!!.getDistanceToEntityBox(entity) > rangeValue.get()) {
                maxRange
            } else {
                rangeValue.get()
            }
            val (_, rotation) = RotationUtils.searchCenterNew(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                distance
            ) ?: return false

            val currentRotation = RotationUtils.serverRotation

            val limitedRotation = RotationUtils.limitAngleChange(currentRotation, rotation,
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, 15)
            else
                limitedRotation.toPlayer(mc.player)
            return true
        }
        if (rotations.get().equals("BackTrack", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )


            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                RotationUtils.OtherRotation(boundingBox,RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(),
                    mc.player!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),maxRange), (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get()) {
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            }else {
                limitedRotation.toPlayer(mc.player!!)
                return true
            }
        }
        return true
    }
    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if(hitableValue.get()){
            hitable = true
            return
        }
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeed.get() <= 0F) {
            hitable = true
            return
        }

        val reach = min(maxRange.toDouble(), mc.player!!.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object : RaycastUtils.EntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return (!livingRaycastValue.get() || (entity is EntityLivingBase && entity !is EntityArmorStand)) &&
                            (isEnemy(entity) || raycastIgnoredValue.get() || aacValue.get() && mc.world!!.getEntitiesWithinAABBExcludingEntity(entity, entity!!.entityBoundingBox).isNotEmpty())
                }

            })

            if (raycastValue.get() && raycastedEntity != null && raycastedEntity is EntityLivingBase
                && (Pride.moduleManager[NoFriends::class.java].state || !(raycastedEntity is EntityPlayer && raycastedEntity.isClientFriend())))
                currentTarget = raycastedEntity

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockValue.equals("Range") && mc.player!!.getDistanceToEntityBox(interactEntity)> BlockRangeValue.get()) {
            return
        }

        if (blockingStatus) {
            return
        }

        if (interact) {
            mc.connection!!.sendPacket(CPacketUseEntity(interactEntity, EnumHand.MAIN_HAND, interactEntity.positionVector))
            mc.connection!!.sendPacket(CPacketUseEntity(interactEntity, EnumHand.MAIN_HAND))
        }
        if(autoBlockPacketValue.get().equals("UseItem", true)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
        }
        if(autoBlockPacketValue.get().equals("GameSettings", true)) {
            mc.gameSettings.keyBindUseItem.pressed = true
        }
        if(autoBlockPacketValue.get().equals("Mouse", true)) {
            Robot().mousePress(InputEvent.BUTTON3_DOWN_MASK)
        }
        if(autoBlockPacketValue.get().equals("Packet", true)) {

            if (vanillamode.get().equals("TryUseItem", true)){

                mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
            }
            if (vanillamode.get().equals("UseItem", true)){


                mc.connection!!.sendPacket(createUseItemPacket(mc.player!!.inventory.getCurrentItem(), EnumHand.MAIN_HAND))
                mc.connection!!.sendPacket(createUseItemPacket(mc.player!!.inventory.getCurrentItem(), EnumHand.OFF_HAND))
            }
            if (vanillamode.get().equals("OldC08", true)){

                mc.connection!!.sendPacket(
                    CPacketPlayerTryUseItemOnBlock(
                    BlockPos(
                        -0.5534147541,
                        -0.5534147541,
                        -0.5534147541
                    ), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f
                )
                )
            }

        }
        if (vanillamode.get().equals("CPacketPlayerBlockPlacement", true)){
            mc.connection!!.sendPacket(CPacketPlayerTryUseItem(
                EnumHand.OFF_HAND)
            )
            mc.connection!!.sendPacket(CPacketPlayerTryUseItem(
                EnumHand.OFF_HAND)
            )
//                        mc.connection!!.sendPacket(classProvider.createCPacketPlayerBlockPlacement(mc.player!!.inventory.getCurrentItemInHand()))
        }
        blockingStatus = true

    }


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {

            if(autoBlockPacketValue.get().equals("Packet", true)) {
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, if (isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN, EnumFacing.DOWN))
            }
            if(autoBlockPacketValue.get().equals("UseItem", true)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            }

            if(autoBlockPacketValue.get().equals("GameSettings", true)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            }
            if(autoBlockPacketValue.get().equals("Mouse", true)) {
                Robot().mouseRelease(InputEvent.BUTTON3_DOWN_MASK)
            }
            if(autoBlockPacketValue.get().equals("Packet", true)) {
                mc.connection!!.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                    )
                )
            }

            blockingStatus = false
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        op.wawa.utils.PacketUtils.cancelC08(event, packet)
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        inline get() = mc.player!!.isSpectator || !isAlive(mc.player!!)
                || Pride.moduleManager[Blink::class.java].state || Pride.moduleManager[FreeCam::class.java].state

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 5


    /**
     * Check if player is able to block
     */
//    private val canBlock: Boolean
//        inline get() = mc.player!!.heldItem != null && classProvider.isItemSword(mc.player!!.heldItem!!.item)
    private fun canBlock(): Boolean {
        return if(mc.player!!.getHeldItem(EnumHand.MAIN_HAND)!!.item is ItemSword){
            if(autoBlockFacing.get()&&(target!!.getDistanceToEntityBox(mc.player!!)<maxRange)){
                target!!.rayTrace(maxRange.toDouble(),1F)!!.typeOfHit != RayTraceResult.Type.MISS
            }else{
                true
            }
        }else{
            false
        }
    }

    /**
     * Range
     */

    private val maxRange: Float
        get() = max(range, throughWallsRangeValue.get())

    private fun getRange(entity: Entity) =
        (if (mc.player!!.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) range else range) - if (mc.player!!.isSprinting) rangeSprintReducementValue.get() else 0F

    /**
     * HUD Tag
     */
    override val tag: String
        get() = targetModeValue.get()

    val isBlockingChestAura: Boolean
        get() = state && target != null
}