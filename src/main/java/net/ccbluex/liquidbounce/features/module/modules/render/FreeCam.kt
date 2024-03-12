/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "FreeCam", description = "Allows you to move out of your body.", category = ModuleCategory.RENDER)
class FreeCam : Module() {
    private val speedValue = FloatValue("Speed", 0.8f, 0.1f, 2f)
    private val flyValue = BoolValue("Fly", true)
    private val noClipValue = BoolValue("NoClip", true)

    private var fakePlayer: EntityOtherPlayerMP? = null

    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0

    override fun onEnable() {
        val player = mc.player ?: return

        oldX = player.posX
        oldY = player.posY
        oldZ = player.posZ

        val playerMP = EntityOtherPlayerMP(mc.world!!, player.gameProfile)


        playerMP.rotationYawHead = player.rotationYawHead
        playerMP.renderYawOffset = player.renderYawOffset
        playerMP.rotationYawHead = player.rotationYawHead
        playerMP.copyLocationAndAnglesFrom(player)

        mc.world!!.addEntityToWorld(-1000, playerMP)

        if (noClipValue.get())
            player.noClip = true

        fakePlayer = playerMP
    }

    override fun onDisable() {
        val player = mc.player

        if (player == null || fakePlayer == null)
            return

        player.setPositionAndRotation(oldX, oldY, oldZ, player.rotationYaw, player.rotationPitch)

        mc.world!!.removeEntityFromWorld(fakePlayer!!.entityId)
        fakePlayer = null

        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player!!

        if (noClipValue.get())
            player.noClip = true

        player.fallDistance = 0.0f

        if (flyValue.get()) {
            val value = speedValue.get()

            player.motionY = 0.0
            player.motionX = 0.0
            player.motionZ = 0.0

            if (mc.gameSettings.keyBindJump.isKeyDown)
                player.motionY += value

            if (mc.gameSettings.keyBindSneak.isKeyDown)
                player.motionY -= value

            MovementUtils.strafe(value)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is CPacketPlayer || packet is CPacketEntityAction)
            event.cancelEvent()
    }
}