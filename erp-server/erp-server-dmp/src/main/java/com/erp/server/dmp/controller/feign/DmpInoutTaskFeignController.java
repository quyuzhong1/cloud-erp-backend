package com.erp.server.dmp.controller.feign;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.server.dmp.inout.utils.DmpOutputRocketMQPushUtils;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

@RestController
@RequestMapping("/feign/inout")
public class DmpInoutTaskFeignController{
	
	@Resource
    private DmpOutputRocketMQPushUtils dmpOutputRocketMQPushUtils;
	
	/**
	 * @param updateDTO
	 */
	@PostMapping("/updateOutputTaskRecord")
    public void updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
		dmpOutputRocketMQPushUtils.updateStatus(updateDTO.getId(), updateDTO.getStatus(), updateDTO.getResponseData() , updateDTO.getMessage());
	}
}
