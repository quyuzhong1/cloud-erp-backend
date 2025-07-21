package com.erp.server.file.controller.feign;

import com.common.business.dto.base.BaseDTO;
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

    @PostMapping("/saveExportTask")
    public String saveDownloadTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        //单据名称+年月日时分秒
        fileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return fileTaskContext.addExport(new FileTaskDTO(event,fileName, params));
    }

    @PostMapping("/saveImportTask")
    public String saveImportTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        //单据名称+年月日时分秒
        fileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return fileTaskContext.addImport(new FileTaskDTO(event,fileName, params));
    }

    @PostMapping("/updateTask")
    public void updateTask(@RequestBody BaseDTO.ImportResultDTO importResultDTO){
        fileTaskContext.updateTask(importResultDTO);
    }
}
