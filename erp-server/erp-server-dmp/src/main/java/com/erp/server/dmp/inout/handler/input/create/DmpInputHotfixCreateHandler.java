package com.erp.server.dmp.inout.handler.input.create;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建快速任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputHotfixCreateHandler extends DmpInputBaseCreateHandler{

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
		List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityList = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
				.eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
				.in(CollUtil.isNotEmpty(cfgInputDetailIdList) , DmpCfgInputDetailEntity::getId, cfgInputDetailIdList)
				.list();
		String msg = "";
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			msg = "输入信息数据代码【"+ dmpCfgInputEntity.getCode() +"】没有符合条件的明细任务";
			log.warn(msg);
			throw new ServiceException(msg);
		}
	
		List<String> nextLevelIdList = dmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getNextLevelId).collect(Collectors.toList());
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(nextLevelIdList.size());
		if(CollUtil.isNotEmpty(nextLevelIdList)) {
			DmpInputTaskEntity dmpInputTaskEntity = null;
			for(String nextLevelId : nextLevelIdList) {
				dmpInputTaskEntity = new DmpInputTaskEntity();
				dmpInputTaskEntity.setCfgInputId(cfgInputId);
				dmpInputTaskEntity.setNextLevelId(nextLevelId);
				
				dmpInputTaskEntity.setStartTime(dmpInputHotfixCreateRequest.getStartTime());
				dmpInputTaskEntity.setEndTime(dmpInputHotfixCreateRequest.getEndTime());
				dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
				dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.HOTFIX.getCode());
				dmpInputTaskEntity.setExecTimeout(dmpInputHotfixCreateRequest.getExecTimeout());
				
				dmpInputTaskEntityList.add(dmpInputTaskEntity);
				
			}
			dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		}
		
		return dmpInputTaskEntityList;
	}

}
