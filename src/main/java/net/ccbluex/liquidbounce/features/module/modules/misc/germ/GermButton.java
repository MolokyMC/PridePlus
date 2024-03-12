package net.ccbluex.liquidbounce.features.module.modules.misc.germ;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import java.awt.*;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

//For MinClient
public class GermButton {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final String path;
    private final String text;
    private boolean aBoolean;

    public GermButton(String path, String text) {
        this.path = path;
        this.text = text;
        aBoolean = false;
    }

    public void drawButton(String parentUuid, int x, int y, int mouseX, int mouseY) {
        if (isHovered(x - 50, y - 10, x + 50, y + 10, mouseX, mouseY)) {
            if (!aBoolean) {
                mc.getConnection().sendPacket(new CPacketCustomPayload("germmod-netease", new PacketBuffer(new PacketBuffer(Unpooled.buffer().writeInt(13)).writeString(parentUuid).writeString(path).writeInt(2))));
                aBoolean = true;
            }
        } else if (aBoolean) {
            mc.getConnection().sendPacket(new CPacketCustomPayload("germmod-netease", new PacketBuffer(new PacketBuffer(Unpooled.buffer().writeInt(13)).writeString(parentUuid).writeString(path).writeInt(3))));
            aBoolean = false;
        }
        GlStateManager.disableBlend();
        int startX = x - 50;
        int endX = x + 50;
        int startY = y - 10;
        int endY = y + 10;

        RenderUtils.drawRoundRect(startX, startY, endX, endY, 5F, new Color(0, 0, 0, 102).getRGB());
        RenderUtils.drawRoundRect(startX, startY, (int) (startX + 0.5D), endY, 5F, new Color(44, 44, 44, 102).getRGB());
        RenderUtils.drawRoundRect((int) (startX + 0.5D), (int) (endY - 0.5D), (int) (endX - 0.5D), endY, 5F, new Color(44, 44, 44, 102).getRGB());
        RenderUtils.drawRoundRect((int) (endX - 0.5D), startY, endX, endY, 5F, new Color(44, 44, 44, 102).getRGB());
        RenderUtils.drawRoundRect((int) (startX + 0.5D), startY, (int) (endX - 0.5D), (int) (startY + 0.5), 5F, new Color(44, 44, 44, 102).getRGB());

        Fonts.minecraftFont.drawString(text, x, startY + 6, new Color(216, 216, 216).getRGB());
        GlStateManager.enableBlend();
    }

    public void mouseClicked(String parentUuid) {
        if (aBoolean) {
            mc.displayGuiScreen(null);
            mc.getConnection().sendPacket(new CPacketCustomPayload("germmod-netease", new PacketBuffer(new PacketBuffer(Unpooled.buffer().writeInt(13)).writeString(parentUuid).writeString(path).writeInt(0))));
            mc.getConnection().sendPacket(new CPacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.buffer().writeInt(11)).writeString(parentUuid)));
        }
    }
    public static boolean isHovered(int x, int y, int x2, int y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }
}
