package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue


@ModuleInfo(name = "CustomUI", description = "Custom", category = ModuleCategory.RENDER)
class CustomUI : Module() {


    companion object {
        @JvmField
        val red = IntegerValue("Rect-Red", 255, 0, 255)
        @JvmField
        val green = IntegerValue("Rect-Green", 255, 0, 255)
        @JvmField
        val blue = IntegerValue("Rect-Blue", 255, 0, 255)
        @JvmField
        val blur = BoolValue("Blur", false)
        @JvmField
        val blurStrength = FloatValue("BlurStrength", 5f,0f,20f).displayable { blur.get() }
        @JvmField
        val shadow = BoolValue("Shadow", true)
    }




}
