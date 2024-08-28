package com.erp.server.dmp.inout.handler.output.task.api;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;

@Service
@Scope("prototype")
public class DmpOutputApiTaskHandler extends DmpOutputTaskHandler{

	@Override
	public List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
		dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(dmpRequest));
		dmpOutputTaskRecordEntity.setResponseData("成功");
		return Collections.singletonList(dmpOutputTaskRecordEntity);
	}

	@Override
	protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity,
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		
	}

}
