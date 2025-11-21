package com.erp.server.file.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName IFileService
 * @description: TODO
 * @date 2025年06月19日
 * @version: 1.0
 */
public interface FileService {
    boolean exist(String fileUrl);
    String uploadFile(MultipartFile file);
    String uploadFile(File file, String fileName);
    String uploadFile(byte[] buff, String fileName, Map<String, String> metaList);
    int deleteFile(String url);
    void deleteBatchFile(List<String> urlList);
    byte[] downloadFile(String fileId);
    ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview);
    InputStream getInputStream(String fileId);
}
