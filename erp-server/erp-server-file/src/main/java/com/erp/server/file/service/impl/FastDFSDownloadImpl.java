package com.erp.server.file.service.impl;

import com.common.business.annotation.FileServiceType;
import com.common.business.enums.FileServiceTypeEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.csource.fastdfs.StorageClient1;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        String outputFilename = "report_"+UUID.randomUUID()+".pdf";
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
                        throw new RuntimeException(e);
                    }
                }, executor);
                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            try {
                allFutures.get(5, TimeUnit.MINUTES);
                List<byte[]> downloadedFileBytes = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                PdfUtil.mergePdfFiles2(downloadedFileBytes, outputFilename);
                String extension = outputFilename.substring(outputFilename.lastIndexOf(".") + 1);
                return FastDFSClientUtil.uploadFile(new File(outputFilename), extension);

            } catch (TimeoutException e) {
                throw new RuntimeException("File download timeout", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Operation interrupted", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("FastDFS operation failed", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            // 清理临时文件
            File file = new File(outputFilename);
            if (file.exists()) {
                try {
                    file.delete();
                } catch (SecurityException se) {
                    log.warn("Cannot delete temp file: {}", outputFilename);
                    file.deleteOnExit();
                }
            }
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview) {
        return FastDFSClientUtil.downloadByte(fileId, fileName, contentType, bPreview);
    }
}
