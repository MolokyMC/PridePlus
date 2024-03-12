/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.server.SPacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "AntiKnockback", description = "Less or Cancel your Knockback", category = ModuleCategory.COMBAT)
object AntiKnockback : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.antikbs", AntiKBMode::class.java)
        .map { it.newInstance() as AntiKBMode }
        .sortedBy { it.modeName }

    private val mode: AntiKBMode
        get() = modes.find { modeValue.get() == it.modeName } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "GrimAC") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val OnlyMove = BoolValue("OnlyMove", true)
    val OnlyGround = BoolValue("OnlyGround", false)
    val horizontalValue = FloatValue("Horizontal", 0f, -2f, 2f).displayable { modeValue.equals("Simple") || modeValue.equals("Tick") }
    val verticalValue = FloatValue("Vertical", 0f, -2f, 2f).displayable { modeValue.equals("Simple") || modeValue.equals("Tick") }
    val chanceValue = IntegerValue("Chance", 100, 0, 100).displayable { modeValue.equals("Simple") }
    val sendC03Value = BoolValue("Grim-SendC03", true).displayable { modeValue.get() == "GrimAC" }
    val breakValue = BoolValue("Grim-BreakBlock", true).displayable { modeValue.get() == "GrimAC" }
    val c0fTestValue = BoolValue("Grim-C0FTest", false).displayable { modeValue.get() == "GrimAC" }
    val onlyCombatValue = BoolValue("OnlyCombat", false)
    // private val onlyHitVelocityValue = BoolValue("OnlyHitVelocity",false)
    private val noFireValue = BoolValue("noFire", false)

    private val overrideDirectionValue = ListValue("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
    private val overrideDirectionYawValue = FloatValue("OverrideDirectionYaw", 0F, -180F, 180F)
        .displayable { !overrideDirectionValue.equals("None") }

    val velocityTimer = MSTimer()
    var wasTimer = false
    var velocityInput = false
    var velocityTick = 0

    var antiDesync = false

    var needReset = true

    override fun onEnable() {
        antiDesync = false
        needReset = true
        mode.onEnable()
    }

    override fun onDisable() {
        antiDesync = false
        mc.player.capabilities.isFlying = false
        mc.player.capabilities.flySpeed = 0.05f
        mc.player.noClip = false

        (mc.timer as IMixinTimer).timerSpeed = 1F
        mc.player.speedInAir = 0.02F

        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return

        mode.onUpdate(event)
        if (wasTimer) {
            (mc.timer as IMixinTimer).timerSpeed = 1f
            wasTimer = false
        }
        if(velocityInput) {
            velocityTick++
        }else velocityTick = 0

        if (mc.player.isInWater || mc.player.isInLava || mc.player.isInWeb) {
            return
        }

        if (onlyCombatValue.get() && !Pride.combatManager.inCombat) {
            return
        }
        if (noFireValue.get() && mc.player.isBurning) return
        mode.onVelocity(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onMotion(event)
    }
    
    @EventTarget
    fun onAttack(event: AttackEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onAttack(event)
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent){
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onStrafe(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onPacket(event)
        if (onlyCombatValue.get() && !Pride.combatManager.inCombat) {
            return
        }

        val packet = event.packet
        if (packet is SPacketEntityVelocity) {
            if (mc.player == null || (mc.world?.getEntityByID(packet.entityID) ?: return) != mc.player) {
                return
            }
            // if(onlyHitVelocityValue.get() && packet.motionY<400.0) return
            if (noFireValue.get() && mc.player.isBurning) return
            velocityTimer.reset()
            velocityTick = 0

            if (!overrideDirectionValue.equals("None")) {
                val yaw = Math.toRadians(
                    if (overrideDirectionValue.get() == "Hard") {
                        overrideDirectionYawValue.get()
                    } else {
                        mc.player.rotationYaw + overrideDirectionYawValue.get() + 90
                    }.toDouble()
                )
                val dist = sqrt((packet.motionX * packet.motionX + packet.motionZ * packet.motionZ).toDouble())
                val x = cos(yaw) * dist
                val z = sin(yaw) * dist
                packet.motionX = x.toInt()
                packet.motionZ = z.toInt()
            }

            mode.onVelocityPacket(event)
        }

    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onWorld(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onJump(event)
    }
    @EventTarget
    fun onTick(event: TickEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onTick(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        if ((OnlyMove.get() && !MovementUtils.isMoving) || (OnlyGround.get() && !mc.player.onGround))
            return
        mode.onStep(event)
    }
    override val tag: String
        get() = if (modeValue.get() == "Simple")
            "${(horizontalValue.get() * 100).toInt()}% ${(verticalValue.get() * 100).toInt()}% ${chanceValue.get()}%"
        else
            modeValue.get()

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
