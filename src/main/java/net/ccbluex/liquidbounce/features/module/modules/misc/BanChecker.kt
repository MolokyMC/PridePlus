package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.SPacketChat
import java.util.regex.Pattern

@ModuleInfo(name = "BanChecker", description = "For HuaYuTing", category = ModuleCategory.MISC)
class BanChecker : Module(){

    var ban = 0
    @EventTarget
    fun onPacket(event : PacketEvent){
        val packet = event.packet
        if(packet is SPacketChat){
            val matcher = Pattern.compile("玩家(.*?)在本局游戏中行为异常").matcher(packet.chatComponent.unformattedText)
            if(matcher.find()){
                ban ++
                val banname = matcher.group(1)
                Pride.hud.addNotification(Notification("BanChecker","$banname was banned. (banned:$ban)",NotifyType.INFO, animeTime = 1000))
                mc.player!!.sendChatMessage("@ $banname  哇哇哇你怎么被封了啊 这都有" + ban + "个人受到禁令了 ")
            }
        }
    }
}