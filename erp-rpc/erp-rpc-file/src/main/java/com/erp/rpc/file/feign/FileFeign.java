package com.erp.rpc.file.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.file.dto.FileDTO;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@FeignClient(name = "erp-file", contextId = "fileFeign",configuration = {FeignErrorDecoder.class})
public interface FileFeign {
    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @PostMapping(value = "/feign/file/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("multipartFile") MultipartFile multipartFile);

    /**
     * 上传文件支持定义文件名称
     * @param file
     * @param fileName
     * @return
     */
    @PostMapping(value ="/feign/file/uploadFileAndName", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFileAndName(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName);

    /**
     * 删除文件
     * @param url
     * @return
     */
    @PostMapping("/feign/file/deleteFile")
    int deleteFile(@RequestParam("url") String url);
    /**
     * 批量删除
     * @param urlList
     */
    @PostMapping("/feign/file/deleteBatchFile")
    void deleteBatchFile(@RequestBody List<String> urlList);

    /**
     * 下载文件
     * @param fileId
     * @return
     */
    @PostMapping("/feign/file/downloadFile")
    byte[] downloadFile(@RequestParam("fileId") String fileId);
    /**
     * 下载文件
     * @param fileId
     * @return
     */
    @GetMapping("/feign/file/getInputStream/{fileId}")
    Response getInputStream(@PathVariable("fileId") String fileId);

    @PostMapping(value = "/feign/file/uploadFileByBase64")
    String uploadFileByBase64(@RequestBody FileDTO.UploadBase64 uploadBase64);

    /**
     * 合并多个文件为一个文件
     * @param fileIds 文件id列表
     * @return 合并后的文件url
     */
    @PostMapping(value = "/feign/file/mergeFiles")
    String mergeFiles(@RequestBody List<String> fileIds);

    /**
     * 压缩图片并上传（优化版本：直接从FastDFS下载、压缩、上传，避免文件系统IO）
     * @param fileUrl 原图片的FastDFS URL
     * @param targetSizeInKB 目标大小（KB），0表示不压缩
     * @return 压缩后图片的FastDFS URL
     */
    @PostMapping("/feign/file/compressAndUploadImage")
    String compressAndUploadImage(@RequestParam("fileUrl") String fileUrl, @RequestParam("targetSizeInKB") Long targetSizeInKB);

    /**
     * 解压缩ZIP文件并上传所有文件到FastDFS
     * @param zipUrl ZIP文件的FastDFS URL
     * @return 解压后的文件信息列表（文件名、URL、大小）
     */
    @PostMapping("/feign/file/unzipAndUploadFiles")
    List<FileDTO.ExtractedFileInfo> unzipAndUploadFiles(@RequestParam("zipUrl") String zipUrl);

    /**
     * 根据文件夹结构创建ZIP文件并上传到FastDFS
     * @param dto 压缩文件请求DTO（包含文件夹结构和文件URL列表）
     * @return ZIP文件的FastDFS URL
     */
    @PostMapping("/feign/file/createZipFromFolderStructure")
    String createZipFromFolderStructure(@RequestBody FileDTO.CreateZipDTO dto);

    /**
     * 批量获取文件大小
     * @param fileUrlList 文件URL列表
     * @return 文件大小信息列表
     */
    @PostMapping("/feign/file/getBatchFileSize")
    List<FileDTO.FileSizeInfo> getBatchFileSize(@RequestBody List<String> fileUrlList);
}
