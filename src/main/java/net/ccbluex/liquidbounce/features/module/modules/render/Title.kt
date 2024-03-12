package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.TextValue

@ModuleInfo(name = "Title", description = "Title", category = ModuleCategory.MISC)
class Title : Module(){
    private val fakeNameValue = TextValue("SetTitle", "\uD835\uDCDF\uD835\uDCFB\uD835\uDCF2\uD835\uDCED\uD835\uDCEE")
    private val CopywritingValue = TextValue("Copywriting", "\uD835\uDCD2\uD835\uDCEA\uD835\uDCF7 \uD835\uDCD2\uD835\uDCFE\uD835\uDCFC\uD835\uDCFD\uD835\uDCF8\uD835\uDCF6")
    private var S = 0
    private var HM = 0
    private var M = 0
    private var H = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        HM += 1
        if (HM ==20){
            S = S + 1
            HM = 0
        }
        if (S ==60){
            M = M +1
            S = 0
        }
        if (M==60){
            H = H+1
            M = 0
        }
        org.lwjgl.opengl.Display.setTitle(fakeNameValue.get()+ " |  "+CopywritingValue.get() +" | 你已游玩"  +H  +"  时  "  +M +"  分  "+S+"  秒  ")
    }
}