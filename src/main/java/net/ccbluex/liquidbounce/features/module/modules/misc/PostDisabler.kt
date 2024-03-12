/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoPot
import net.ccbluex.liquidbounce.features.module.modules.movement.Sneak
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayer

@ModuleInfo(name = "PostDisabler", category = ModuleCategory.MISC, description = "Post disabler for HYT GrimAC")
class PostDisabler : Module() {

    var timer = MSTimer()
    private val chestrvaule = BoolValue("ChestAura", true)
    private val sneakvaule = BoolValue("sneak", true)
    private val autopotvaule = BoolValue("autopot", true)
    private val debug = BoolValue("Debug",false)



    @EventTarget
    fun onPacket(event: PacketEvent) {
        val chestAura = Pride.moduleManager[ChestAura::class.java] as ChestAura
        val sneak = Pride.moduleManager[Sneak::class.java] as Sneak
        val autopot = Pride.moduleManager[AutoPot::class.java] as AutoPot
        val serverData = mc.currentServerData
        if ((chestrvaule.get() && chestAura.state)  || (sneakvaule.get() && sneak.state) || (autopot.state && autopotvaule.get())) {
            if (serverData != null) {
                val pingTime = serverData.pingToServer
                if (event.packet is CPacketPlayer){
                    if (timer.hasTimePassed(pingTime)) {
                        mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction())
                        if (debug.get()) {
                            ClientUtils.displayChatMessage("§b[§b${Pride.CLIENT_NAME}]§dsent")
                        }
                        timer.reset()
                    }
                }
            }
        }
    }
}