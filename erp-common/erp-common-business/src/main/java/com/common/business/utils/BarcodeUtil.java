package com.common.business.utils;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BarcodeUtil {
    private static Logger log = LoggerFactory.getLogger(BarcodeUtil.class);

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    //生成的条形码存在时间
    private static final long THREAD_SLEEP_TIME = 20;

    private static String TEMP_SPACE;

    static {
        try {
            TEMP_SPACE = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX).getPath() + "fileTemp";
        } catch (FileNotFoundException e) {
            log.error("【classpath路径未找到】涉及的条形码相关功能无法正常完成");
        }
    }

    /**
     * 设置 条形码参数
     */
    private static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        private static final long serialVersionUID = 1L;
        {
            // 设置编码方式
            put(EncodeHintType.CHARACTER_SET, "utf-8");
        }
    };

    /**
     * @Description 生成条形码图片(存储在fileTemp资源目录下)，20s后将删除生成的二维码图片
     * @Param
     * @Return java.io.File
     * @Author wjx
     * @Date 2023/2/20 18:10
     **/
    public static File generateTempImageFile(String str) throws Exception{
        if (StringUtils.isEmpty(str)) {
            throw new Exception("二维码内容为空");
        }
        //初始化临时空间
        initTempSpace();
        //生成二维码图片路径
        File file = new File(TEMP_SPACE + File.separator
                + format.format(new Date())
                + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + ".png");
        //删除条形码图片
        ExecutorService executorService = Executors.newFixedThreadPool(2);//防止主线程结束后，新生成的线程不执行
        CompletableFuture.runAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error("【条形码图片删除操作异常】异常原因：线程沉睡失败 图片路径：" + file.getPath());
            }
            boolean successful = false;
            if (file.exists()){
                successful = file.delete();
            }
            if (successful){
                log.info("【条形码图片删除成功】图片路径：" + file.getPath());
            }else {
                log.error("【条形码图片删除失败】图片路径：" + file.getPath());
            }
        },executorService);
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(str,BarcodeFormat.CODE_128,300,100,hints);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix) ;
        //在条形码下插入文字
        image = insertWords(image, str);
        boolean successful = ImageIO.write(image, "png", file);
        if (!successful) {
            throw new Exception("条形码图片生成操作失败");
        }
        return file;
    }

    /**
     * @Description 将生成的条形码下插入文字
     * @Param image 生成的条形码
     * @Param words
     * @Return java.awt.image.BufferedImage
     * @Author wjx
     * @Date 2023/2/21 14:50
     **/
    public static BufferedImage insertWords(BufferedImage image, String words){
        BufferedImage outImage = new BufferedImage(300, 130, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outImage.createGraphics();
        g2d.setBackground(Color.WHITE);
        // 抗锯齿
        setGraphics2D(g2d);
        // 设置白色
        setColorWhite(g2d);
        // 画条形码到新的面板
        g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        // 画文字到新的面板
        Color color=new Color(0, 0, 0);
        g2d.setColor(color);
        // 字体、字型、字号
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        //文字长度
        int strWidth = g2d.getFontMetrics().stringWidth(words);

        // 画文字
        g2d.drawString(words,(image.getWidth() - strWidth) / 2  , 120);
        g2d.dispose();
        outImage.flush();
        return outImage;
    }

    /**
     * @Description 抗锯齿
     * @Param g2d
     * @Return void
     * @Author wjx
     * @Date 2023/2/21 14:51
     **/
    private static void setGraphics2D(Graphics2D g2d){
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        Stroke s = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        g2d.setStroke(s);
    }

    /**
     * @Description 设置画布和笔刷
     * @Param g2d
     * @Return void
     * @Author wjx
     * @Date 2023/2/21 14:51
     **/
    private static void setColorWhite(Graphics2D g2d){
        g2d.setColor(Color.WHITE);
        //填充整个屏幕
        g2d.fillRect(0,0,600,600);
        //设置笔刷
        g2d.setColor(Color.BLACK);
    }


    /**
     * @Description 初始化临时空间
     * @Return void
     * @Author wjx
     * @Date 2023/2/21 14:52
     **/
    private static void initTempSpace(){
        File file = new File(TEMP_SPACE);
        if (!file.exists()){
            boolean mkdir = file.mkdir();
            if (mkdir){
                log.info("【文件夹创建成功】" + TEMP_SPACE);
            }else {
                log.info("【文件夹创建失败】" + TEMP_SPACE);
            }
        }
    }

}