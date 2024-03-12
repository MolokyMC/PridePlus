package me

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue


@ModuleInfo(name = "CustomUI", description = "Custom", category = ModuleCategory.RENDER)
class CustomUI : Module() {


    companion object {
        @JvmField
        val r = IntegerValue("红色", 39, 0, 255)
        @JvmField
        val g = IntegerValue("绿色", 120, 0, 255)
        @JvmField
        val b = IntegerValue("蓝色", 186, 0, 255)
        @JvmField
        val r2= IntegerValue("红色2", 20, 0, 255)
        @JvmField
        val g2= IntegerValue("绿色2", 50, 0, 255)
        @JvmField
        val b2 = IntegerValue("蓝色2", 80, 0, 255)
        @JvmField
        val a = IntegerValue("透明度", 180, 0, 255)
        @JvmField
        val radius = FloatValue("圆角大小", 3f, 0f, 10f)
        @JvmField
        val outlinet = FloatValue("描边大小", 0.4f, 0f, 5f)
        @JvmField
        val drawMode = ListValue("绘制模式", arrayOf("圆角矩形", "描边和圆角矩形","阴影","高斯模糊和阴影"), "圆角矩形")
        @JvmField
        val shadowValue = FloatValue("阴影强度", 8f,0f,20f)
        @JvmField
        val blurValue = FloatValue("模糊强度", 15f,0f,30f)
        @JvmField
        var Chinese = BoolValue("全局中文",true)

    }




}
