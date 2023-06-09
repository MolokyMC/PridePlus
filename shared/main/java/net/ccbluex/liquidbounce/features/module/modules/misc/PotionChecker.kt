package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

@ModuleInfo(name = "PotionChecker", description = "Custom.", category = ModuleCategory.MISC)
class PotionChecker : Module(){
    val whilevalue = BoolValue("While",true)
    val distancevalue = BoolValue("Distance",true)
    val potionvalue = ListValue("CheckPotion", arrayOf("Power","Speed","Jump","Regen","Haste"),"Power")
    var players = 0
    @EventTarget
    fun onUpdate(event : UpdateEvent){
        if(whilevalue.get()) {
            net.ccbluex.liquidbounce.api.minecraft.potion.PotionType.MOVE_SPEED
            for (entity in mc.theWorld!!.loadedEntityList) {
                if (!EntityUtils.isSelected(entity, false)) continue
                if (AntiBot.isBot(entity.asEntityLivingBase())) continue

                check(entity.asEntityLivingBase())
            }
        }
    }

    override fun onDisable() {
        players = 0
    }
    override fun onEnable() {
        if(whilevalue.get()) return
        for (entity in mc.theWorld!!.loadedEntityList){
            if (!EntityUtils.isSelected(entity, false)) continue
            if (AntiBot.isBot(entity.asEntityLivingBase())) continue

            check(entity.asEntityLivingBase())
        }
    }
    fun check(entity: IEntityLivingBase){
        val speed = Potion.getPotionById(1)
        val haste = Potion.getPotionById(3)
        val power = Potion.getPotionById(5)
        val jump = Potion.getPotionById(8)
        val regen = Potion.getPotionById(10)
        if (entity is EntityPlayer) {
            System.out.println("entity is EntityPlayer")
            val potions = entity.getActivePotionEffects()
            if (!potions.isEmpty()) {
                for (potion in potions) {
                    val name = potion.effectName
                    when(potionvalue.get().toLowerCase()){
                        "power"->{
                            if (potion.potion.equals(power)){
                                players++
                                if(distancevalue.get()){
                                    val distance = mc.thePlayer!!.getDistanceToEntity(entity)
                                    ClientUtils.displayChatMessage(entity.name+" Has →"+ name + "← Effect "+"| Distance: "+distance.toString())
                                }else {
                                    ClientUtils.displayChatMessage(entity.name + " Has →" + name + "← Effect")
                                }
                            }
                        }
                        "speed"->{
                            players++
                            if (potion.potion.equals(speed)){
                                if(distancevalue.get()){
                                    val distance = mc.thePlayer!!.getDistanceToEntity(entity)
                                    ClientUtils.displayChatMessage(entity.name+" Has →"+ name + "← Effect "+"| Distance: "+distance.toString())
                                }else {
                                    ClientUtils.displayChatMessage(entity.name + " Has →" + name + "← Effect")
                                }
                            }
                        }
                        "jump"->{
                            players++
                            if (potion.potion.equals(jump)){
                                if(distancevalue.get()){
                                    val distance = mc.thePlayer!!.getDistanceToEntity(entity)
                                    ClientUtils.displayChatMessage(entity.name+" Has →"+ name + "← Effect "+"| Distance: "+distance.toString())
                                }else {
                                    ClientUtils.displayChatMessage(entity.name + " Has →" + name + "← Effect")
                                }
                            }
                        }
                        "regen"->{
                            players++
                            if (potion.potion.equals(regen)){
                                if(distancevalue.get()){
                                    val distance = mc.thePlayer!!.getDistanceToEntity(entity)
                                    ClientUtils.displayChatMessage(entity.name+" Has →"+ name + "← Effect "+"| Distance: "+distance.toString())
                                }else {
                                    ClientUtils.displayChatMessage(entity.name + " Has →" + name + "← Effect")
                                }
                            }
                        }
                        "haste"->{
                            players++
                            if (potion.potion.equals(haste)){
                                if(distancevalue.get()){
                                    val distance = mc.thePlayer!!.getDistanceToEntity(entity)
                                    ClientUtils.displayChatMessage(entity.name+" Has →"+ name + "← Effect "+"| Distance: "+distance.toString())
                                }else {
                                    ClientUtils.displayChatMessage(entity.name + " Has →" + name + "← Effect")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    override val tag: String
        get() = players.toString()
}