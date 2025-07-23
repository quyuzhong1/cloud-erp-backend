package com.erp.server.file.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author zdy
 * @ClassName IFileService
 * @description: TODO
 * @date 2025年06月19日
 * @version: 1.0
 */
public interface FileService {
    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    String uploadFile(MultipartFile file);

    int deleteFile(String url);
    boolean exist(String fileUrl);

    void deleteBatchFile(List<String> urlList);

    String uploadFile(File file, String fileName);

    byte[] downloadFile(String fileId);
    InputStream getInputStream(String fileId);

    ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview);
}
