package com.common.business.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZIP工具类
 */
@Slf4j
public class ZipUtils {

    private ZipUtils() {
    }

    public static final String GBK = "gbk";

    /**
     * 压缩多个文件
     *
     * @param fileList
     * @param zipFile
     * @throws Exception
     */
    public static void zipFiles(List<File> fileList, File zipFile) throws Exception {
        log.info("zipFiles待压缩文件：" + zipFile.getAbsolutePath());
        if (fileList != null) {
            //压缩文件已经存在，则只能单个添加
            if (zipFile.exists()) {
                for (File file : fileList) {
                    zip(zipFile, file);
                }
            } else {//不存在则新建
                // 创建zip输出流
                ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8);
                // 创建缓冲输出流
                BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
                for (File file : fileList) {
                    zipFile(file, zipOutStream, bufferOutStream);
                }
                //最后关闭输出流
                bufferOutStream.close();
                zipOutStream.close();
            }
        }
    }

    /**
     * 执行文件压缩
     *
     * @param file
     * @param zipOutStream
     * @param bufferOutStream
     * @throws IOException
     */
    private static void zipFile(File file, ZipOutputStream zipOutStream, BufferedOutputStream bufferOutStream) throws IOException {
        // 创建压缩文件实体
        ZipEntry entry = new ZipEntry(file.getName());
        // 添加实体
        zipOutStream.putNextEntry(entry);
        // 创建输入流
        BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(file));
        write(bufferInputStream, bufferOutStream);
        zipOutStream.closeEntry();
    }

    /**
     * 压缩单个文件
     *
     * @param zipFile
     * @param sourceFile
     * @throws Exception
     */
    public static void zip(File zipFile, File sourceFile) throws Exception {
        log.info("待压缩文件：" + zipFile.getAbsolutePath());
        // 添加到已经存在的压缩文件中
        if (zipFile.exists()) {
            File tempFile = new File(zipFile.getAbsolutePath() + ".tmp");
            // 创建zip输出流
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
            // 创建缓冲输出流
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
            ZipFile zipOutFile = new ZipFile(zipFile);

            Enumeration<? extends ZipEntry> entries = zipOutFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                log.info("copy: " + entry.getName());
                zipOutStream.putNextEntry(entry);
                if (!entry.isDirectory()) {
                    write(zipOutFile.getInputStream(entry), bufferOutStream);
                }
                zipOutStream.closeEntry();
            }
            zipOutFile.close();//记得关闭zip文件，否则后面无法删除原始文件
            ZipEntry entry = new ZipEntry(sourceFile.getName());
            // 添加实体
            zipOutStream.putNextEntry(entry);
            BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            write(bufferInputStream, bufferOutStream);
            //最后关闭输出流
            bufferOutStream.close();
            zipOutStream.close();
            boolean flag = zipFile.delete();
            if (flag) {
                tempFile.renameTo(zipFile);
            } else {
                log.info("删除文件失败。");
            }
        } else {// 新创建压缩文件
            // 创建zip输出流
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8);
            // 创建缓冲输出流
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
            // 创建压缩文件实体
            ZipEntry entry = new ZipEntry(sourceFile.getName());
            // 添加实体
            zipOutStream.putNextEntry(entry);
            // 创建输入流
            BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            write(bufferInputStream, bufferOutStream);
            //最后关闭输出流
            bufferOutStream.close();
            zipOutStream.close();
        }
    }

    /**
     * 读写zip文件
     *
     * @param inputStream
     * @param outStream
     * @throws IOException
     */
    private static void write(InputStream inputStream, OutputStream outStream) throws IOException {
        byte[] data = new byte[4096];
        int length = 0;
        while ((length = inputStream.read(data)) != -1) {
            outStream.write(data, 0, length);
        }
        outStream.flush();//刷新输出流
        inputStream.close();//关闭输入流
    }

    /**
     * 压缩文件目录
     *
     * @param dirFile
     * @param zipFile
     * @throws IOException
     */
    public static void zipDirectory(File dirFile, File zipFile) throws IOException {
        if (dirFile != null && dirFile.isDirectory()) {
            if (zipFile == null) {
                zipFile = new File(dirFile.getAbsolutePath() + ".zip");
            }
            String dirName = dirFile.getName() + File.separator;
            // 创建zip输出流
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipFile), Charset.forName("UTF-8"));
            // 创建缓冲输出流
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
            dealDirFile(dirFile, dirName, bufferOutStream, zipOutStream);

            //最后关闭输出流
            bufferOutStream.close();
            zipOutStream.close();
        } else {
            log.info("[" + dirFile.getName() + "]不是一个文件夹,或者不存在。");
        }
    }

    /**
     * 执行文件目录压缩
     *
     * @param dirFile
     * @param parentDir
     * @param bufferOutStream
     * @param zipOutStream
     * @throws IOException
     */
    private static void dealDirFile(File dirFile, String parentDir, BufferedOutputStream bufferOutStream, ZipOutputStream zipOutStream) throws IOException {
        File[] fileList = dirFile.listFiles();
        for (File file : fileList) {
            if (file.isFile()) {
                // 创建压缩文件实体
                ZipEntry entry = new ZipEntry(parentDir + file.getName());
                // 添加实体
                zipOutStream.putNextEntry(entry);
                // 创建输入流
                BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(file));
                write(bufferInputStream, bufferOutStream);
            } else {
                dealDirFile(file, parentDir + file.getName() + File.separator, bufferOutStream, zipOutStream);
            }
        }
    }

    /**
     * 压缩文件目录
     *
     * @param dirPath
     * @param zipPath
     * @throws IOException
     */
    public static void zipDirectory(String dirPath, String zipPath) throws IOException {
        if (zipPath == null || "".equals(zipPath)) {
            zipDirectory(new File(dirPath), null);
        } else {
            zipDirectory(new File(dirPath), new File(zipPath));
        }
    }

    /**
     * 解压文件
     *
     * @param zipFile
     * @param destDir
     * @throws IOException
     */
    public static void unzip(File zipFile, File destDir) throws IOException {
        ZipFile zipOutFile = new ZipFile(zipFile, Charset.forName(GBK));
        Enumeration<? extends ZipEntry> entries = zipOutFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                File tempFile = new File(destDir.getAbsolutePath() + File.separator + entry.getName());
                if (!tempFile.exists()) {
                    tempFile.mkdirs();
                }
            } else {
                File tempFile = new File(destDir.getAbsolutePath() + File.separator + entry.getName());
                checkParentDir(tempFile);
                FileOutputStream fileOutStream = new FileOutputStream(tempFile);
                BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
                write(zipOutFile.getInputStream(entry), bufferOutStream);
                bufferOutStream.close();
                fileOutStream.close();
            }
        }
        zipOutFile.close();//记得关闭zip文件
    }

    /**
     * 验证父目录是否存在，否则创建
     *
     * @param file
     */
    public static void checkParentDir(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }
}
