package com.erp.server.file.controller.feign;

import com.common.business.service.impl.RedisService;
import com.common.business.utils.ExportUtil;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/feign/downloadTask")
public class DownloadTaskFeignController {
    @Resource
    private FileTaskContext fileTaskContext;

    @Resource
    private RedisService redisService;

    @PostMapping
    String saveDownloadTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        //单据名称+年月日时分秒

        fileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return fileTaskContext.add(new FileTaskDTO(event,fileName, params));
    }
}
