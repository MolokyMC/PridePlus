/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin


object RaycastUtils : MinecraftInstance() {

    @JvmStatic
    fun raycastEntity(range: Double, entityFilter: EntityFilter) = raycastEntity(range, RotationUtils.serverRotation.yaw, RotationUtils.serverRotation.pitch, entityFilter)

    fun raycastEntity(range: Double, yaw: Float, pitch: Float, entityFilter: EntityFilter): Entity? {
        val renderViewEntity = mc.renderViewEntity

        if (renderViewEntity != null && mc.world != null) {
            var blockReachDistance = range
            val eyePosition = renderViewEntity.getPositionEyes(1f)

            val yawCos = cos(-yaw * 0.017453292f - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292f - Math.PI.toFloat())
            val pitchCos = (-cos(-pitch * 0.017453292f.toDouble())).toFloat()
            val pitchSin = sin(-pitch * 0.017453292f.toDouble()).toFloat()

            val entityLook = Vec3d((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
            val vector = eyePosition.addVector(entityLook.x * blockReachDistance, entityLook.y * blockReachDistance, entityLook.z * blockReachDistance)
            val entityList = mc.world!!.getEntitiesInAABBexcluding(renderViewEntity, renderViewEntity.entityBoundingBox.expand(entityLook.x * blockReachDistance, entityLook.y * blockReachDistance, entityLook.z * blockReachDistance).expand(1.0, 1.0, 1.0)) {
                it != null && (it !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith()
            }

            var pointedEntity: Entity? = null

            for (entity in entityList) {
                if (!entityFilter.canRaycast(entity))
                    continue

                val collisionBorderSize = entity.collisionBorderSize.toDouble()
                val axisAlignedBB = entity.entityBoundingBox.expand(collisionBorderSize, collisionBorderSize, collisionBorderSize)

                val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector)

                if (axisAlignedBB.contains(eyePosition)) {
                    if (blockReachDistance >= 0.0) {
                        pointedEntity = entity
                        blockReachDistance = 0.0
                    }
                } else if (movingObjectPosition != null) {
                    val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)

                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                        if (entity == renderViewEntity.ridingEntity && !renderViewEntity.canRiderInteract()) {
                            if (blockReachDistance == 0.0)
                                pointedEntity = entity
                        } else {
                            pointedEntity = entity
                            blockReachDistance = eyeDistance
                        }
                    }
                }
            }

            return pointedEntity
        }

        return null
    }

    interface EntityFilter {
        fun canRaycast(entity: Entity?): Boolean
    }
}