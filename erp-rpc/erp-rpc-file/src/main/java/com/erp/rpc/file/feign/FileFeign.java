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

    @PostMapping("/feign/file/uploadFile")
    String uploadFile(@RequestParam("multipartFile") MultipartFile file);

    @PostMapping("/feign/file/uploadFile")
    String uploadFile(@RequestParam("file") File file,@RequestParam("fileName") String fileName);
    @PostMapping("/feign/file/deleteFile")
    int deleteFile(@RequestParam("url") String url);
    @PostMapping("/feign/file/deleteBatchFile")
    void deleteBatchFile(@RequestParam("urlList") List<String> urlList);
}
