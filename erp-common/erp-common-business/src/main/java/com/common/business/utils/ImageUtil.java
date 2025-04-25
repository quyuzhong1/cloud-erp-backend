package com.common.business.utils;

import cn.hutool.core.img.gif.AnimatedGifEncoder;
import cn.hutool.core.img.gif.GifDecoder;
import cn.hutool.core.lang.Pair;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * 图片处理util
 * @author will
 * @date 2024/12/26 17:41
 */
public class ImageUtil {


    /**
     * 图片压缩并且根据输出file返回
     * @author will
     * @date 2024/12/26 17:41
     * @param inputFile
     * @param outputFile
     * @param targetSizeInBytes
     */
    public static File compressImage(File inputFile, File outputFile, long targetSizeInBytes) throws IOException {
        // 获取图片的原始大小
        BufferedImage image = ImageIO.read(inputFile);
        long currentSize = inputFile.length();

        // 如果图片已经小于目标大小，直接保存
        if (currentSize <= targetSizeInBytes) {
            System.out.println("目标尺寸已经小于配置大小无需压缩");
            return inputFile;
        }

        // 初始缩放比例，避免过度压缩
        float scaleFactor = 0.95f;

        // 压缩图片直到文件大小小于目标大小
        while (currentSize > targetSizeInBytes) {
            // 使用缩放比例调整图片尺寸
            int targetWidth = (int) (image.getWidth() * scaleFactor);
            int targetHeight = (int) (image.getHeight() * scaleFactor);

            // 使用Thumbnailator进行图片缩放
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(inputFile)
                    .size(targetWidth, targetHeight)
                    // 初始压缩质量
                    .outputQuality(0.8)
                    .toOutputStream(baos);

            // 获取缩放后的文件大小
            byte[] compressedBytes = baos.toByteArray();
            currentSize = compressedBytes.length;

            // 如果文件大小符合要求，保存图片
            if (currentSize <= targetSizeInBytes) {
                System.out.println("图片的size: " + currentSize / 1024 + " KB");

                // 使用FileOutputStream将压缩后的数据写入文件
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(compressedBytes);
                }
                break;
            }
            // 如果图片仍然太大，继续缩小，调整压缩比例
            scaleFactor -= 0.05f;
            // 防止过度压缩
            if (scaleFactor < 0.3f) {
                System.out.println("图片已压缩至最小");
                break;
            }
        }
        return outputFile;
    }

    /**
     * 压缩图片返回MultipartFile
     * @author will
     * @date 2024/12/26 19:24
     * @param multipartFile
     * @param size
     * @return MultipartFile
     */
    public static MultipartFile compressImageMultipartFile(MultipartFile multipartFile, long size) throws IOException {
        //类型转换
        File inputFile = FileUtil.multiToFile(multipartFile);

        // 获取文件格式
        Pair<String, String> contentType = getImageContentType(inputFile);
        ByteArrayOutputStream byteArrayOutputStream = null;
        if ("gif".equals(contentType.getKey())) {
            byteArrayOutputStream =  compressGifToMemory(inputFile,size);

        } else {
            byteArrayOutputStream =  compressImage(inputFile,size * 1024,contentType.getKey());
        }
        // 使用 MockMultipartFile 创建 MultipartFile 对象并返回
        return new MockMultipartFile("file", inputFile.getName(), contentType.getValue(), byteArrayOutputStream.toByteArray());
    }

    /**
     * 获取图片的 contentType（根据文件格式）
     *
     * @param inputFile 输入的图片文件
     * @return 文件的 contentType (例如 image/jpeg, image/png)
     * @throws IOException 如果读取文件时发生错误
     */
    private static Pair<String,String>  getImageContentType(File inputFile) throws IOException {
        // 使用 ImageIO 读取文件头，确定文件类型
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(inputFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);

            if (readers.hasNext()) {
                String format = readers.next().getFormatName().toLowerCase();

                // 返回不同格式的 contentType
                switch (format) {
                    case "jpeg":
                    case "jpg":
                        return new Pair<>("jpeg","image/jpeg");
                    case "png":
                        return new Pair<>("png","image/png");
                    case "gif":
                        return new Pair<>("gif","image/gif");
                    case "webp":
                        return new Pair<>("webp","image/webp");
                    case "bmp":
                        return new Pair<>("bmp","image/bmp");
                    case "tiff":
                        return new Pair<>("tiff","image/tiff");
                    default:
                        // 如果未识别的格式
                        throw new IOException("无法识别文件格式: " + inputFile.getName());
                }
            } else {
                throw new IOException("无法识别文件格式: " + inputFile.getName());
            }
        }
    }


    /**
     * 根据大小压缩返回内存流
     * @author will
     * @date 2024/12/26 19:09
     * @param inputFile
     * @param targetSizeInBytes
     * @return ByteArrayOutputStream
     */
    public static ByteArrayOutputStream compressImage(File inputFile, long targetSizeInBytes,String imageType) throws IOException {
        // 获取图片的原始大小
        BufferedImage image = ImageIO.read(inputFile);
        long currentSize = inputFile.length();

        // 如果图片已经小于目标大小，直接返回
        if (currentSize <= targetSizeInBytes) {
            System.out.println("目标尺寸已经小于配置大小无需压缩");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputFile)
                    .outputFormat(imageType)
                    .scale(1)
                    // 将原始图片写入流
                    .toOutputStream(byteArrayOutputStream);
            return byteArrayOutputStream;
        }

        // 初始缩放比例，避免过度压缩
        float scaleFactor = 0.9f;

        // 创建一个内存流来保存压缩后的图片
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // 压缩图片直到文件大小小于目标大小
        while (currentSize > targetSizeInBytes) {
            // 使用缩放比例调整图片尺寸
            int targetWidth = (int) (image.getWidth() * scaleFactor);
            int targetHeight = (int) (image.getHeight() * scaleFactor);

            // 使用Thumbnailator进行图片缩放
            Thumbnails.of(inputFile)
                    .outputFormat(imageType)
                    .size(targetWidth, targetHeight)
                    // 初始压缩质量
                    .outputQuality(0.8)
                    .toOutputStream(byteArrayOutputStream);

            // 获取缩放后的文件大小
            byte[] compressedBytes = byteArrayOutputStream.toByteArray();
            currentSize = compressedBytes.length;

            // 如果文件大小符合要求，结束压缩
            if (currentSize <= targetSizeInBytes) {
                System.out.println("图片的size: " + currentSize / 1024 + " KB");
                break;
            }

            // 如果图片仍然太大，继续缩小,调整压缩比例
            scaleFactor -= 0.05f;
            // 防止过度压缩
            if (scaleFactor < 0.3f) {
                System.out.println("图片已压缩至最小");
                break;
            }
            // 清空流，准备下次压缩
            byteArrayOutputStream.reset();
        }
        // 返回包含压缩图片数据的内存流
        return byteArrayOutputStream;
    }


    /**
     * 压缩图片返回内存流
     *
     * @param inputFile 输入的图片文件
     * @param targetSizeInBytes 目标大小（字节）
     * @param initialQuality 初始压缩质量
     * @return 返回一个包含压缩图片数据的内存流（ByteArrayOutputStream）
     * @throws IOException 如果发生IO异常
     */
    public static ByteArrayOutputStream compressImage(File inputFile, long targetSizeInBytes, float initialQuality) throws IOException {
        // 获取图片的原始大小
        BufferedImage image = ImageIO.read(inputFile);
        long currentSize = inputFile.length();

        // 如果图片已经小于目标大小，直接返回
        if (currentSize <= targetSizeInBytes) {
            System.out.println("目标尺寸已经小于配置大小无需压缩");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputFile)
                    // 将原始图片写入流
                    .toOutputStream(byteArrayOutputStream);
            return byteArrayOutputStream;
        }

        // 初始压缩质量
        float quality = initialQuality;

        // 创建一个内存流来保存压缩后的图片
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // 压缩图片直到文件大小小于目标大小
        while (currentSize > targetSizeInBytes) {
            // 使用Thumbnailator进行图片压缩
            Thumbnails.of(inputFile)
                    // 设置压缩质量
                    .outputQuality(quality)
                    .toOutputStream(byteArrayOutputStream);

            // 获取压缩后的文件大小
            byte[] compressedBytes = byteArrayOutputStream.toByteArray();
            currentSize = compressedBytes.length;

            // 如果文件大小符合要求，结束压缩
            if (currentSize <= targetSizeInBytes) {
                System.out.println("图片的size: " + currentSize / 1024 + " KB");
                break;
            }

            // 如果图片仍然太大，继续降低压缩质量
            quality -= 0.05f;
            // 防止过度压缩
            if (quality < 0.3f) {
                System.out.println("图片已压缩至最小");
                break;
            }
            // 清空流，准备下次压缩
            byteArrayOutputStream.reset();
        }

        // 返回包含压缩图片数据的内存流
        return byteArrayOutputStream;
    }


    /**
     * 根据长和宽进行压缩
     * @author will
     * @date 2024/12/26 19:14
     * @param inputFile
     * @param outputFile
     * @param targetWidth
     * @param targetHeight
     */
    public static void compressImage(File inputFile, File outputFile, int targetWidth, int targetHeight) throws IOException {
        Thumbnails.of(inputFile)
                // 设置目标宽度和高度，保持比例
                .size(targetWidth, targetHeight)
                // 保存压缩后的图片
                .toFile(outputFile);
    }


    //--------------------------------------------------------gif压缩--------------------------------------------------------


    public static ByteArrayOutputStream compressGifToMemory(File sourceFile, long targetFileSizeKB) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            // 读取源 GIF 图像
            GifDecoder decoder = new GifDecoder();
            int status = decoder.read(Files.newInputStream(sourceFile.toPath()));
            if (status != GifDecoder.STATUS_OK) {
                throw new IOException("read image " + sourceFile.getAbsolutePath() + " error!");
            }
            // 初始压缩参数
            int targetWidth = (int) (decoder.getFrame(0).getWidth() / 1.5);
            int targetHeight = (int) (decoder.getFrame(0).getHeight() / 1.5);
            BigDecimal ratio = MathUtil.divide(new BigDecimal(targetWidth), new BigDecimal(targetHeight));
            // 初始时保留所有帧
            int frameSkip = 3;
            // 初始延迟因子
            int delayFactor = 2;
            // 初始质量设置（0-10，10为最差）
            int quality = 5;

            // 循环逐步压缩，直到文件大小满足目标值,文件大小（KB）
            long fileSizeKB = sourceFile.length() / 1024;

            //压缩次数
            int compressCount = 0;

            // 循环，直到文件大小满足目标
            while (fileSizeKB > targetFileSizeKB) {
                //只允许压缩15次
                if (compressCount > 15) {
                    break;
                }

                System.out.println("当前文件大小：" + fileSizeKB + "KB，继续压缩...");

                // 每次循环时重新初始化输出流，确保不会重复写入
                byteArrayOutputStream.reset();

                // 创建 GIF 编码器并开始生成目标文件
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                // 启动内存流编码
                encoder.start(byteArrayOutputStream);
                encoder.setRepeat(decoder.getLoopCount());
                // 设置编码器的质量
                encoder.setQuality(quality);
                // 逐帧添加图像，按照frameSkip减少帧数
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    if (i % frameSkip != 0) {
                        // 跳过不需要的帧
                        continue;
                    }
                    BufferedImage childImage = decoder.getFrame(i);
                    // 缩小每一帧的尺寸
                    BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                    scaledImage.getGraphics().drawImage(childImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

                    // 设置每帧的延迟时间
                    int delay = decoder.getDelay(i) * delayFactor;
                    encoder.setDelay(delay);

                    // 添加缩放后的帧
                    encoder.addFrame(scaledImage);
                }
                // 完成编码
                encoder.finish();

                // 检查内存流中的文件大小
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                // 文件大小（KB）
                fileSizeKB = byteArray.length / 1024;

                // 如果文件太大，进行进一步优化
                if (fileSizeKB > targetFileSizeKB) {
                    // 逐步调整压缩参数
                    // 1. 缩小图像尺寸，每次减少 10
                    // 不低于 150
                    targetWidth = Math.max(150, targetWidth - Integer.parseInt(MathUtil.multiplyWithTwo(ratio,new BigDecimal(100),0).toString()));
                    // 不低于 150
                    targetHeight = Math.max(150, targetHeight - 100);

                    // 2. 降低图像质量，每次增加 1,不高于 10
                    quality = Math.min(10, quality + 2);

                    // 3. 每次跳过更多的帧
                    frameSkip = Math.min(10, frameSkip + 2);

                    // 4. 延迟时间增加，减缓动画
                    delayFactor = Math.min(10, delayFactor + 2);
                } else {
                    // 文件已符合目标大小，不再压缩
                    break;
                }
                compressCount++;
            }
            System.out.println("GIF 压缩完成");

        } catch (IOException e) {
            throw new ServiceException("图片压缩失败");
        }
        // 返回内存流
        return byteArrayOutputStream;
    }
}
