package com.erp.server.dmp.inout.handler.output.task.api;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.erp.server.dmp.inout.dto.base.DmpOutputTaskDto;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;

@Service
public class DmpOutputApiTaskHandler extends DmpOutputTaskHandler{

	@Override
	public DmpOutputTaskDto outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpOutputTaskDto dmpOutputTaskDto = new DmpOutputTaskDto();
		dmpOutputTaskDto.setRequestData(JSON.toJSONString(dmpRequest));
		dmpOutputTaskDto.setResponseData("成功");
		return dmpOutputTaskDto;
	}
	
}
