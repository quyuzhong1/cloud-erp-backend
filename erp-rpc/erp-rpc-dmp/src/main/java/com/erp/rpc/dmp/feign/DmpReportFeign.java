package com.erp.rpc.dmp.feign;


import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * DMP远程调用接口
 *
 * @author Jim
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp", contextId = "DmpReportFeign")
public interface DmpReportFeign {


    /**
     * 添加报告计划
     */
    @PostMapping("/reportSchedule/add")
    Boolean addReportSchedule(@RequestBody DmpSyncReportScheduleDTO dto);

    /**
     * 取消报告计划
     */
    @PostMapping("/reportSchedule/cancel")
    Boolean cancelReportSchedule(@RequestBody DmpSyncReportScheduleDTO dto);
}