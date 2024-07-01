package com.erp.server.dmp.inout.handler.output.create;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.service.DmpOutputTaskService;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输出任务创建处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpOutputInputCreateHandler extends DmpOutputBaseCreateHandler{
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest,
			DmpOutputCreateResponse dmpResponse) {
		DmpOutputInputCreateRequest dmpOutputInputCreateRequest = (DmpOutputInputCreateRequest) dmpRequest;
		DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = dmpOutputInputCreateRequest.getDmpCfgOutputDetailEntity();
		String nextLevelId = dmpCfgOutputDetailEntity.getNextLevelId();
		DmpOutputTaskEntity dmpOutputTaskEntity = new DmpOutputTaskEntity();
		dmpOutputTaskEntity.setCfgOutputId(dmpRequest.getCfgOutputId());
		dmpOutputTaskEntity.setNextLevelId(nextLevelId);
		dmpOutputTaskEntity.setStatus(DmpOutputTaskStatusEnum.INIT.getCode());
		dmpOutputTaskEntity.setTaskType(DmpOutputTaskTypeEnum.INPUT.getCode());
		
		dmpOutputTaskService.save(dmpOutputTaskEntity);
		return Collections.singletonList(dmpOutputTaskEntity);
	}
}
