/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox)
    val xDist = abs(pos.x - eyes.x)
    val yDist = abs(pos.y - eyes.y)
    val zDist = abs(pos.z - eyes.z)
    return sqrt(xDist.pow(2) + yDist.pow(2) + zDist.pow(2))
}
val EntityLivingBase.hurtPercent: Float
    get() = (this.renderHurtTime)/10

val EntityLivingBase.renderHurtTime: Float
    get() = this.hurtTime - if(this.hurtTime!=0) { Minecraft.getMinecraft().timer.renderPartialTicks } else { 0f }

fun getNearestPointBB(eye: Vec3d, box: AxisAlignedBB): Vec3d {
    val origin = doubleArrayOf(eye.x, eye.y, eye.z)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3d(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing(): Int {
    val playerInfo = mc.connection!!.getPlayerInfo(uniqueID)
    return playerInfo.responseTime
}

fun Entity.isAnimal(): Boolean {
    return this is EntityAnimal || this is EntitySquid || this is EntityGolem || this is EntityBat
}

fun Entity.isMob(): Boolean {
    return this is EntityMob ||
            this is EntityVillager ||
            this is EntitySlime
            || this is EntityGhast ||
            this is EntityDragon ||
            this is EntityShulker
}

fun EntityPlayer.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return Pride.fileManager.friendsConfig.isFriend(stripColor(entityName))
}