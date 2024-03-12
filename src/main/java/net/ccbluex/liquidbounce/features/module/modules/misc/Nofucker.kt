package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.Fucker
import net.ccbluex.liquidbounce.features.value.BlockValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos

@ModuleInfo(name ="NoFucker", description = "CNM", category = ModuleCategory.MISC)
class NoFucker: Module(){
    private val blockValue = BlockValue("Block", 26)
    private val targetId =blockValue.get()
    private var pos: BlockPos? = null
    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(pos == null || Block.getIdFromBlock(getBlock(pos!!)) != targetId || BlockUtils.getCenterDistance(Fucker.pos!!) >7)
            pos = Fucker.find(targetId)
    }
}