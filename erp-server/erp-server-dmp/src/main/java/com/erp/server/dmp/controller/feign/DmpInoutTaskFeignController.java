package com.erp.server.dmp.controller.feign;

import com.common.business.dto.DmpSyncTaskDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/inout")
public class DmpInoutTaskFeignController{
	
	@Resource
    private DmpOutputUtils dmpOutputUtils;

	@Resource
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	
	/**
	 * @param updateDTO
	 */
	@PostMapping("/updateOutputTaskRecord")
    public ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
		return ApiResult.success(dmpOutputUtils.updateStatus(updateDTO.getId(), updateDTO.getStatus(), updateDTO.getResponseData() , updateDTO.getMessage()));
	}

	/**
	 * 查询同步数据
	 * @author will
	 * @date 2024/10/21 18:44
	 * @param oneDTO
	 * @return SyncInfoDTO
	 */
	@PostMapping("/getErrorData")
	public DmpPushTaskDTO.SyncInfoDTO getErrorData(@RequestBody DmpSyncTaskDTO.OneDTO oneDTO) {
		return dmpOutputTaskRecordService.getErrorData(oneDTO);
	}
}
