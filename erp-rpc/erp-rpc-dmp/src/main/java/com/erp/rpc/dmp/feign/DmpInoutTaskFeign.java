package com.erp.rpc.dmp.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;

@FeignClient(name = "erp-dmp" , contextId = "DmpInoutTaskFeign")
public interface DmpInoutTaskFeign {
	/**
     * 更新任务状态
     */
    @PostMapping("feign/inout/updateOutputTaskRecord")
    ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO);
}