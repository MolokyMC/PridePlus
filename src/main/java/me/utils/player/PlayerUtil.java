package me.utils.player;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;


;import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

public class PlayerUtil {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isBlockUnder() {
        if(mc.player.posY < 0)
            return false;
        for(int off = 0; off < (int)mc.player.posY+2; off += 2){
            AxisAlignedBB bb = mc.player.getEntityBoundingBox().offset(0, -off, 0);
            if(!mc.world.getCollisionBoxes(mc.player, bb).isEmpty()){
                return true;
            }
        }
        return false;
    }




    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = x1 - x2;
        double d2 = y1 - y2;
        double d3 = z1 - z2;
        return Math.sqrt((double)(d0 * d0 + d2 * d2 + d3 * d3));
    }


    public static boolean isMoving() {
        if ((!mc.player.collidedHorizontally) && (!mc.player.isSneaking())) {
            return ((mc.player.movementInput.moveForward != 0.0F || mc.player.movementInput.moveStrafe != 0.0F));
        }
        return false;
    }

    public EntityLivingBase getEntity() {

        return null;
    }
    }


