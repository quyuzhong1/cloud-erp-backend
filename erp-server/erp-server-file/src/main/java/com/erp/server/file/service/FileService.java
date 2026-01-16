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

    /**
     * 合并多个文件为一个文件
     * @param fileUrlList 文件url列表
     * @return 合并后的文件url
     */
    String mergeFiles(List<String> fileUrlList);

    /**
     * 压缩图片并上传（优化版本：直接从FastDFS下载、压缩、上传，避免文件系统IO）
     * @param fileUrl 原图片的FastDFS URL
     * @param targetSizeInKB 目标大小（KB），0表示不压缩
     * @return 压缩后图片的FastDFS URL
     */
    String compressAndUploadImage(String fileUrl, Long targetSizeInKB);

    /**
     * 解压缩ZIP文件并上传所有文件到FastDFS
     * @param zipUrl ZIP文件的FastDFS URL
     * @return 解压后的文件信息列表（文件名、URL、大小）
     */
    List<com.erp.model.file.dto.FileDTO.ExtractedFileInfo> unzipAndUploadFiles(String zipUrl);

    /**
     * 根据文件夹结构创建ZIP文件并上传到FastDFS
     * @param dto 压缩文件请求DTO（包含文件夹结构和文件URL列表）
     * @return ZIP文件的FastDFS URL
     */
    String createZipFromFolderStructure(com.erp.model.file.dto.FileDTO.CreateZipDTO dto);

    /**
     * 批量获取文件大小
     * @param fileUrlList 文件URL列表
     * @return 文件大小信息列表
     */
    List<com.erp.model.file.dto.FileDTO.FileSizeInfo> getBatchFileSize(List<String> fileUrlList);
}
