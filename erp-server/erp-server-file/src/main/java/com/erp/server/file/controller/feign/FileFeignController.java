package com.erp.server.file.controller.feign;


import cn.hutool.core.collection.CollUtil;
import com.common.core.utils.FileUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/feign/file")
public class FileFeignController {
    @Resource
    private FileRegistry fileRegistry;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestPart("multipartFile")MultipartFile multipartFile){
        FileService fileService = fileRegistry.getHandler();
        return fileService.uploadFile(multipartFile);
    }

    @PostMapping("/deleteFile")
    public int deleteFile(@RequestParam("url") String url){
        FileService fileService = fileRegistry.getHandler();
        return fileService.deleteFile(url);
    }
    @PostMapping("/deleteBatchFile")
    public void deleteBatchFile(@RequestBody List<String> urlList){
        if(CollUtil.isNotEmpty(urlList)){
            FileService fileService = fileRegistry.getHandler();
            fileService.deleteBatchFile(urlList);
        }
    }
    @PostMapping(value = "/uploadFileAndName", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFileAndName(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName){
        FileService fileService = fileRegistry.getHandler();
        return fileService.uploadFile(FileUtil.multiToFile(file), fileName);
    }

    @PostMapping("/downloadFile")
    public byte[] downloadFile(@RequestParam("fileId") String fileId){
        FileService fileService = fileRegistry.getHandler();
        return fileService.downloadFile(fileId);
    }

    @GetMapping("/getInputStream/{fileId}")
    public void getInputStream(@PathVariable("fileId") String fileId, HttpServletResponse response) throws IOException {
        FileService fileService = fileRegistry.getHandler();
        InputStream input = fileService.getInputStream(fileId);
        OutputStream out = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
    @PostMapping(value = "/uploadFileByBase64")
    public String uploadFileByBase64(@RequestBody FileDTO.UploadBase64 uploadBase64){
        FileService fileService = fileRegistry.getHandler();
        String[] parts = uploadBase64.getBase64().split(",");
        byte[] bytes = Base64.getDecoder().decode(parts.length > 1 ? parts[1] : parts[0]);
        String fileName = uploadBase64.getFileName() != null ? uploadBase64.getFileName() : UUID.randomUUID().toString() + ".pdf";
        return fileService.uploadFile(bytes,fileName,null);
    }

    /**
     * 合并多个文件为一个文件
     * @param fileIds 文件id列表
     * @return 合并后的文件url
     */
    @PostMapping(value = "/mergeFiles")
    public String mergeFiles(@RequestBody List<String> fileIds){
        FileService fileService = fileRegistry.getHandler();
        return fileService.mergeFiles(fileIds);
    }

    /**
     * 压缩图片并上传（优化版本：直接从FastDFS下载、压缩、上传，避免文件系统IO）
     * @param fileUrl 原图片的FastDFS URL
     * @param targetSizeInKB 目标大小（KB），0表示不压缩
     * @return 压缩后图片的FastDFS URL
     */
    @PostMapping("/compressAndUploadImage")
    public String compressAndUploadImage(@RequestParam("fileUrl") String fileUrl, @RequestParam("targetSizeInKB") Long targetSizeInKB){
        FileService fileService = fileRegistry.getHandler();
        return fileService.compressAndUploadImage(fileUrl, targetSizeInKB);
    }

    /**
     * 解压缩ZIP文件并上传所有文件到FastDFS
     * @param zipUrl ZIP文件的FastDFS URL
     * @return 解压后的文件信息列表（文件名、URL、大小）
     */
    @PostMapping("/unzipAndUploadFiles")
    public List<FileDTO.ExtractedFileInfo> unzipAndUploadFiles(@RequestParam("zipUrl") String zipUrl){
        FileService fileService = fileRegistry.getHandler();
        return fileService.unzipAndUploadFiles(zipUrl);
    }

    /**
     * 根据文件夹结构创建ZIP文件并上传到FastDFS
     * @param dto 压缩文件请求DTO（包含文件夹结构和文件URL列表）
     * @return ZIP文件的FastDFS URL
     */
    @PostMapping("/createZipFromFolderStructure")
    public String createZipFromFolderStructure(@RequestBody FileDTO.CreateZipDTO dto){
        FileService fileService = fileRegistry.getHandler();
        return fileService.createZipFromFolderStructure(dto);
    }
}
