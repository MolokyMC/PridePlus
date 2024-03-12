 package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.InfosUtils.Recorder
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.SPacketChat
import java.io.File

 @ModuleInfo(name = "AutoL", description = "AutoL", category = ModuleCategory.COMBAT)
class AutoL : Module() {
    private val enablement = BoolValue ("EnableAutoL", true)
    private val delayValue = IntegerValue("ChatDelay",3000,2400,6000)
    private val mode = ListValue("Mode", arrayOf("WithWords","RawWord","Clear","Custom","HeyGuy"),"Clear")
    private val waterMark = BoolValue ("WaterMark", true)
    private val enableHYTAtall = BoolValue ("Prefix@", true)
    private val textValue = TextValue("Text", "ExampleChat")
    private val chatTotalKill = BoolValue("ChatTotalKill",false)
    private val suffixTextBeforeRecord = TextValue("TextBeforeKillRecord","我已经击杀了")
    private val suffixTextAfterRecord = TextValue("TextAfterKillRecord","人!")
    private val showNotification = BoolValue("ShowNotificationsOnKill",false)

    // Target
    private val lastAttackTimer = MSTimer()
    var target: Entity? = null
    private var kill = 0
    private var tempkill = 0
    private var text = ""
    private var inCombat = false
    private val attackedEntityList = mutableListOf<Entity>()
    private val insultFile = File(Pride.fileManager.dir, "filter.json")
    private var insultWords = mutableListOf<String>()
    private val ms = MSTimer()
    private val delay = MSTimer()

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if ((target is Entity) && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }
        }
        lastAttackTimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        attackedEntityList.filter { it.isDead }.forEach {
            playerDeathEvent(it.name!!)
            attackedEntityList.remove(it)
        }

        inCombat = false

        if (!lastAttackTimer.hasTimePassed(1000)) {
            inCombat = true
            return
        }

        if (target != null) {
            if (mc.player!!.getDistanceToEntityBox(target!!) > 7 || !inCombat || target!!.isDead) {
                target = null
            } else {
                inCombat = true
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is SPacketChat) {
            val chat = packet.chatComponent.unformattedText
            if (chat.contains("起床战争") && chat.contains(">>") && chat.contains("游戏开始")) {
                attackedEntityList.clear()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        inCombat = false
        target = null
        attackedEntityList.clear()
    }

    private fun showNotifications (number: Int) {
        if (showNotification.get()) {
            Pride.hud.addNotification(
                Notification(
                    (if (number > 1) "$number Kills!" else "Kill!"),
                    ("You killed $number ${if (number > 1) "players" else "player"}"),
                    NotifyType.INFO
                )
            )
        }
    }
    private fun playerDeathEvent (name: String) {
        kill++
        Recorder.killCounts++
        tempkill++
        if (delay.hasTimePassed(delayValue.get().toLong())) {
            playerChat(name)
            delay.reset()
        }
        if (ms.hasTimePassed(5000)) {
            showNotifications(tempkill)
            tempkill = 0
            ms.reset()
        }
        if (!inCombat && tempkill != 0 && ms.hasTimePassed(5000)) {
            showNotifications(tempkill)
            tempkill = 0
            ms.reset()

        }
    }

    private fun playerChat(name: String) {
        if (enablement.get()) {
            when (mode.get().toLowerCase()) {
                "custom" -> {
                    text = (textValue.get())
                    text = (text.replace("%name%",name))
                    text = ("$text ")
                }
                "clear" -> {
                    text = ("Ｌ $name")
                }
                "rawwords" -> {
                    text = (getRandomOne())
                }
                "withwords" -> {
                    text = ("Ｌ $name | " + getRandomOne())
                }
                "heyguy" -> {
                    val random = (Math.random() * 7).toInt()
                    when(random){
                        1 -> text = ("Ｌ $name | 嗨，我是风动，这是我的neibu神器，3000收neibu是我的秘密武器，花钱一分钟，赚钱两个月，不要告诉别人哦")
                        2 -> text = ("Ｌ $name | 嗨，我是Pro，这是我的neibu神器，200整30个conf是我的秘密武器，花钱一分钟，赚钱两年半，不要告诉别人哦")
                        3 -> text = ("Ｌ $name | 嗨，我是瓦瓦，这是我的pride+神器，grimvel是我的秘密武器，vel一分钟，死号两小时，不要告诉_RyF哦")
                        4 -> text = ("Ｌ $name | 嗨，我是_RyF,这是我的彩色字节，彩色字节是我的秘密武器，出击一分钟，殴打两小时，不要告诉瓦瓦哦")
                        5 -> text = ("Ｌ $name | 嗨，我是狼牙，这是我的混淆神器，NellyObf是我的秘密武器，花钱一秒钟，抽烟一辈子，不要告诉paimon哦")
                        6 -> text = ("Ｌ $name | 嗨，我是原批，这是我的启动神器，你说的对，但是原神启动是我的秘密武器，启动十分钟，充电五小时，不要告诉别人哦")
                        7 -> text = ("Ｌ $name | 嗨，我是执剑，这是我的圈钱神器，圈钱造谣是我的秘密武器，圈钱一秒钟，高兴一个月，不要告诉别人哦")
                    }
                }
            }
            replaceFilterWords()
            if (chatTotalKill.get()) text = ("$text | " + suffixTextBeforeRecord.get() + kill + suffixTextAfterRecord.get())
            if (waterMark.get()) text = ("[${Pride.CLIENT_NAME}] $text")
            if (enableHYTAtall.get()) text = ("@a$text")
            mc.player!!.sendChatMessage(text)
        }
    }

    private fun getRandomOne(): String {
        return insultWords[RandomUtils.nextInt(0, insultWords.size - 1)]
    }

    fun resetAttackedList() {
        attackedEntityList.clear()
    }

    private fun replaceFilterWords() {
        text = (text.replace("%L%","Ｌ"))
        text = (text.replace("%l%","Ｌ"))

    }


    override val tag: String
        get() = "Kills $kill"
}
