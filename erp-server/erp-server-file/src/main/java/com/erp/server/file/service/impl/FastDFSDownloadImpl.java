package com.erp.server.file.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.FileServiceType;
import com.common.business.enums.FileServiceTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskTypeEnum;
import com.erp.server.file.repository.IFileTaskRepository;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.StorageClient1;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import com.erp.model.file.dto.FileDTO;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

/**
 * @author zdy
 * @ClassName FastDFSDownloadImpl
 * @description: TODO
 * @date 2025年06月19日
 * @version: 1.0
 */
@Slf4j
@Service
@FileServiceType(FileServiceTypeEnum.FAST_DFS)
public class FastDFSDownloadImpl implements FileService {

    @Resource(name = "fastDFSExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private IFileTaskRepository fileTaskRepository;
    private static final int THREAD_POOL_SIZE = 5;

    @Override
    public String uploadFile(MultipartFile file) {
        return FastDFSClientUtil.uploadFile(file);
    }

    @Override
    public int deleteFile(String url) {
        return FastDFSClientUtil.deleteFile(url);
    }

    @Override
    public boolean exist(String fileUrl) {
        return false;
    }

    @Override
    public void deleteBatchFile(List<String> urlList) {
        FastDFSClientUtil.deleteBatchFile(urlList);
    }

    @Override
    public String uploadFile(File file, String fileName) {
        return FastDFSClientUtil.uploadFile(file, fileName);
    }

    @Override
    public String uploadFile(byte[] buff, String fileName, Map<String, String> metaList) {
        return FastDFSClientUtil.uploadFile(buff, fileName, metaList);
    }

    @Override
    public byte[] downloadFile(String fileId) {
        return FastDFSClientUtil.getFileByte(fileId);
    }

    @Override
    public InputStream getInputStream(String fileId) {
        return FastDFSClientUtil.getInputStream(fileId);
    }

    @Override
    public String mergeFiles(List<String> fileIds) {
        String fileName = UUID.randomUUID()+".pdf";
        FileTask fileTask = FileTask.create(FileTaskEventEnum.MERGE_LABEL.getCode(), fileName, JSONUtil.toJsonStr(fileIds));
        fileTask.setType(FileTaskTypeEnum.MERGE_LABEL.getCode());
        fileTask.setStartTime(LocalDateTime.now());
        // 保存文件任务
        fileTaskRepository.save(fileTask);
        try {
            StorageClient1 client = FastDFSClientUtil.getStorageClient();
            List<CompletableFuture<byte[]>> futures = new ArrayList<>();

            for (String fileId : fileIds) {
                CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        byte[] fileBytes = client.download_file1(fileId);
                        if (fileBytes == null || fileBytes.length == 0) {
                            throw new IOException("Downloaded file is empty for: " + fileId);
                        }
                        return fileBytes;
                    } catch (Exception e) {
                        log.error("Error processing file {}: {}", fileId, e.getMessage());
                        throw new ServiceException(ApiError.FILE_OPERATION_FAILED, e.getMessage());
                    }
                }, threadPoolTaskExecutor);
                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            try {
                allFutures.get(1, TimeUnit.MINUTES);
                List<byte[]> downloadedFileBytes = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                byte[] bytes = PdfUtil.mergePdfFiles(downloadedFileBytes);
                String fileUrl = FastDFSClientUtil.uploadFile(bytes, UUID.randomUUID() + ".pdf", null);
                fileTask.setStatus(FileTaskStatusEnum.FINISH.getCode());
                fileTask.setFileUrl(fileUrl);
                return fileUrl;
            } catch (TimeoutException e) {
                throw new ServiceException(ApiError.FILE_DOWNLOAD_TIMEOUT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException(ApiError.FILE_OPERATION_INTERRUPTED);
            }
        } catch (Exception e) {
            fileTask.setStatus(FileTaskStatusEnum.FAIL.getCode());
            fileTask.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            throw new ServiceException(ApiError.FILE_OPERATION_FAILED, e.getMessage());
        }finally {
            // 更新文件任务状态为已完成
            fileTask.setFinishTime(LocalDateTime.now());
            fileTaskRepository.updateById(fileTask);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview) {
        return FastDFSClientUtil.downloadByte(fileId, fileName, contentType, bPreview);
    }

    /**
     * 压缩图片并上传（优化版本：直接从FastDFS下载、压缩、上传，避免文件系统IO）
     * @param fileUrl 原图片的FastDFS URL
     * @param targetSizeInKB 目标大小（KB），0表示不压缩
     * @return 压缩后图片的FastDFS URL
     */
    @Override
    public String compressAndUploadImage(String fileUrl, Long targetSizeInKB) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }
        
        // 如果目标大小为0，不压缩，直接返回原URL
        if (targetSizeInKB == null || targetSizeInKB <= 0) {
            return fileUrl;
        }
        
        try {
            // 1. 从FastDFS下载图片（字节数组）
            byte[] originalBytes = FastDFSClientUtil.getFileByte(fileUrl);
            if (originalBytes == null || originalBytes.length == 0) {
                throw new IOException("下载的文件为空: " + fileUrl);
            }
            
            long targetSizeInBytes = targetSizeInKB * 1024;
            long currentSize = originalBytes.length;
            
            // 如果图片已经小于目标大小，直接返回原URL
            if (currentSize <= targetSizeInBytes) {
                log.info("图片已小于目标大小，无需压缩: {} KB", currentSize / 1024);
                return fileUrl;
            }
            
            // 2. 在内存中压缩图片
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (image == null) {
                throw new IOException("无法读取图片: " + fileUrl);
            }
            
            // 优化的压缩算法：使用二分法快速找到合适的压缩参数
            byte[] compressedBytes = compressImageInMemory(image, originalBytes, targetSizeInBytes);
            
            // 3. 上传压缩后的图片到FastDFS
            String fileName = getFileNameFromUrl(fileUrl);
            String compressedFileUrl = FastDFSClientUtil.uploadFile(compressedBytes, fileName, null);
            
            log.info("图片压缩完成: 原始大小 {} KB, 压缩后大小 {} KB, 压缩率 {}%", 
                    currentSize / 1024, compressedBytes.length / 1024, 
                    String.format("%.2f", (1 - (double)compressedBytes.length / currentSize) * 100));
            
            return compressedFileUrl;
        } catch (Exception e) {
            log.error("压缩图片失败: {}", fileUrl, e);
            throw new ServiceException(ApiError.FILE_IMAGE_COMPRESS_FAILED, e.getMessage());
        }
    }
    
    /**
     * 在内存中压缩图片（优化版本：智能预判压缩参数，减少迭代次数）
     */
    private byte[] compressImageInMemory(BufferedImage image, byte[] originalBytes, long targetSizeInBytes) throws IOException {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        long originalSize = originalBytes.length;
        
        // 智能预判初始压缩比例：根据文件大小比例估算
        // 经验公式：压缩后大小 ≈ 原始大小 * (scale^2) * quality
        // 假设quality=0.8，则 scale ≈ sqrt(targetSize / (originalSize * 0.8))
        double sizeRatio = (double) targetSizeInBytes / originalSize;
        float estimatedScale = (float) Math.sqrt(sizeRatio / 0.8);
        // 限制在合理范围内
        estimatedScale = Math.max(0.3f, Math.min(0.95f, estimatedScale));
        
        float minScale = 0.3f;
        float maxScale = 0.95f;
        float currentScale = estimatedScale;
        byte[] bestResult = null;
        int iterations = 0;
        int maxIterations = 5; // 减少最大迭代次数，通过智能预判减少迭代
        
        // 固定质量参数，避免质量变化带来的不确定性
        float quality = 0.8f;
        
        while (iterations < maxIterations) {
            int targetWidth = (int) (originalWidth * currentScale);
            int targetHeight = (int) (originalHeight * currentScale);
            
            // 确保尺寸至少为1
            if (targetWidth < 1) targetWidth = 1;
            if (targetHeight < 1) targetHeight = 1;
            
            // 使用Thumbnailator在内存中压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(image)
                    .size(targetWidth, targetHeight)
                    .outputQuality(quality)
                    .outputFormat("jpg") // 明确指定格式，提高性能
                    .toOutputStream(baos);
            
            byte[] compressedBytes = baos.toByteArray();
            long compressedSize = compressedBytes.length;
            
            // 如果压缩后大小符合要求（允许5%的误差范围，避免过度迭代）
            if (compressedSize <= targetSizeInBytes * 1.05) {
                bestResult = compressedBytes;
                // 如果压缩后大小已经接近目标，直接返回，不再尝试优化
                if (compressedSize >= targetSizeInBytes * 0.9) {
                    break;
                }
                // 如果还有优化空间，尝试稍微提高质量
                if (currentScale < maxScale && iterations < 2) {
                    float nextScale = Math.min(currentScale + 0.1f, maxScale);
                    int nextWidth = (int) (originalWidth * nextScale);
                    int nextHeight = (int) (originalHeight * nextScale);
                    if (nextWidth >= 1 && nextHeight >= 1) {
                        ByteArrayOutputStream nextBaos = new ByteArrayOutputStream();
                        Thumbnails.of(image)
                                .size(nextWidth, nextHeight)
                                .outputQuality(quality)
                                .outputFormat("jpg")
                                .toOutputStream(nextBaos);
                        byte[] nextBytes = nextBaos.toByteArray();
                        if (nextBytes.length <= targetSizeInBytes * 1.05) {
                            bestResult = nextBytes;
                            break;
                        }
                    }
                }
                break;
            }
            
            // 如果压缩后仍然太大，使用二分法调整
            if (compressedSize > targetSizeInBytes) {
                maxScale = currentScale;
                currentScale = (minScale + maxScale) / 2;
            } else {
                // 如果压缩后太小，可以尝试提高
                minScale = currentScale;
                currentScale = (minScale + maxScale) / 2;
            }
            
            iterations++;
        }
        
        // 如果迭代后仍未找到合适的结果，使用最后一次的结果或最小尺寸
        if (bestResult == null) {
            int finalWidth = Math.max(1, (int) (originalWidth * minScale));
            int finalHeight = Math.max(1, (int) (originalHeight * minScale));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(image)
                    .size(finalWidth, finalHeight)
                    .outputQuality(0.7f) // 降低质量以进一步压缩
                    .outputFormat("jpg")
                    .toOutputStream(baos);
            bestResult = baos.toByteArray();
        }
        
        return bestResult;
    }
    
    /**
     * 从URL中提取文件名
     */
    private String getFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return UUID.randomUUID().toString() + ".jpg";
        }
        // FastDFS URL格式通常是: group1/M00/00/00/xxx.jpg
        int lastSlash = fileUrl.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < fileUrl.length() - 1) {
            String fileName = fileUrl.substring(lastSlash + 1);
            // 确保有扩展名
            if (fileName.contains(".")) {
                return fileName;
            }
        }
        // 如果没有找到文件名，生成一个
        return UUID.randomUUID().toString() + ".jpg";
    }

    /**
     * 解压缩ZIP文件并上传所有文件到FastDFS
     * @param zipUrl ZIP文件的FastDFS URL
     * @return 解压后的文件信息列表（文件名、URL、大小）
     */
    @Override
    public List<FileDTO.ExtractedFileInfo> unzipAndUploadFiles(String zipUrl) {
        log.info("开始解压缩ZIP文件：{}", zipUrl);
        List<FileDTO.ExtractedFileInfo> fileInfoList = new ArrayList<>();
        
        try {
            // 1. 下载ZIP文件字节数据
            byte[] zipBytes = downloadFile(zipUrl);
            if (zipBytes == null || zipBytes.length == 0) {
                throw new ServiceException(ApiError.FILE_ZIP_NOT_FOUND, zipUrl);
            }
            
            // 2. 解压ZIP文件，提取其中的文件并上传到FastDFS
            try (ZipInputStream zipStream = new ZipInputStream(
                    new BufferedInputStream(new ByteArrayInputStream(zipBytes)), 
                    Charset.forName("GBK"))) {
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    
                    // 获取文件名（只取文件名，忽略路径）
                    String entryName = entry.getName();
                    String fileName = new File(entryName).getName();
                    
                    // 跳过空文件名
                    if (fileName.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 读取文件内容到字节数组
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zipStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] fileBytes = baos.toByteArray();
                    
                    // 上传文件到FastDFS
                    String fileUrl = uploadFile(fileBytes, fileName, null);
                    if (fileUrl != null && !fileUrl.isEmpty()) {
                        FileDTO.ExtractedFileInfo fileInfo = FileDTO.ExtractedFileInfo.builder()
                                .fileName(fileName)
                                .fileUrl(fileUrl)
                                .fileSize((long) fileBytes.length)
                                .build();
                        fileInfoList.add(fileInfo);
                        log.debug("解压并上传文件成功：fileName={}, fileUrl={}, size={} bytes", 
                                fileName, fileUrl, fileBytes.length);
                    } else {
                        log.warn("上传文件失败：fileName={}", fileName);
                    }
                }
            }
            
            log.info("解压缩ZIP文件完成，共解压{}个文件", fileInfoList.size());
            return fileInfoList;
        } catch (Exception e) {
            log.error("解压缩ZIP文件失败：{}", zipUrl, e);
            throw new ServiceException(ApiError.FILE_ZIP_EXTRACT_FAILED, e.getMessage());
        }
    }

    /**
     * 根据文件夹结构创建ZIP文件并上传到FastDFS
     * @param dto 压缩文件请求DTO（包含文件夹结构和文件URL列表）
     * @return ZIP文件的FastDFS URL
     */
    @Override
    public String createZipFromFolderStructure(FileDTO.CreateZipDTO dto) {
        log.info("开始根据文件夹结构创建ZIP文件");
        
        if (dto == null) {
            throw new ServiceException(ApiError.FILE_PARAM_EMPTY);
        }
        
        Map<String, List<String>> folderStructure = dto.getFolderStructure();
        Map<String, String> fileUrlToNameMap = dto.getFileUrlToNameMap() != null 
                ? dto.getFileUrlToNameMap() 
                : new HashMap<>();
        
        // 如果文件夹结构为空，则从fileUrlToNameMap中获取所有文件，放在根目录
        if (folderStructure == null || folderStructure.isEmpty()) {
            if (fileUrlToNameMap.isEmpty()) {
                throw new ServiceException(ApiError.FILE_STRUCTURE_AND_FILES_EMPTY);
            }
            // 构建一个空的文件夹结构，所有文件放在根目录
            folderStructure = new HashMap<>();
            folderStructure.put("", new ArrayList<>(fileUrlToNameMap.keySet()));
            log.info("文件夹结构为空，所有文件将放在ZIP根目录，共{}个文件", fileUrlToNameMap.size());
        }
        
        try {
            // 创建临时ZIP文件
            File tempZipFile = File.createTempFile("download_", ".zip");
            tempZipFile.deleteOnExit();
            
            try (ZipOutputStream zos = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(tempZipFile)), 
                    Charset.forName("GBK"))) {
                
                // 遍历文件夹结构
                for (Map.Entry<String, List<String>> entry : folderStructure.entrySet()) {
                    String folderPath = entry.getKey(); // 文件夹路径，空字符串表示根目录
                    List<String> fileUrls = entry.getValue();
                    
                    if (fileUrls == null || fileUrls.isEmpty()) {
                        continue;
                    }
                    
                    // 下载并添加文件到ZIP
                    for (String fileUrl : fileUrls) {
                        try {
                            // 下载文件
                            byte[] fileBytes = downloadFile(fileUrl);
                            if (fileBytes == null || fileBytes.length == 0) {
                                log.warn("文件下载失败或为空，跳过：{}", fileUrl);
                                continue;
                            }
                            
                            // 获取文件名
                            String fileName = fileUrlToNameMap.get(fileUrl);
                            if (fileName == null || fileName.trim().isEmpty()) {
                                // 从URL中提取文件名
                                int lastSlash = fileUrl.lastIndexOf('/');
                                if (lastSlash >= 0 && lastSlash < fileUrl.length() - 1) {
                                    fileName = fileUrl.substring(lastSlash + 1);
                                } else {
                                    fileName = "file_" + System.currentTimeMillis();
                                }
                            }
                            
                            // 构建ZIP中的文件路径
                            String zipEntryPath;
                            if (folderPath == null || folderPath.trim().isEmpty()) {
                                zipEntryPath = fileName;
                            } else {
                                // 确保文件夹路径以/结尾
                                String normalizedFolderPath = folderPath.replace('\\', '/');
                                if (!normalizedFolderPath.endsWith("/")) {
                                    normalizedFolderPath += "/";
                                }
                                zipEntryPath = normalizedFolderPath + fileName;
                            }
                            
                            // 添加到ZIP
                            ZipEntry zipEntry = new ZipEntry(zipEntryPath);
                            zos.putNextEntry(zipEntry);
                            zos.write(fileBytes);
                            zos.closeEntry();
                            
                            log.debug("添加文件到ZIP：{} -> {}", fileUrl, zipEntryPath);
                        } catch (Exception e) {
                            log.error("处理文件失败：{}", fileUrl, e);
                            // 继续处理其他文件，不中断整个流程
                        }
                    }
                }
            }
            
            // 上传ZIP文件到FastDFS
            String zipFileName = "download_" + System.currentTimeMillis() + ".zip";
            String zipUrl = uploadFile(tempZipFile, zipFileName);
            
            // 删除临时文件
            tempZipFile.delete();
            
            log.info("根据文件夹结构创建ZIP文件完成，ZIP URL: {}", zipUrl);
            return zipUrl;
        } catch (Exception e) {
            log.error("根据文件夹结构创建ZIP文件失败", e);
            throw new ServiceException(ApiError.FILE_ZIP_CREATE_FAILED, e.getMessage());
        }
    }

    /**
     * 批量获取文件大小
     * @param fileUrlList 文件URL列表
     * @return 文件大小信息列表
     */
    @Override
    public List<FileDTO.FileSizeInfo> getBatchFileSize(List<String> fileUrlList) {
        List<FileDTO.FileSizeInfo> result = new ArrayList<>();
        if (fileUrlList == null || fileUrlList.isEmpty()) {
            return result;
        }
        
        // 调用FastDFSClientUtil批量获取文件大小
        Map<String, Long> sizeMap = FastDFSClientUtil.getBatchFileSize(fileUrlList);
        
        // 转换为FileSizeInfo列表
        for (String fileUrl : fileUrlList) {
            FileDTO.FileSizeInfo fileSizeInfo = FileDTO.FileSizeInfo.builder()
                    .fileUrl(fileUrl)
                    .fileSize(sizeMap.get(fileUrl))
                    .build();
            result.add(fileSizeInfo);
        }
        
        return result;
    }

    @Override
    public String uploadFileByUrl(FileDTO.UploadBase64 uploadBase64) {
        if (uploadBase64 == null || StringUtils.isBlank(uploadBase64.getUrl())) {
            return null;
        }
        HttpURLConnection conn = null;
        InputStream inStream = null;
        try {
            URL url = new URL(uploadBase64.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            // 配置连接参数
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000); // 增加读取超时时间，防止大文件传输中断
            // 设置请求头
            if (StringUtils.isNotBlank(uploadBase64.getToken())) {
                conn.setRequestProperty("X-Auth-token", uploadBase64.getToken());
            }
            // 检查响应码
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ServiceException(ApiError.FILE_DOWNLOAD_FAILED, "HTTP error code: " + responseCode);
            }
            inStream = conn.getInputStream();
            // 直接读取字节数组，避免不必要的 Base64 编解码转换
            byte[] bytes = readAllBytes(inStream);
            if (bytes.length == 0) {
                throw new ServiceException(ApiError.FILE_DOWNLOAD_FAILED, "Downloaded file is empty");
            }
            // 确定文件名
            String fileName = uploadBase64.getFileName();
            if (StringUtils.isBlank(fileName)) {
                fileName = UUID.randomUUID() + ".pdf";
            }
            // 上传文件
            return this.uploadFile(bytes, fileName, null);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("通过URL上传文件失败: {}", uploadBase64.getUrl(), e);
            throw new ServiceException(ApiError.FILE_OPERATION_FAILED, e.getMessage());
        } finally {
            // 关闭资源
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.warn("关闭输入流失败", e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 从输入流中读取所有字节
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
