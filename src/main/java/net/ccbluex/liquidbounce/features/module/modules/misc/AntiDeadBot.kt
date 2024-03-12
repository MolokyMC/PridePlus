package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.server.SPacketChat
import java.util.regex.Pattern

/**
 * Skid or Made By WaWa
 * @date 2023/9/15 21:12
 * @author WaWa
 */
@ModuleInfo(name = "AntiDeadBot", category = ModuleCategory.MISC, description = "HYTGetName")
class AntiDeadBot : Module() {
    private val autoModeValue = BoolValue("AutoSwitch", false)
    private val mode = ListValue("Mode", arrayOf("Classic4v4", "BW32", "BW16", "Kit"), "classic4v4").displayable { autoModeValue.get() }
    private val logMode = ListValue("LogStyle", arrayOf("FDPClient","NullClient", "WaWa", "Old"), "Normal")
    private val msg = BoolValue("PrintLog", false)
    private val hideKillChatValue = BoolValue("HideKillChat", false)
    private val showMyKillDeathChatValue = BoolValue("ShowKillDeathChat", false).displayable { hideKillChatValue.get() }

    private var bots = 0
    override fun onDisable() {
        bots = 0
        clearAll()
        super.onDisable()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if ((packet is SPacketChat) && !packet.chatComponent.unformattedText.contains(":") && (packet.chatComponent.unformattedText.startsWith("起床战争") || packet.chatComponent.unformattedText.startsWith("[起床战争"))) {
            val chat = packet.chatComponent.unformattedText
            when (mode.get().toLowerCase()) {
                "classic4v4" -> {
                    val matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(chat)
                    val matcher2 = Pattern.compile("起床战争>> (.*?) (\\(((.*?)死了!))").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        if (name != "") {
                            botchange(name, 4988)
                            hideMsg(event)
                        }
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        if (name != "") {
                            botchange(name, 4988)
                            hideMsg(event)
                        }
                    }
                }

                "bw32" -> {
                    val matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(chat)
                    val matcher2 = Pattern.compile("起床战争 >> (.*?) (\\(((.*?)死了!))").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        if (name != "") {
                            botchange(name, 7400)
                            hideMsg(event)
                        }
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        if (name != "") {
                            botchange(name, 4988)
                            hideMsg(event)
                        }
                    }
                }

                "bw16" -> {
                    val matcher = Pattern.compile("击败了 (.*?)!").matcher(chat)
                    val matcher2 = Pattern.compile("玩家 (.*?)死了！").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        if (name != "") {
                            botchange(name, 10000)
                            hideMsg(event)
                        }
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        if (name != "") {
                            botchange(name, 10000)
                            hideMsg(event)
                        }
                    }
                }

                "kit" -> {
                    if (chat.startsWith("花雨庭 >>") && chat.contains("你的 coins 被修正为")) return
                    val matcher = Pattern.compile("击杀了(.*?) !").matcher(chat)
                    val matcher2 = Pattern.compile("花雨庭 >>(.*?) 被").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        botchange(name, 10000)
                        hideMsg(event)
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        botchange(name, 10000)
                        hideMsg(event)
                    }
                }
            }
        }

        if (packet is SPacketChat && autoModeValue.get()) {
            val chat = packet.chatComponent.unformattedText
            if (chat.startsWith("花雨庭 >>") && mode.get().toLowerCase() != "kit") {
                mode.set("Kit")
                ClientUtils.displayChatMessage("§7[§dAntiDeadBot§7] §f模式职业战争模式。")
            }
            if (chat.startsWith("起床战争>>") && mode.get().toLowerCase() != "classic4v4") {
                mode.set("Classic4v4")
                ClientUtils.displayChatMessage("§7[§dAntiDeadBot§7] §f切换为经典起床模式。")
            }
            if (chat.startsWith("起床战争 >>") && mode.get().toLowerCase() != "bw32") {
                mode.set("BW32")
                ClientUtils.displayChatMessage("§7[§dAntiDeadBot§7] §f切换为经验32模式。")
            }
            if (chat.startsWith("[起床战争]") && mode.get().toLowerCase() != "bw16") {
                mode.set("BW16")
                ClientUtils.displayChatMessage("§7[§dAntiDeadBot§7] §f切换为经验16模式")
            }
        }
    }

    private fun hideMsg(event: PacketEvent) {
        val packet = event.packet
        if ((packet is SPacketChat) && hideKillChatValue.get() && !(showMyKillDeathChatValue.get() && packet.chatComponent.unformattedText.contains(
                mc.player!!.displayNameString
            ))
        ) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        if (event?.worldClient == null) return
        clearAll()
    }

    private fun botchange(name: String, cooldown: Long) {
        bots++
        Pride.fileManager.friendsConfig.addFriend(name)
        if (msg.get()) {
            logString(name, "add")
        }
        Thread {
            try {
                Thread.sleep(cooldown)
                Pride.fileManager.friendsConfig.removeFriend(name)
                bots--
                if (msg.get()) {
                    logString(name, "remove")
                }
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }
        }.start()
    }

    private fun logString(name: String, mode: String) {
        ClientUtils.displayChatMessage(
            when (mode.toLowerCase()) {
                "add" -> {
                    when (logMode.get().toLowerCase()) {
                        "old" -> "§8[§c§l${Pride.CLIENT_NAME}提醒您§8] §d添加无敌人：§7$name"
                        "fdpclient" -> "§7[§cAntiBot§7] §fAdded §7$name§f due to it being a bot."
                        "nullclient" -> "§7[§cAntiBots§7] §fAdded a bot(§7$name§f)"
                        "wawa" -> "§6${Pride.CLIENT_NAME} §7=» §f玩家 §7$name §f狂暴死去"
                        else -> "§8[§c§l${Pride.CLIENT_NAME}提醒您§8] §d添加无敌人：§7$name"
                    }
                }
                "remove" -> {
                    when (logMode.get().toLowerCase()) {
                        "old" -> ("§8[§c§l${Pride.CLIENT_NAME}提醒您§8] §d删除无敌人：§7$name")
                        "fdpclient" -> ("§7[§cAntiBot§7] §fRemoved §7$name§f due to respawn.")
                        "nullclient" -> ("§7[§cAntiBots§7] §fRemoved a bot(§7$name§f)")
                        "wawa" -> "§6${Pride.CLIENT_NAME} §7=» §f玩家 §7$name §f炸裂归来"
                        else -> "§8[§c§l${Pride.CLIENT_NAME}提醒您§8] §d添加无敌人：§7$name"
                    }
                }
                else -> ""
            }
        )
    }


    private fun clearAll() {
        Pride.fileManager.friendsConfig.clearFriends()
    }

    override val tag: String
        get() = (mode.get() + ", Bots: " + bots)
}