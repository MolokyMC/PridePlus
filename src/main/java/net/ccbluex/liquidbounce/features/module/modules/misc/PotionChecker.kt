package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
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
            MobEffects.SPEED
            for (entity in mc.world!!.loadedEntityList) {
                if (!EntityUtils.isSelected(entity, false)) continue
                if (entity is EntityLivingBase && AntiBot.isBot(entity)) continue
                if (entity !is EntityLivingBase) continue

                check(entity)
            }
        }
    }

    override fun onDisable() {
        players = 0
    }
    override fun onEnable() {
        if(whilevalue.get()) return
        for (entity in mc.world!!.loadedEntityList){
            if (!EntityUtils.isSelected(entity, false)) continue
            if (entity is EntityLivingBase && AntiBot.isBot(entity)) continue
            if (entity !is EntityLivingBase) continue

            check(entity)
        }
    }
    fun check(entity: EntityLivingBase){
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
                                    val distance = mc.player!!.getDistanceToEntityBox(entity)
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
                                    val distance = mc.player!!.getDistanceToEntityBox(entity)
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
                                    val distance = mc.player!!.getDistanceToEntityBox(entity)
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
                                    val distance = mc.player!!.getDistanceToEntityBox(entity)
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
                                    val distance = mc.player!!.getDistanceToEntityBox(entity)
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