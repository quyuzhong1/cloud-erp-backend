package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DmpInputNormalCreateHandler extends DmpInputCreateHandler{

	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse) {
		DmpCfgInputEntity dmpCfgInputEntity = dmpResponse.getDmpCfgInputEntity();
		String cfgInputId = dmpCfgInputEntity.getId();
		
		List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityList = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
				.eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
				.le(DmpCfgInputDetailEntity::getNextTime, new Date())
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的明细任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		Set<String> inputDetailIdSet = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getInputDetailId, dmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getId).collect(Collectors.toList()))
				.eq(DmpInputTaskEntity::getTaskType, DmpInputTaskTaskTypeEnum.NORMAL.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.FINISH.getCode())
				.select(DmpInputTaskEntity::getInputDetailId)
				.list().stream().map(DmpInputTaskEntity::getInputDetailId).collect(Collectors.toSet());
		if(CollUtil.isNotEmpty(inputDetailIdSet)) {
			Iterator<DmpCfgInputDetailEntity> iterator = dmpCfgInputDetailEntityList.iterator();
			while(iterator.hasNext()) {
				String dmpCfgInputDetail = iterator.next().getId();
				if(inputDetailIdSet.contains(dmpCfgInputDetail)) {
					iterator.remove();
					log.info("输入信息数据代码【{}】下，明细任务【{}】还有正在执行中的正常任务，此次不生成任务" ,  dmpCfgInputEntity.getCode() , dmpCfgInputDetail);
				}
			}
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(dmpCfgInputDetailEntityList.size());
		DmpInputTaskEntity dmpInputTaskEntity = null;
		for(DmpCfgInputDetailEntity dmpCfgInputDetailEntity : dmpCfgInputDetailEntityList) {
			dmpInputTaskEntity = new DmpInputTaskEntity();
			dmpInputTaskEntity.setInputDetailId(dmpCfgInputDetailEntity.getId());
			
			LocalDateTime startTime = LocalDateTime.now();
			LocalDateTime lastTime = dmpCfgInputDetailEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			Integer overrideTime = dmpCfgInputDetailEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1, ChronoUnit.SECONDS);
			}
			dmpInputTaskEntity.setStartTime(startTime);
			
			LocalDateTime nextTime = dmpCfgInputDetailEntity.getNextTime();
			LocalDateTime endTime = nextTime;
			Integer dealyTime = dmpCfgInputDetailEntity.getDealyTime();
			if(dealyTime != null && dealyTime != 0) {
				endTime = LocalDateTimeUtil.offset(endTime, dealyTime * -1, ChronoUnit.SECONDS);
			}
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
			dmpCfgInputDetailEntity.setLastTime(nextTime);
			dmpCfgInputDetailEntity.setNextTime(LocalDateTimeUtil.offset(nextTime, dmpCfgInputDetailEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		dmpCfgInputDetailService.updateBatchById(dmpCfgInputDetailEntityList);
		
		return dmpInputTaskEntityList;
	}

}
