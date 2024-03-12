/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object EntityUtils : MinecraftInstance() {

    @JvmField
    var targetInvisible = false

    @JvmField
    var targetPlayer = true

    @JvmField
    var targetMobs = true

    @JvmField
    var targetAnimals = false

    @JvmField
    var targetDead = false

    @JvmStatic
    fun isSelected(entity: Entity?, canAttackCheck: Boolean): Boolean {
        if ((entity is EntityLivingBase) && (targetDead || entity.isEntityAlive) && entity != mc.player) {
            if (targetInvisible || !entity.isInvisible) {
                if (targetPlayer && (entity is EntityPlayer)) {
                    val entityPlayer = entity

                    if (canAttackCheck) {
                        if (isBot(entityPlayer))
                            return false

                        if (entityPlayer.isClientFriend() && !Pride.moduleManager.getModule(NoFriends::class.java).state)
                            return false

                        if (entityPlayer.isSpectator) return false
                        val teams = Pride.moduleManager.getModule(Teams::class.java) as Teams
                        return !teams.state || !teams.isInYourTeam(entityPlayer)
                    }
                    return true
                }

                return targetMobs && entity.isMob() || targetAnimals && entity.isAnimal()
            }
        }
        return false
    }
    fun isFriend(entity: Entity): Boolean {
        return entity is EntityPlayer && Pride.fileManager.friendsConfig.isFriend(stripColor(entity.name))
    }
    fun getPing(entityPlayer: EntityPlayer?): Int {
        if (entityPlayer == null) return 0
        val networkPlayerInfo: NetworkPlayerInfo = mc.connection!!.getPlayerInfo(entityPlayer.uniqueID)
        return networkPlayerInfo.responseTime
    }
}