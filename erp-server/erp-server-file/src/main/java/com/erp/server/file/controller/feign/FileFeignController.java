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
import java.util.ArrayList;
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

    @PostMapping(value = "/batchUploadFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> batchUploadFiles(@RequestPart("multipartFiles") MultipartFile[] multipartFiles){
        FileService fileService = fileRegistry.getHandler();
        List<String> filePathList = new ArrayList<>();
        for (int i = 0; i < multipartFiles.length; i++) {
            String filePath = fileService.uploadFile(multipartFiles[i]);
            filePathList.add(filePath);
        }
        return filePathList;
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
}
