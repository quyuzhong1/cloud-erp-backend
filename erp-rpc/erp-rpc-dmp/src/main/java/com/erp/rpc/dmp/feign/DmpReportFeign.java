package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;

/**
 * DMP远程调用接口
 *
 * @author Jim
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp", contextId = "DmpReportFeign",configuration = {FeignErrorDecoder.class})
public interface DmpReportFeign {


    /**
     * 添加或更新报告计划
     */
    @PostMapping("/reportSchedule/addOrUpdate")
    Boolean addReportSchedule(@RequestBody DmpSyncReportScheduleDTO dto);

}