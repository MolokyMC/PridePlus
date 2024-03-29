/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.client.CPacketResourcePackStatus
import net.minecraft.network.play.server.SPacketResourcePackSend
import java.net.URI
import java.net.URISyntaxException

@ModuleInfo(name = "ResourcePackSpoof", description = "Prevents servers from forcing you to download their resource pack.", category = ModuleCategory.MISC)
class ResourcePackSpoof : Module() {

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is SPacketResourcePackSend) {
            val packet = event.packet

            val url = packet.url
            val hash = packet.hash

            try {
                val scheme = URI(url).scheme
                val isLevelProtocol = "level" == scheme

                if ("http" != scheme && "https" != scheme && !isLevelProtocol)
                    throw URISyntaxException(url, "Wrong protocol")

                if (isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip")))
                    throw URISyntaxException(url, "Invalid levelstorage resourcepack path")

                mc.connection!!.sendPacket(
                    CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED)
                )
                mc.connection!!.sendPacket(CPacketResourcePackStatus(CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED))
            } catch (e: URISyntaxException) {
                ClientUtils.getLogger().error("Failed to handle resource pack", e)
                mc.connection!!.sendPacket(CPacketResourcePackStatus(CPacketResourcePackStatus.Action.FAILED_DOWNLOAD))
            }
        }
    }

}