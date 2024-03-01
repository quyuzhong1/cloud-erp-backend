package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.server.dmp.service.AmzReportScheduleService;
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
    private AmzReportScheduleService reportScheduleService;

    /**
     * 添加或更新报告计划
     */
    @PostMapping("/reportSchedule/addOrUpdate")
    public Boolean addReportSchedule(@RequestBody @Valid PlatformTaskDTO.DisabledDTO dto) {
        return reportScheduleService.addOrUpdateReportSchedule(dto);
    }


}
