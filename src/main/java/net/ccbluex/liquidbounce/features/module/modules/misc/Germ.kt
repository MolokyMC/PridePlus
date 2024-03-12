package net.ccbluex.liquidbounce.features.module.modules.misc

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.germ.GermButton
import net.ccbluex.liquidbounce.features.module.modules.misc.germ.GermPage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraft.util.text.TextComponentString
import org.yaml.snakeyaml.Yaml

/**
 * Skid or Made By WaWa
 * @date 2023/9/9 21:12
 * @author WaWa
 */
//参考了MinClient
@ModuleInfo(name = "Germ", category = ModuleCategory.MISC, description = "萌芽引擎代替品 只供学习使用")
class Germ : Module() {
    fun onPacket(event: PacketEvent?) {
        val packet = event!!.packet
        if (packet is SPacketCustomPayload) {
            if (packet.channelName.equals("germplugin-netease")) {
                process(packet.bufferData)
            }
        }
    }

    fun process(byteBuf: ByteBuf) {
        MinecraftInstance.mc.player.sendMessage(TextComponentString("Germ >> germplugin-netease"))
        val intMax = 9999999
        val packetBuffer = PacketBuffer(byteBuf)
        if (packetBuffer.readInt() != 73) return
        val buffer = PacketBuffer(packetBuffer.copy())
        val string: String = buffer.readString(Short.MAX_VALUE.toInt())
        if (string.equals("gui", ignoreCase = true)) {
            val guiUuid: String = buffer.readString(Short.MAX_VALUE.toInt())
            val yml: String = buffer.readString(intMax)
            val yaml = Yaml()
            var map: Map<String, Any?>? = yaml.load(yml)
            if (map != null) map = map[guiUuid] as Map<String, Any?>?
            val buttons = ArrayList<GermButton>()
            if (map != null) {
                for (s in map.keys) {
                    if (s.equals("options", ignoreCase = true) || s.endsWith("_bg")) continue
                    var set = map[s] as Map<String, Any?>?
                    for (k in set!!.keys) {
                        if (!k.equals("scrollableParts", ignoreCase = true)) continue
                        set = set["scrollableParts"] as Map<String, Any?>?
                        for (uuid in set!!.keys) {
                            var objectMap = set[uuid] as Map<String, Any?>?
                            if (objectMap!!.containsKey("relativeParts")) {
                                objectMap = objectMap["relativeParts"] as Map<String, Any?>?
                                for (kk in objectMap!!.keys) {
                                    objectMap = objectMap[kk] as Map<String, Any?>?
                                    if (objectMap!!.containsKey("texts")) {
                                        val buttonText = (objectMap["texts"] as ArrayList<String?>?)!![0]
                                        buttons.add(GermButton("$s$$uuid$$kk", buttonText))
                                        break
                                    }
                                }
                            }
                        }
                        val sendBuffer =
                            PacketBuffer(Unpooled.buffer().writeInt(4).writeInt(0).writeInt(0)).writeString(guiUuid)
                                .writeString(guiUuid).writeString(guiUuid)
                        MinecraftInstance.mc.displayGuiScreen(GermPage(guiUuid, buttons))
                        MinecraftInstance.mc.connection!!.sendPacket(
                            CPacketCustomPayload(
                                "germmod-netease",
                                sendBuffer
                            )
                        )
                        return
                    }
                }
            }
        }
    }
}