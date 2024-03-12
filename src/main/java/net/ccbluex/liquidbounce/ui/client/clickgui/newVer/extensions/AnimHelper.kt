package net.ccbluex.liquidbounce.ui.client.clickgui.newVer.extensions

import net.ccbluex.liquidbounce.features.module.modules.other.ClickGUINew
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import op.wawa.utils.animation.AnimationUtil

fun Float.animSmooth(target: Float, speed: Float) = if (ClickGUINew.fastRenderValue.get()) target else AnimationUtil.animate(target, this, speed * RenderUtils.deltaTime * 0.025F)
fun Float.animLinear(speed: Float, min: Float, max: Float) = if (ClickGUINew.fastRenderValue.get()) { if (speed < 0F) min else max } else (this + speed).coerceIn(min, max)