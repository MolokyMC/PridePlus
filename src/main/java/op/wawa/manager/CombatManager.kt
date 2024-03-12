package op.wawa.manager

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase


class CombatManager : Listenable, MinecraftInstance() {
    var kill = 0
    var death = 0
    var inCombat=false
    private val lastAttackTimer=MSTimer()
    var target: EntityLivingBase? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.player == null) return
        MovementUtils.updateBlocksPerSecond()

        inCombat=false

        if(!lastAttackTimer.hasTimePassed(1000)){
            inCombat=true
            return
        }

        for (entity in mc.world!!.loadedEntityList) {
            if (entity is EntityLivingBase
                && entity.getDistanceToEntityBox(mc.player!!) < 7 && EntityUtils.isSelected(entity, true)) {
                inCombat = true
                break
            }
        }

        if(target!=null){
            if(mc.player!!.getDistanceToEntityBox(target!!)>7 || !inCombat){
                target=null
            }
            if (target!!.isDead){
                target = null
                kill++
            }
        }
        if (mc.player!!.isDead){
            death++
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent){
        if(event.targetEntity is EntityLivingBase && EntityUtils.isSelected(event.targetEntity,true)){
            target = event.targetEntity
        }
        lastAttackTimer.reset()

    }
    fun getNearByEntity(radius: Float): EntityLivingBase?{
        return try {
            mc.world!!.loadedEntityList
                .filter { mc.player!!.getDistanceToEntityBox(it)<radius&&EntityUtils.isSelected(it,true) }
                .sortedBy { it.getDistanceToEntityBox(mc.player!!) }[0] as EntityLivingBase?
        }catch (e: Exception){
            null
        }
    }

    override fun handleEvents(): Boolean {
        return true
    }
}