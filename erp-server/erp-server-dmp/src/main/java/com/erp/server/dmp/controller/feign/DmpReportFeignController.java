package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.server.dmp.service.ReportScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 中台添加报告计划Feign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/
@Slf4j
@RestController
@RequestMapping("feign/dmp")
public class DmpReportFeignController {
    @Resource
    private ReportScheduleService reportScheduleService;

    /**
     * 添加报告计划
     */
    @PostMapping("/reportSchedule/add")
    public Boolean addReportSchedule(@RequestBody @Valid DmpSyncReportScheduleDTO dto){
        return reportScheduleService.addReportSchedule(dto);
    }

    /**
     * 取消报告计划
     */
    @PostMapping("/reportSchedule/cancel")
    public Boolean cancelReportSchedule(@RequestBody @Valid DmpSyncReportScheduleDTO dto){
        return reportScheduleService.cancelReportSchedule(dto);
    }



}
