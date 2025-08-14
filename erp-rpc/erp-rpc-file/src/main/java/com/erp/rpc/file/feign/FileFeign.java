package com.erp.rpc.file.feign;

import com.common.business.config.FeignErrorDecoder;
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
    void deleteBatchFile(@RequestParam("urlList") List<String> urlList);

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
}
