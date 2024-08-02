package com.erp.server.dmp.controller.feign;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.server.dmp.inout.utils.DmpOutputRocketMQPushUtils;

@RestController
@RequestMapping("/feign/inout")
public class DmpInoutTaskFeignController{
	
	@Resource
    private DmpOutputRocketMQPushUtils dmpOutputRocketMQPushUtils;
	
	/**
	 * @param updateDTO
	 */
	@PostMapping("/updateOutputTaskRecord")
    public ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
		return ApiResult.success(dmpOutputRocketMQPushUtils.updateStatus(updateDTO.getId(), updateDTO.getStatus(), updateDTO.getResponseData() , updateDTO.getMessage()));
	}
}
