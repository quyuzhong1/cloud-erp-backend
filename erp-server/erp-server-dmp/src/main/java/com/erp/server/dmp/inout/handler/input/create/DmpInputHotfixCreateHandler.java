package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DmpInputHotfixCreateHandler extends DmpInputCreateHandler{

	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse) {
		DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = (DmpInputHotfixCreateRequest)dmpRequest;
		DmpCfgInputEntity dmpCfgInputEntity = dmpResponse.getDmpCfgInputEntity();
		String cfgInputId = dmpCfgInputEntity.getId();
		List<String> cfgInputDetailIdList = dmpInputHotfixCreateRequest.getCfgInputDetailIdList();
		List<String> nextLevelIdList = dmpInputHotfixCreateRequest.getNextLevelIdList();
		
		List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityList = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
				.eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
				.in(CollUtil.isNotEmpty(cfgInputDetailIdList) , DmpCfgInputDetailEntity::getId, cfgInputDetailIdList)
				.in(CollUtil.isNotEmpty(nextLevelIdList) , DmpCfgInputDetailEntity::getNextLevelId, nextLevelIdList)
				.list();
		String msg = "";
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			msg = "输入信息数据代码【"+ dmpCfgInputEntity.getCode() +"】没有符合条件的明细任务";
			log.warn(msg);
			throw new ServiceException(msg);
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(dmpCfgInputDetailEntityList.size());
		DmpInputTaskEntity dmpInputTaskEntity = null;
		for(DmpCfgInputDetailEntity dmpCfgInputDetailEntity : dmpCfgInputDetailEntityList) {
			dmpInputTaskEntity = new DmpInputTaskEntity();
			dmpInputTaskEntity.setInputDetailId(dmpCfgInputDetailEntity.getId());
			
			dmpInputTaskEntity.setStartTime(dmpInputHotfixCreateRequest.getStartTime());
			
			LocalDateTime endTime = dmpInputHotfixCreateRequest.getEndTime();
			if(endTime != null) {
				Integer dealyTime = dmpCfgInputDetailEntity.getDealyTime();
				if(dealyTime != null && dealyTime != 0) {
					endTime = LocalDateTimeUtil.offset(endTime, dealyTime * -1, ChronoUnit.SECONDS);
				}
			}
			
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.HOTFIX.getCode());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		return dmpInputTaskEntityList;
	}

}
