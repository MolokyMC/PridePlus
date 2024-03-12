package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.utils.JammingUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer


@ModuleInfo(name = "Jamming", description = "woc, esu!", category = ModuleCategory.MISC)
class Jamming : Module() {

    private val text = TextValue("Text", "WTF, Im D1ck")
    private val name1 = TextValue("Name", "D1ck")
    private val ip = TextValue("IP", "127.0.0.1")
    private val port = TextValue("Port", "14438")


    @EventTarget
    fun onAttack(event: AttackEvent){
        if (event.targetEntity is EntityLivingBase && event.targetEntity is EntityPlayer){
            val entity = event.targetEntity
            JammingUtils.SendMsg(
                ip.get(),
                port.get(),
                text.get(),
                name1.get(),
                entity
            )
        }
    }

}