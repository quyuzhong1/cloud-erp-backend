package com.erp.rpc.file.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@FeignClient(name = "erp-file", contextId = "file")
public interface FileFeign {
    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping("/feign/file/uploadFile")
    String uploadFile(@RequestParam("multipartFile") MultipartFile file);

    /**
     * 上传文件支持定义文件名称
     * @param file
     * @param fileName
     * @return
     */
    @PostMapping("/feign/file/uploadFile")
    String uploadFile(@RequestParam("file") File file,@RequestParam("fileName") String fileName);

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
    void deleteBatchFile(@RequestParam("urlList") List<String> urlList);
}
