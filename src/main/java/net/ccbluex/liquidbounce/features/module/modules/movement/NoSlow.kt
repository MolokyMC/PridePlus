/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.login.client.CPacketEncryptionResponse
import net.minecraft.network.login.client.CPacketLoginStart
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.status.client.CPacketPing
import net.minecraft.network.status.client.CPacketServerQuery
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import op.wawa.utils.PacketUtils
import java.util.*

@ModuleInfo(
    name = "NoSlow", description = "Cancels slowness effects caused by soulsand and using items.",
    category = ModuleCategory.MOVEMENT
)
class NoSlow : Module() {

    private val modeValue = ListValue(
        "PacketMode",
        arrayOf("Faith", "GrimFix", "Grim", "NoPacket", "AAC", "AAC5","HYTLATEST", "Matrix", "Vulcan", "Custom","Grim AC"),
        "AntiCheat"
    )
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val customOnGround = BoolValue("CustomOnGround", false)
    private val customDelayValue = IntegerValue("CustomDelay", 60, 10, 200)
    private val blinkValue = IntegerValue("Blink Delay", 230, 10, 1000)

    // Soulsand
    val soulsandValue = BoolValue("Soulsand", false)

    val timer = MSTimer()
    private val Timer = MSTimer()
    private var pendingFlagApplyPacket = false
    private val msTimer = MSTimer()
    private var sendBuf = false
    private var packetBuf = LinkedList<Packet<INetHandlerPlayServer>>()
    private var nextTemp = false
    private var waitC03 = false
    private var lvZiQiaoing = false
    private var lastBlockingStat = false

    val killAura = Pride.moduleManager[KillAura::class.java] as KillAura


    fun isBlock(): Boolean {
        return mc.player.isActiveItemStackBlocking || killAura.blockingStatus
    }

    fun fuckKotline(value: Int): Boolean {
        return value == 1
    }

    private fun OnPre(event: MotionEvent): Boolean {
        return event.eventState == EventState.PRE
    }

    private fun OnPost(event: MotionEvent): Boolean {
        return event.eventState == EventState.POST
    }

    private val isBlocking: Boolean
        get() = (mc.player!!.isHandActive || (Pride.moduleManager[KillAura::class.java] as KillAura).blockingStatus) && mc.player!!.heldItemMainhand != null && mc.player!!.heldItemMainhand!!.item is ItemSword

    override fun onDisable() {
        Timer.reset()
        msTimer.reset()
        pendingFlagApplyPacket = false
        sendBuf = false
        packetBuf.clear()
        nextTemp = false
        waitC03 = false
    }

    private fun sendPacket(
        Event: MotionEvent,
        SendC07: Boolean,
        SendC08: Boolean,
        Delay: Boolean,
        DelayValue: Long,
        onGround: Boolean,
        Hypixel: Boolean = false
    ) {
        val aura = Pride.moduleManager[KillAura::class.java] as KillAura
        val digging = CPacketPlayerDigging(
            CPacketPlayerDigging.Action.RELEASE_USE_ITEM,
            BlockPos(-1, -1, -1),
            EnumFacing.DOWN
        )
        val blockPlace =
            CPacketPlayerTryUseItem(EnumHand.MAIN_HAND)
        val blockMent = CPacketPlayerTryUseItemOnBlock(
            BlockPos(-1, -1, -1),
            EnumFacing.DOWN,
            EnumHand.MAIN_HAND,
            0f,
            0f,
            0f
        )
        if (onGround && !mc.player!!.onGround) {
            return
        }

        if (SendC07 && OnPre(Event)) {
            if (Delay && Timer.hasTimePassed(DelayValue)) {
                mc.connection!!.sendPacket(digging)
            } else if (!Delay) {
                mc.connection!!.sendPacket(digging)
            }
        }
        if (SendC08 && OnPost(Event)) {
            if (Delay && Timer.hasTimePassed(DelayValue) && !Hypixel) {
                mc.connection!!.sendPacket(blockPlace)
                Timer.reset()
            } else if (!Delay && !Hypixel) {
                mc.connection!!.sendPacket(blockPlace)
            } else if (Hypixel) {
                mc.connection!!.sendPacket(blockMent)
            }
        }
    }
    override fun onEnable() {
        msTimer.reset()
        lvZiQiaoing = false
        packetBuf.clear()
        lastBlockingStat = false
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.player ?: return
        var test = fuckKotline(mc.player.ticksExisted and 1)
        val heldItem = thePlayer.heldItemMainhand

        if (!MovementUtils.isMoving) {
            return
        }

        when (modeValue.get().toLowerCase()) {
            "custom" -> {
                sendPacket(event, true, true, true, customDelayValue.get().toLong(), customOnGround.get())
            }
            "faith" -> {
                if (mc.player.heldItemMainhand.item is ItemSword && isBlocking) {
                    when(event.eventState) {
                        EventState.PRE -> {
                            mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem + 1))
                            mc.connection!!.sendPacket(CPacketCustomPayload("tesst", PacketBuffer(Unpooled.buffer())))
                            mc.connection!!.sendPacket(CPacketHeldItemChange(mc.player!!.inventory.currentItem))
                        }
                        EventState.POST -> {
                            PacketUtils.sendPacketC0F()
                            mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                }
            }

            "aac" -> {
                if (mc.player!!.ticksExisted % 3 == 0) {
                    sendPacket(event, true, false, false, 0, false)
                } else {
                    sendPacket(event, false, true, false, 0, false)
                }
            }
        }


    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (modeValue.equals("Matrix") || modeValue.equals("Vulcan") && nextTemp) {
            if ((packet is CPacketPlayerDigging || packet is CPacketPlayerTryUseItem) && isBlocking) {
                event.cancelEvent()
            }
            event.cancelEvent()
        } else if (packet is CPacketPlayer || packet is CPacketAnimation || packet is CPacketEntityAction || packet is CPacketUseEntity || packet is CPacketPlayerDigging || packet is CPacketPlayerTryUseItem) {
            if (modeValue.equals("Vulcan") && waitC03 && packet is CPacketPlayer) {
                waitC03 = false
                return
            }
            packetBuf.add(packet as Packet<INetHandlerPlayServer>)
        }
        if (mc.player == null || mc.world == null) return

        when (modeValue.get().toLowerCase()) {
            "hytlatest" -> {
                if (mc.player.heldItemMainhand.item is ItemFood || mc.player.heldItemMainhand.item is ItemPotion) {
                    if (packet is CPacketPlayerTryUseItem) {
                        lvZiQiaoing = true
                    } else if (lvZiQiaoing && (packet !is CPacketCustomPayload && packet !is CPacketChatMessage && packet !is C00Handshake && packet !is CPacketServerQuery && packet !is CPacketLoginStart && packet !is CPacketPing && packet !is CPacketEncryptionResponse && !packet.javaClass.simpleName.startsWith("s", ignoreCase = true))) {
                        if (packet is CPacketPlayerDigging && packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                            lvZiQiaoing = false
                            packetBuf.forEach { mc.connection!!.sendPacket(it) }
                            packetBuf.clear()
                        } else {
                            event.cancelEvent()
                            packetBuf.add(packet as Packet<INetHandlerPlayServer>)
                        }
                    }
                }
            }
        }

    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if ((modeValue.equals("Matrix") || modeValue.equals("Vulcan")) && (lastBlockingStat || isBlocking)) {
            if (msTimer.hasTimePassed(230) && nextTemp) {
                nextTemp = false
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos(-1, -1, -1),
                    EnumFacing.DOWN
                )
                if (packetBuf.isNotEmpty()) {
                    var canAttack = false
                    for (packet in packetBuf) {
                        if (packet is CPacketPlayer) {
                            canAttack = true
                        }
                        if (!((packet is CPacketUseEntity || packet is CPacketAnimation) && !canAttack)) {
                            PacketUtils.sendPacketNoEvent(packet)
                        }
                    }
                    packetBuf.clear()
                }
            }
            if (!nextTemp) {
                lastBlockingStat = isBlocking
                if (!isBlocking) {
                    return
                }
                nextTemp = true
                waitC03 = modeValue.equals("Vulcan")
                msTimer.reset()
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.player!!.heldItemMainhand.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when {
            item is ItemFood || item is ItemPotion || item is ItemBucketMilk -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }

            item is ItemSword -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }

            item is ItemBow -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }

            else -> 0.2F
        }
    }
    private val isEatting: Boolean
        get() = mc.player!!.isHandActive && mc.player!!.heldItemMainhand != null && (mc.player!!.heldItemMainhand.item is ItemFood) || (mc.player.heldItemMainhand.item is ItemPotion)


    override val tag: String
        get() = modeValue.get()

}
