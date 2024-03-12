package net.ccbluex.liquidbounce.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.EnumFacing;

import static net.ccbluex.liquidbounce.utils.MovementUtils.getDirection;

public class CodeTool extends MinecraftInstance {
    public static GuiIngame guiIngame;
    public static EnumFacing enumFacing;

    public static void setSpeed(double speed) {
        Minecraft.getMinecraft().player.motionX = -Math.sin(getDirection()) * speed;
        Minecraft.getMinecraft().player.motionZ = Math.cos(getDirection()) * speed;
    }

}
