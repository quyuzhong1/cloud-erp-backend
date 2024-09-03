package com.erp.server.file.controller.feign;

import com.common.core.utils.date.DateUtil;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/feign/downloadTask")
public class DownloadTaskFeignController {
    @Resource
    private FileTaskContext fileTaskContext;

    @PostMapping
    String saveDownloadTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params) {
        //单据名称+年月日+流水号
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(fileName);
        sb.append(date);
        return fileTaskContext.add(new FileTaskDTO(event,sb.toString(), params));
    }
}
