package net.ccbluex.liquidbounce.ui.cnfont;


import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class FontLoaders {

    public static FontDrawer F40;

    public static FontDrawer F14;
    public static FontDrawer F35;
    public static FontDrawer F22;
    public static FontDrawer F10;
    public static FontDrawer F24;
    public static FontDrawer F26;
    public static FontDrawer F16;
    public static void initFonts() {
        F10 = getFont("misans.ttf", 10, true);
        F16 = getFont("misans.ttf", 16, true);
        F14 = getFont("misans.ttf", 14, true);
        F35 = getFont("misans.ttf", 35, true);
        F40 = getFont("misans.ttf", 40, true);
        F22 = getFont("misans.ttf", 22, true);
        F24 = getFont("misans.ttf", 24, true);
        F26 = getFont("misans.ttf", 22, true);
    }

    public static FontDrawer getFont(String name, int size, boolean antiAliasing) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("pride/font/" + name)).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (FontFormatException | IOException e) {
            // System.out.println("PridePlus CNFont >> Error loading " + name + ".");
            font = new Font("default", Font.PLAIN, size);
        }
        return new FontDrawer(font, antiAliasing);
    }
}
