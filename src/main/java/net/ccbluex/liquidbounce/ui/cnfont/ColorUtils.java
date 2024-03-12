package net.ccbluex.liquidbounce.ui.cnfont;

import net.minecraft.client.Minecraft;

import java.awt.*;

public class ColorUtils {

    public static final int RED = ColorUtils.getRGB(255, 0, 0);
    public static final int GREED = ColorUtils.getRGB(0, 255, 0);
    public static final int BLUE = ColorUtils.getRGB(0, 0, 255);
    public static final int WHITE = ColorUtils.getRGB(255, 255, 255);
    public static final int BLACK = ColorUtils.getRGB(0, 0, 0);
    public static final int NO_COLOR = ColorUtils.getRGB(0, 0, 0, 0);

    public static int getRGB(int r, int g, int b) {
        return ColorUtils.getRGB(r, g, b, 255);
    }

    public static int getRGB(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }
    public static int rainbowTick=0;
    public static int astolfoRainbow(int delay, int offset, int index) {
        //if (++rainbowTick > 50) {
        //    rainbowTick = 0;
        //}
        Color rainbow = new Color(Color.HSBtoRGB(
                (float) ((double) Minecraft.getMinecraft().player.ticksExisted / 50.0 + Math.sin((double) rainbowTick / 50.0 * 1.6))
                        % 1.0f,
                0.5f, 1.0f));
        return rainbow.getRGB();
    }
    public static int[] splitRGB(int rgb) {
        return new int[]{rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF};
    }

    public static int getRGB(int rgb) {
        return 0xFF000000 | rgb;
    }
    public static int getColor(Color color) {
        return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getColor(Color leftColor, int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }

    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        byte color = 0;
        int color1 = color | alpha << 24;
        color1 |= red << 16;
        color1 |= green << 8;
        color1 |= blue;
        return color1;
    }
    public static int reAlpha(int rgb, int alpha) {
        return ColorUtils.getRGB(ColorUtils.getRed(rgb), ColorUtils.getGreen(rgb), ColorUtils.getBlue(rgb), alpha);
    }

    public static int getRed(int rgb) {
        return rgb >> 16 & 0xFF;
    }

    public static int getGreen(int rgb) {
        return rgb >> 8 & 0xFF;
    }

    public static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    public static int getAlpha(int rgb) {
        return rgb >> 24 & 0xFF;
    }
}
