package com.common.business.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
     * @param inputFile
     * @param targetSizeInBytes
     * @return MultipartFile
     */
    public static MultipartFile compressImageMultipartFile(File inputFile, long  targetSizeInBytes) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream =  compressImage(inputFile,targetSizeInBytes);
        // 使用 MockMultipartFile 创建 MultipartFile 对象并返回
        return new MockMultipartFile("file", inputFile.getName(), "image/jpeg", byteArrayOutputStream.toByteArray());
    }

    /**
     * 根据大小压缩返回内存流
     * @author will
     * @date 2024/12/26 19:09
     * @param inputFile
     * @param targetSizeInBytes
     * @return ByteArrayOutputStream
     */
    public static ByteArrayOutputStream compressImage(File inputFile, long targetSizeInBytes) throws IOException {
        // 获取图片的原始大小
        BufferedImage image = ImageIO.read(inputFile);
        long currentSize = inputFile.length();

        // 如果图片已经小于目标大小，直接返回
        if (currentSize <= targetSizeInBytes) {
            System.out.println("目标尺寸已经小于配置大小无需压缩");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputFile)
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
}
