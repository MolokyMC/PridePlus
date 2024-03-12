package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.Pride.CLIENT_NAME
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "AutoGG", description = "Send chat like GG after winning a game.", category = ModuleCategory.MISC)
class AutoGG : Module() {
    private val delayValue = IntegerValue("Delay", 1000, 100, 5000)
    private val ggValue = BoolValue("SendGG", true)
    private val ggMessageValue = TextValue("GGMessage", "[${CLIENT_NAME}] GG!")

    private var winverify = false
    private var gamestarted = false
    private var winning = false
    private var sWStart1 = false
    private var sWStart2 = false
    private val timer = MSTimer()

    private fun gg() {
        Pride.hud.addNotification(Notification("AutoGG", "You won the game! GG!", NotifyType.SUCCESS, 4000, 700))
        if (ggValue.get()) Pride.hud.addNotification(
            Notification(
                "AutoGG",
                "Sent",
                NotifyType.SUCCESS,
                2000,
                400
            )
        )
        if (ggValue.get()) mc.player!!.sendChatMessage(ggMessageValue.get())
        stateReset()
    }

    private fun total() {
        stateReset()
    }

    private fun gameend() {
        stateReset()
    }

    private fun stateReset() {
        timer.reset()
        winverify = false
        gamestarted = false
        winning = false
        sWStart1 = false
        sWStart2 = false
    }

    override fun onEnable() {
        stateReset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        stateReset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is SPacketChat) {
            val message = packet.chatComponent.unformattedText
            if (message.contains("游戏开始 ...") && message.startsWith("起床战争") && !message.contains(":")) gamestarted =
                true
            if ((message.contains("你现在是观察者状态. 按E打开菜单.")) || (message.contains("恭喜") && message.startsWith(
                    "起床战争"
                )) || (message.startsWith("[起床战争]") && message.contains("赢得了游戏"))
            ) gameend()
            if (message.contains("开始倒计时: 1 秒")) sWStart2 = true
        }
        if (packet is SPacketTitle) {
            val title = (packet.message ?: return).unformattedText
            if (title.contains("恭喜") || title.contains("你的队伍获胜了") || title.contains("VICTORY")) winning = true
            if (title.contains("战斗开始")) sWStart1 = true
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(delayValue.get().toLong())) return
        if (winning) gg()
        if ((sWStart1 && sWStart2) || gamestarted) {
            total()
        }

    }

}