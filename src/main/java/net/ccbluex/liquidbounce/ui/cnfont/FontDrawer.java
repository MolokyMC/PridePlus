package net.ccbluex.liquidbounce.ui.cnfont;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class FontDrawer {
    private static final Map<Integer, Font> RUNTIME_FONT_MAP = new HashMap<>();
    private static final int[] COLORS = new int[32];
    private static final int SHADOW_COLOR = ColorUtils.getRGB(0, 0, 0, 50);
    public static boolean RuntimeFontAntiAliasing = true;
    private float red;
    private float blue;
    private float green;
    private float alpha;

    static {
        for (int i = 0; i < COLORS.length; ++i) {
            int offset = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;
            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            COLORS[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }

    }

    private final Glyph[] glyphs = new Glyph[65536];
    private final Font font;
    private final Font runtimeFont;
    private final int imageSize;
    private final int halfHeight;
    private final boolean antiAliasing;
    public int FONT_HEIGHT;

    public FontDrawer(Font font, boolean antiAliasing) {
        this.font = font;
        this.FONT_HEIGHT = font.getSize();
        this.imageSize = font.getSize() + 4;
        this.halfHeight = font.getSize() / 2;
        this.antiAliasing = antiAliasing;
        this.runtimeFont = this.getRuntimeFont();
    }

    private static void setRenderingHints(Graphics2D g, boolean antiAliasing) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (antiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        } else {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static void drawLine(double left, double top, double right, double bottom) {
        double j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = Tessellator.getInstance().getBuffer();
        if (GL11.glIsEnabled(3553)) {
            GlStateManager.disableTexture2D();
        }

        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        if (!GL11.glIsEnabled(3553)) {
            GlStateManager.enableTexture2D();
        }

    }

    public float DisplayFontWidths(FontDrawer font, String str) {
        return (float) font.getStringWidth(str);
    }

    public Font getFont() {
        return this.font;
    }

    public float DisplayFontWidths(String str, FontDrawer font) {
        return (float) font.getStringWidth(str);
    }

    public FontDrawer getDefaultFont() {
        return this;
    }

    public void DisplayFonts(FontDrawer font, String str, float x, float y, int color) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFonts(FontDrawer font, String str, int x, int y, int color) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFonts(String str, float x, float y, int color, FontDrawer font) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFonts(String str, float x, float y, int color) {
        this.drawString(str, x, y, color);
    }

    public void DisplayFont(String str, float x, float y, int color) {
        this.drawString(str, x, y, color);
    }

    public void DisplayFont(FontDrawer font, String str, float x, float y, int color) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFont(FontDrawer font, String str, int x, int y, int color) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFont(String str, float x, float y, int color, FontDrawer font) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFont2(FontDrawer font, String str, float x, float y, int color, boolean b) {
        if (b) {
            font.drawStringWithShadow(str, x, y, color);
        } else {
            font.drawString(str, x, y, color);
        }

    }

    public void DisplayFont2(FontDrawer font, String str, int x, int y, int color) {
        font.drawString(str, x, y, color);
    }

    public void DisplayFont2(String str, float x, float y, int color, FontDrawer font) {
        font.drawString(str, x, y, color);
    }

    private Font getRuntimeFont() {
        Font runtimeFont = (Font) RUNTIME_FONT_MAP.get(this.FONT_HEIGHT);
        if (runtimeFont == null) {
            runtimeFont = new Font("SansSerif", 0, this.FONT_HEIGHT);
            RUNTIME_FONT_MAP.put(this.FONT_HEIGHT, runtimeFont);
        }

        return runtimeFont;
    }

    public int getStringWidth(String s) {
        if (s != null && !s.isEmpty()) {
//            s = EnumChatFormatting.getTextWithoutFormattingCodes(s);
            int ret = 0;

            for (int i = 0; i < s.length(); ++i) {
                ret += this.getGlyph(s.charAt(i)).halfWidth;
            }

            return ret + 2;
        } else {
            return 0;
        }
    }

    public int getHeight() {
        return this.FONT_HEIGHT;
    }

    public int getHalfHeight() {
        return this.halfHeight;
    }

    public void drawCenteredStringWithShadow(String s, double x, double y, int color) {
        this.drawStringWithShadow(s, x - (double) this.getStringWidth(s) / 2.0D, y, color);
    }

    public int drawStringWithShadow(String s, float x, float y, int color) {
        return this.drawStringWithShadow(s, x, (double) y, color);
    }

    public int drawStringWithShadow(String s, double x, double y, int color) {
        return this.drawString(s, x, y, color, true);
    }

    public void drawStringWithShadowDirect(String s, double x, double y, int color) {
        this.drawStringDirect(s, x + 0.5D, y + 0.5D, SHADOW_COLOR);
        this.drawStringDirect(s, x, y, color);
    }

    public void drawCenteredString(String s, double x, double y, int color) {
        this.drawString(s, x - (double) this.getStringWidth(s) / 2.0D, y, color);
    }

    public void drawCenteredString(String s, double x, double y, int color, boolean b) {
        this.drawString(s, x - (double) this.getStringWidth(s) / 2.0D, y, color, b);
    }

    public void drawCenteredString(String s, float x, float y, int color, boolean b) {
        this.drawString(s, (double) x - (double) this.getStringWidth(s) / 2.0D, y, color, b);
    }

    public void drawStringDirect(String s, double x, double y, int color) {
        if (s != null && !s.isEmpty()) {
            this.preDraw();
            GLUtils.color(color);
            x = (x - 2.0D) * 2.0D;
            y = (y - 1.0D) * 2.0D;

            for (int i = 0; i < s.length(); ++i) {
                Glyph glyph = this.getGlyph(s.charAt(i));
                glyph.draw(x, y, false);
                x += glyph.width;
            }

            this.postDraw();
        }
    }

    public void drawString(String s, double x, double y, int color) {
        this.drawString(s, x, y, color, false);
    }

    public int drawString(String s, int x, int y, int color) {
        return this.drawString(s, x, y, color, false);
    }

    public void drawString(String s, float x, float y, int color) {
        this.drawString(s, x, y, color, false);
    }

    public int drawString(String s, int x, int y, int color, boolean shadow) {
        return this.drawString(s, x, (double) y, color, shadow);
    }

    public int drawString(String s, float x, float y, int color, boolean shadow) {
        return this.drawString(s, x, (double) y, color, shadow);
    }

    public int drawString(String s, double x, double y, int color, boolean shadow) {
        GlStateManager.enableAlpha();
        if (s != null && !s.isEmpty()) {

            if (shadow) drawString(s, x+1.0, y+1.0, ColorUtils.getRGB(0, 0, 0, 100), false);

            if (this.font.getSize() == 18) {
                this.FONT_HEIGHT = 18;
            }
            s = StringUtils.filterEmoji(s);
            this.preDraw();
            this.red = (float)(color >> 16 & 255) / 255.0F;
            this.blue = (float)(color >> 8 & 255) / 255.0F;
            this.green = (float)(color & 255) / 255.0F;
            this.alpha = (float)(color >> 24 & 255) / 255.0F;
            GlStateManager.color(this.red, this.blue, this.green, this.alpha);
            x = (x - 2.0D) * 2.0D;
            y = (y - 1.0D) * 2.0D;
            boolean bold = false;
            boolean italic = false;
            boolean strikethrough = false;
            boolean underline = false;

            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                Glyph glyph;
                if (c == 167) {
                    ++i;
                    if (i >= s.length()) {
                        glyph = this.getGlyph('§');
                        this.drawGlyph(glyph, x, y, bold, strikethrough, underline, italic);
                        x += glyph.width;
                    }

                    if (!shadow) {
                        int colorIndex = 21;
                        if (i < s.length()) {
                            colorIndex = "0123456789abcdefklmnor".indexOf(s.charAt(i));
                        }

                        switch (colorIndex) {
                            case 17:
                                bold = true;
                                break;
                            case 18:
                                strikethrough = true;
                                break;
                            case 19:
                                underline = true;
                                break;
                            case 20:
                                italic = true;
                                break;
                            case 21:
                                bold = false;
                                italic = false;
                                underline = false;
                                strikethrough = false;
                                GLUtils.color(color);
                                break;
                            default:
                                if (colorIndex < 16) {
                                    if (colorIndex == -1) {
                                        colorIndex = 15;
                                    }

                                    int finalColor = COLORS[colorIndex];
                                    GLUtils.color(ColorUtils.getRed(finalColor), ColorUtils.getGreen(finalColor), ColorUtils.getBlue(finalColor), ColorUtils.getAlpha(color));
                                }
                        }
                    }
                } else {
                    glyph = this.getGlyph(c);
                    this.drawGlyph(glyph, x, y, bold, strikethrough, underline, italic);
                    x += glyph.width;
                }
            }

            this.postDraw();
            return this.getStringWidth(s);
        } else {
            return 0;
        }
    }

    private void drawGlyph(Glyph glyph, double x, double y, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        if (bold) {
            glyph.draw(x + 1.0D, y, italic);
        }

        glyph.draw(x, y, italic);
        if (strikethrough) {
            double mid = y + (double) this.FONT_HEIGHT / 2.0D;
            drawLine(x, mid - 1.0D, x + (double) glyph.width, mid + 1.0D);
        }

        if (underline) {
            drawLine(x, y + (double) this.FONT_HEIGHT - 1.0D, x + (double) glyph.width, y + (double) this.FONT_HEIGHT + 1.0D);
        }

    }

    private void preDraw() {
        GLUtils.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        if (!GL11.glIsEnabled(3553)) {
            GlStateManager.enableTexture2D();
        }

        GlStateManager.scale(0.5D, 0.5D, 0.5D);
    }

    private void postDraw() {
        GlStateManager.disableBlend();
        GLUtils.popMatrix();
        GLUtils.resetColor();
    }

    public String trimStringToWidth(String p_trimStringToWidth_1_, int p_trimStringToWidth_2_, boolean p_trimStringToWidth_3_) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = p_trimStringToWidth_3_ ? p_trimStringToWidth_1_.length() - 1 : 0;
        int k = p_trimStringToWidth_3_ ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for(int l = j; l >= 0 && l < p_trimStringToWidth_1_.length() && i < p_trimStringToWidth_2_; l += k) {
            char c0 = p_trimStringToWidth_1_.charAt(l);
            int i1 = this.getStringWidth(String.valueOf(c0));
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;
                if (flag1) {
                    ++i;
                }
            }

            if (i > p_trimStringToWidth_2_) {
                break;
            }

            if (p_trimStringToWidth_3_) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private Glyph getGlyph(char c) {
        Glyph glyph = this.glyphs[c];
        if (glyph == null) {
            this.glyphs[c] = glyph = this.createGlyph(c);
        }

        return glyph;
    }

    private Glyph createGlyph(char c) {
        String s = String.valueOf(c);
        BufferedImage image = new BufferedImage(this.imageSize, this.imageSize, 2);
        Graphics2D g = image.createGraphics();
        int offset = 0;
        if (this.font.canDisplay(c)) {
            setRenderingHints(g, this.antiAliasing);
            g.setFont(this.font);
        } else {
            setRenderingHints(g, RuntimeFontAntiAliasing);
            g.setFont(this.runtimeFont);
            offset = 1;
        }

        FontMetrics fontMetrics = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(s, 0, this.FONT_HEIGHT - 1 + offset);
        g.dispose();
        return new Glyph(new DynamicTexture(image), fontMetrics.getStringBounds(s, g).getBounds().width);
    }

    final class Glyph {
        public final DynamicTexture texture;
        public final int width;
        public final int halfWidth;

        public Glyph(DynamicTexture texture, int width) {
            this.texture = texture;
            this.width = width;
            this.halfWidth = width / 2;
        }

        public void draw(double x, double y, boolean italic) {
            GlStateManager.bindTexture(this.texture.getGlTextureId());
            double offset = italic ? 2.0 : 0.0;
            GL11.glBegin(5);
            GL11.glTexCoord2d(0.0, 0.0);
            GL11.glVertex3d(x + offset, y, 0.0);
            GL11.glTexCoord2d(0.0, 1.0);
            GL11.glVertex3d(x - offset, y + (double) imageSize, 0.0);
            GL11.glTexCoord2d(1.0, 0.0);
            GL11.glVertex3d(x + (double) imageSize + offset, y, 0.0);
            GL11.glTexCoord2d(1.0, 1.0);
            GL11.glVertex3d(x + (double) imageSize - offset, y + (double) imageSize, 0.0);
            GL11.glEnd();
        }
    }
}
