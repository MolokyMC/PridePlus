package op.wawa.utils.render;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoUtils {
    /**
     * 提取视频里的所有帧
     * 导出为.png
     * 来自CSDN | Skid By WaWa
     */
    public static void getVideoPic(File video, String picPath) {
        // 1.根据一个视频文件，创建一个按照视频中每一帧进行抓取图片的抓取对象
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(video);
        // 2.创建一个帧-->图片的转换器
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            ff.start();
            // 3.先知道这个视频一共有多少帧
            int length = ff.getLengthInFrames();

            // 4.读取视频中每一帧图片
            int cnt = 1;
            Frame frame;
            while (true) {
                frame = ff.grabFrame();

                if (frame == null) {
                    break;
                }

                if (frame.image == null) {
                    continue;
                }

                // 5.将获取的帧，存储为图片
                BufferedImage image = converter.getBufferedImage(frame);
                File picFile = new File(picPath, cnt + ".png");

                // 6.将图片保存到目标文件中
                ImageIO.write(image, "png", picFile);
                cnt++;

            }

            ff.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取视频里的所有帧
     * 导出为.png
     * 来自CSDN | Skid By WaWa
     */
    public static void getVideoPic(File video) {
        String picFolder = video.getName().split("\\.")[0] + "-png";
        String picPath = video.getParent() + "\\" + picFolder;
        File picPathFile = new File(picPath);
        if (!picPathFile.isDirectory()) {
            if (!picPathFile.mkdirs()) {
                throw new RuntimeException("创建图片存储目录失败（" + picPathFile.getAbsolutePath() + ")");
            }
        }
        getVideoPic(video, picPath);
    }
    public static void deleteFile(File file){
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()){
            return;
        }
        //取得这个目录下的所有子文件对象
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f: files){
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()){
                deleteFile(f);
            }else {
                f.delete();
            }
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete();
    }
}
