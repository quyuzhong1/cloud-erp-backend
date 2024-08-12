package com.erp.server.file.controller.feign;

import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/downloadTask")
public class DownloadTaskFeignController {
    @Resource
    private FileTaskContext fileTaskContext;

    @PostMapping
    String saveDownloadTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        return fileTaskContext.add(new FileTaskDTO(event,fileName, params));
    }
}
