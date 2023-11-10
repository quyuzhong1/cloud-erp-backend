package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.server.dmp.service.ReportHandleService;
import com.erp.server.dmp.service.ReportScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 中台请求亚马逊Feign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/
@Slf4j
@RestController
@RequestMapping("feign/dmp")
public class DmpAmazonFeignController {
    @Resource
    private ReportHandleService reportHandleService;

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/amazon/getShipment")
    public Boolean pullShipment(@RequestBody @Valid DmpPullShipmentDTO dto){
        return reportHandleService.pullShipment(dto);
    }



}
