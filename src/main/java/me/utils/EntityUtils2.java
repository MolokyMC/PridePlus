/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package me.utils;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends;
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot;
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;

public final class EntityUtils2 extends MinecraftInstance {

    public static boolean targetInvisible = false;
    public static boolean targetPlayer = true;
    public static boolean targetMobs = true;
    public static boolean targetAnimals = false;
    public static boolean targetDead = false;

    public static boolean isSelected(final Entity entity, final boolean canAttackCheck) {
        if ((entity instanceof EntityLivingBase) && (targetDead || entity.isEntityAlive()) && entity != null && !entity.equals(mc.player)) {
            if (targetInvisible || !entity.isInvisible()) {
                if (targetPlayer && (entity instanceof EntityPlayer)) {
                    EntityPlayer entityPlayer = (EntityPlayer) entity;

                    if (canAttackCheck) {
                        if (AntiBot.isBot(entityPlayer))
                            return false;


                        if (isFriend(entityPlayer) && !Pride.moduleManager.getModule(NoFriends.class).getState())
                            return false;

                        if (entityPlayer.isSpectator())
                            return false;

                        final Teams teams = (Teams) Pride.moduleManager.getModule(Teams.class);
                        return !teams.getState() || !teams.isInYourTeam(entityPlayer);
                    }

                    return true;
                }

                return targetMobs && isMob(entity) || targetAnimals && isAnimal(entity);

            }
        }
        return false;
    }

    public static boolean isFriend(final Entity entity) {
        return (entity instanceof EntityPlayer) && entity.getName() != null &&
                Pride.fileManager.friendsConfig.isFriend(ColorUtils.stripColor(entity.getName()));
    }

    public static boolean isAnimal(final Entity entity) {
        return (entity instanceof EntityAnimal) || (entity instanceof EntitySquid) || (entity instanceof EntityGolem) ||
                (entity instanceof EntityBat);
    }

    public static boolean isMob(final Entity entity) {
        return (entity instanceof EntityMob) || (entity instanceof EntityVillager) || (entity instanceof EntitySlime)
                || (entity instanceof EntityGhast) || (entity instanceof EntityDragon) || (entity instanceof EntityShulker);
    }

    public static String getName(final NetworkPlayerInfo networkPlayerInfoIn) {
        if (networkPlayerInfoIn.getDisplayName() != null)
            return networkPlayerInfoIn.getDisplayName().getFormattedText();

        Team team = networkPlayerInfoIn.getPlayerTeam();
        String name = networkPlayerInfoIn.getGameProfile().getName();

        return team == null ? name : team.formatString(name);
    }

    public static int getPing(final EntityPlayer entityPlayer) {
        if (entityPlayer == null)
            return 0;

        final NetworkPlayerInfo networkPlayerInfo = mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID());

        return networkPlayerInfo == null ? 0 : networkPlayerInfo.getResponseTime();
    }
}
