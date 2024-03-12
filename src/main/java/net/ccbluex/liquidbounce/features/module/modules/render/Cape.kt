
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ListValue

import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "Cape", description = "LiquidBounce+ capes.", category = ModuleCategory.RENDER)
class Cape : Module() {

    val styleValue = ListValue("Style", arrayOf("Dark", "Astolfo", "Sunny", "Target", "Wyy", "PowerX", "Azrael", "Flux", "LiquidBounce", "Light", "Novoline", "Special1", "Special2"), "Dark")

    fun getCapeLocation(value: String): ResourceLocation {
        return try {
            CapeStyle.valueOf(value.toUpperCase()).location
        } catch (e: IllegalArgumentException) {
            CapeStyle.DARK.location
        }
    }

    enum class CapeStyle(val location: ResourceLocation) {
        DARK(ResourceLocation("pride/capes/dark.png")),
        ASTOLFO(ResourceLocation("pride/capes/astolfo.png")),
        LIGHT(ResourceLocation("pride/capes/light.png")),
        SUNNY(ResourceLocation("pride/capes/Sunny.png")),
        WYY(ResourceLocation("pride/capes/Wyy.png")),
        POWERX(ResourceLocation("pride/capes/PowerX.png")),
        AZRAEL(ResourceLocation("pride/capes/azrael.png")),
        TARGET(ResourceLocation("pride/capes/Target.png")),
        FLUX(ResourceLocation("pride/capes/Flux.png")),
        LIQUIDBOUNCE(ResourceLocation("pride/capes/LiquidBounce.png")),
        NOVOLINE(ResourceLocation("pride/capes/Novoline.png")),
        SPECIAL1(ResourceLocation("pride/capes/special1.png")),
        SPECIAL2(ResourceLocation("pride/capes/special2.png"))
    }

    override val tag: String
        get() = styleValue.get()

}