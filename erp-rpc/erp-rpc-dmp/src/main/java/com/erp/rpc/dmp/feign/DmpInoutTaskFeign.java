package com.erp.rpc.dmp.feign;


import com.common.business.dto.DmpSyncTaskDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "erp-dmp" , contextId = "DmpInoutTaskFeign")
public interface DmpInoutTaskFeign {
	/**
     * 更新任务状态
     */
    @PostMapping("feign/inout/updateOutputTaskRecord")
    ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO);

    /**
     * 查询同步数据
     */
    @PostMapping("feign/inout/getErrorData")
    DmpPushTaskDTO.SyncInfoDTO getErrorData(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO);
}