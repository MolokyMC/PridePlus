package op.wawa;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import op.wawa.utils.render.VideoUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class VideoBackground {
    private final File video = Pride.fileManager.backgroundVideoFile;
    private final File dir = Pride.fileManager.tempDir;
    private File[] pics;
    private int count = 1;
    private long time = System.currentTimeMillis();
    private ResourceLocation pic = new ResourceLocation("fxxk");
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ScaledResolution scaledResolution = new ScaledResolution(mc);


    public void load(){
        if (!video.exists()) return;

        if(dir.list().length > 0) {
            VideoUtils.deleteFile(dir);
            dir.mkdir();
        }

        VideoUtils.getVideoPic(video, dir.getPath());

        pics = dir.listFiles();
    }

    public void reload(){
        if(!dir.exists())
            dir.mkdir();

        load();
    }

    public void play(){
        final int width = scaledResolution.getScaledWidth();
        final int height = scaledResolution.getScaledHeight();
        try {
            BufferedImage bufferedImage = ImageIO.read(pics[count]);
            mc.getTextureManager().loadTexture(pic, new DynamicTexture(bufferedImage));
            mc.getTextureManager().bindTexture(pic);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, width, height, width, height, width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (count >= pics.length){
            count = 1;
        } else if (System.currentTimeMillis() + 33 >= time) {
            count++;
            time = System.currentTimeMillis();
        }
    }
}
