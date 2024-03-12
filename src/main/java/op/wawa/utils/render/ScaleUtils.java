package op.wawa.utils.render;

import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.NumberFormat;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

public class ScaleUtils {
    static Color startColor = new Color(0xFFFA00BC);
    static Color endColor = new Color(0xFF00E4FF);

    public static void drawOutline(float x,float y,float width,float height,float radius,float line,float offset){
        enableRender2D();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        float edgeRadius = radius;
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        int vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);
        int i;
        int colorI = 0;
        double angleRadians;
        centerX = width;
        centerY = height + edgeRadius;
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }

        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = width + edgeRadius;
        centerY = height + edgeRadius;
        for (i = 0; i <= (height - y); ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            GL11.glVertex2d(centerX,centerY -  i);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = width;
        centerY = (y) + edgeRadius;
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = width;
        centerY = (y);
        for (i = 0; i <= (width - x); ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            GL11.glVertex2d(centerX - i,centerY);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x;
        centerY = (y + edgeRadius);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i+180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        colorI = 0;
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = width;
        centerY = (height + vertices + offset);
        for (i = 0; i <= (width - x); ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            GL11.glVertex2d(centerX - i,centerY);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x;
        centerY = (height + edgeRadius);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY - Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x - edgeRadius;
        centerY = (height + edgeRadius);

        for (i = 0; i <= (height - y); ++i) {
            RenderUtils.setColor(fadeBetween(startColor.getRGB(),endColor.getRGB(),(long) 20L * colorI));
            GL11.glVertex2d(centerX,centerY -  i);
            colorI++;
        }
        GL11.glEnd();
        disableRender2D();
    }
    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float) ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        } catch (IllegalArgumentException exp) {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            exp.printStackTrace();
        }
        return color3;
    }

    public static void setColor(int colorHex) {
        float alpha = (float) (colorHex >> 24 & 255) / 255.0F;
        float red = (float) (colorHex >> 16 & 255) / 255.0F;
        float green = (float) (colorHex >> 8 & 255) / 255.0F;
        float blue = (float) (colorHex & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
    public static int fadeBetween(int startColour, int endColour, double progress) {
        if (progress > 1) progress = 1 - progress % 1;
        return fadeTo(startColour, endColour, progress);
    }

    public static int fadeBetween(int startColour, int endColour, long offset) {
        return fadeBetween(startColour, endColour, ((System.currentTimeMillis() + offset) % 2000L) / 1000.0);
    }

    public static int fadeBetween(int startColour, int endColour) {
        return fadeBetween(startColour, endColour, 0L);
    }

    public static int fadeTo(int startColour, int endColour, double progress) {
        double invert = 1.0 - progress;
        int r = (int) ((startColour >> 16 & 0xFF) * invert +
                (endColour >> 16 & 0xFF) * progress);
        int g = (int) ((startColour >> 8 & 0xFF) * invert +
                (endColour >> 8 & 0xFF) * progress);
        int b = (int) ((startColour & 0xFF) * invert +
                (endColour & 0xFF) * progress);
        int a = (int) ((startColour >> 24 & 0xFF) * invert +
                (endColour >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }
    public static void enableRender2D() {

        glEnable(3042);
        glDisable(2884);
        glDisable(3553);
        glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glBlendFunc(770, 771);
        GL11.glPushMatrix();
    }

    public static void disableRender2D() {
        GL11.glPopMatrix();

        glDisable(3042);
        glEnable(2884);
        glEnable(3553);
        glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
    public static int[] getScaledMouseCoordinates(Minecraft mc, int mouseX, int mouseY) {
        int x = mouseX;
        int y = mouseY;
        switch (mc.gameSettings.guiScale) {
            case 0:
                x <<= 1;
                y <<= 1;
                break;
            case 1:
                x *= 0.5;
                y *= 0.5;
                break;
            case 3:
                x *= 1.4999999999999999998;
                y *= 1.4999999999999999998;
        }
        return new int[]{x, y};
    }


    public static double[] getScaledMouseCoordinates(Minecraft mc, double mouseX, double mouseY) {
        double x = mouseX;
        double y = mouseY;
        switch (mc.gameSettings.guiScale) {
            case 0:
                x *= 2;
                y *= 2;
                break;
            case 1:
                x *= 0.5;
                y *= 0.5;
                break;
            case 3:
                x *= 1.4999999999999999998;
                y *= 1.4999999999999999998;
        }
        return new double[]{x, y};
    }

    public static void scale(Minecraft mc) {

        switch (mc.gameSettings.guiScale) {
            case 0:
                GlStateManager.scale(0.5, 0.5, 0.5);
                break;
            case 1:
                GlStateManager.scale(2, 2, 2);
                break;
            case 3:
                GlStateManager.scale(0.6666666666666667, 0.6666666666666667, 0.6666666666666667);

        }
    }


}
