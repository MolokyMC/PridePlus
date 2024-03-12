package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.minecraft.block.Block
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

object PlayerUtils {
    fun randomUnicode(str: String): String {
        val stringBuilder = StringBuilder()
        for (c in str.toCharArray()) {
            if (Math.random()> 0.5 && c.hashCode() in 33..128) {
                stringBuilder.append(Character.toChars(c.hashCode() + 65248))
            } else {
                stringBuilder.append(c)
            }
        }
        return stringBuilder.toString()
    }
    fun isBlockUnder(): Boolean {
        if (mc.player.posY < 0) return false
        var off = 0
        while (off < mc.player.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.player.entityBoundingBox
                .offset(0.0, -off.toDouble(), 0.0)
            if (mc.world!!.getCollisionBoxes(
                    mc.player,
                    bb
                ).isNotEmpty()
            ) {
                return true
            }
            off += 2
        }
        return false
    }
    fun getAr(player : EntityLivingBase):Double{
        var arPercentage: Double = (player.totalArmorValue / player.maxHealth).toDouble()
        arPercentage = MathHelper.clamp(arPercentage, 0.0, 1.0)
        return 100 * arPercentage
    }
    fun getBlockRelativeToPlayer(offsetX: Double, offsetY: Double, offsetZ: Double): Block? {
        return mc.world!!.getBlockState(
            BlockPos(
                mc.player!!.posX + offsetX,
                mc.player!!.posY + offsetY,
                mc.player!!.posZ + offsetZ
            )
        ).block
    }
}