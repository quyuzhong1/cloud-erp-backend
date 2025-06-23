package com.erp.server.file.controller.feign;


import com.erp.server.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/feign/file")
public class FileFeignController {
    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart("multipartFile")MultipartFile multipartFile){
        return fileService.uploadFile(multipartFile);
    }

    @PostMapping("/deleteFile")
    public int deleteFile(@RequestParam("url") String url){
        return fileService.deleteFile(url);
    }
    @PostMapping("/deleteBatchFile")
    public void deleteBatchFile(@RequestParam("urlList") List<String> urlList){
        fileService.deleteBatchFile(urlList);
    }
    @PostMapping("/uploadFileAndName")
    public String uploadFileAndName(@RequestPart("file") File file, @RequestParam("fileName") String fileName){
        return fileService.uploadFile(file, fileName);
    }
}
