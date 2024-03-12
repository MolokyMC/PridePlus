package net.ccbluex.liquidbounce.ui.cnfont;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GLUtils {
    public static void startScissor(int x, int y, int width, int height) {
        int scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        GL11.glScissor(x * scaleFactor, Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
    }

    public static void stopScissor() {
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void enable(int cap) {
        GL11.glEnable(cap);
    }

    public static void disable(int cap) {
        GL11.glDisable(cap);
    }

    public static void blendFunc(int sFactor, int dFactor) {
        GL11.glBlendFunc(sFactor, dFactor);
    }

    public static void translated(double x, double y, double z) {
        GL11.glTranslated(x, y, z);
    }

    public static void rotated(double angle, double x, double y, double z) {
        GL11.glRotated(angle, x, y, z);
    }

    public static void depthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }

    public static void color(int r, int g, int b) {
        GLUtils.color(r, g, b, 255);
    }

    public static void color(int r, int g, int b, int a) {
        GlStateManager.color((float) r / 255.0f, (float) g / 255.0f, (float) b / 255.0f, (float) a / 255.0f);
    }

    public static void color(int hex) {
        GlStateManager.color((float) (hex >> 16 & 0xFF) / 255.0f, (float) (hex >> 8 & 0xFF) / 255.0f, (float) (hex & 0xFF) / 255.0f, (float) (hex >> 24 & 0xFF) / 255.0f);
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
