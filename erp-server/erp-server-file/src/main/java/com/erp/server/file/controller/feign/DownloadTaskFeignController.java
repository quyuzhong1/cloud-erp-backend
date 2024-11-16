package com.erp.server.file.controller.feign;

import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/feign/downloadTask")
public class DownloadTaskFeignController {
    @Resource
    private FileTaskContext fileTaskContext;

    @PostMapping
    String saveDownloadTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        //单据名称+年月日时分秒

        fileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return fileTaskContext.add(new FileTaskDTO(event,fileName, params));
    }
}
