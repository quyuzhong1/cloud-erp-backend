package com.erp.rpc.file.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("erp-file")
public interface DownloadTaskFeign {

    @PostMapping("/feign/downloadTask/saveDownloadTask")
    String saveDownloadTask(@RequestParam String fileName,@RequestParam String event,@RequestBody Object params);

}
