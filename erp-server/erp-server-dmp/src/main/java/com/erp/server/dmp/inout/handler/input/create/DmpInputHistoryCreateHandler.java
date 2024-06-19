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
import com.erp.model.dmp.entity.DmpCfgInputHistoryEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgInputHistoryTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputHistoryService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DmpInputHistoryCreateHandler extends DmpInputCreateHandler{

	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpCfgInputHistoryService dmpCfgInputHistoryService;
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
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的明细任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		List<String> dmpCfgInputDetailIdList = dmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getId).collect(Collectors.toList());
		List<DmpCfgInputHistoryEntity> dmpCfgInputHistoryEntityList = dmpCfgInputHistoryService.lambdaQuery()
				.in(DmpCfgInputHistoryEntity::getMainId, dmpCfgInputDetailIdList)
				.eq(DmpCfgInputHistoryEntity::getDisabled, Boolean.FALSE)
				.le(DmpCfgInputHistoryEntity::getNextTime, new Date())
				.list();
		
		Set<String> inputDetailIdSet = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getInputDetailId, dmpCfgInputDetailIdList)
				.eq(DmpInputTaskEntity::getTaskType, DmpInputTaskTaskTypeEnum.HISTORY.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.FINISH.getCode())
				.select(DmpInputTaskEntity::getInputDetailId)
				.list().stream().map(DmpInputTaskEntity::getInputDetailId).collect(Collectors.toSet());
		if(CollUtil.isNotEmpty(inputDetailIdSet)) {
			Iterator<DmpCfgInputHistoryEntity> iterator = dmpCfgInputHistoryEntityList.iterator();
			while(iterator.hasNext()) {
				String dmpCfgInputDetail = iterator.next().getMainId();
				if(inputDetailIdSet.contains(dmpCfgInputDetail)) {
					iterator.remove();
					log.info("输入信息数据代码【{}】下，明细任务【{}】还有正在执行中的历史任务，此次不生成任务" ,  dmpCfgInputEntity.getCode() , dmpCfgInputDetail);
				}
			}
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(dmpCfgInputDetailEntityList.size());
		DmpInputTaskEntity dmpInputTaskEntity = null;
		for(DmpCfgInputHistoryEntity dmpCfgInputHistoryEntity : dmpCfgInputHistoryEntityList) {
			dmpInputTaskEntity = new DmpInputTaskEntity();
			dmpInputTaskEntity.setInputDetailId(dmpCfgInputHistoryEntity.getMainId());
			
			/**
			currTime	        	overrideTime	StartTime				dealyTime	EndTime					LastTime				NextTime				IntervalTime
			2024-06-19 18:10:00		1800			2024-06-18 17:30:00		4*3600		2024-06-19 12:00:00		2024-06-19 12:00:00		2024-06-20 18:00:00		24*3600
			2024-06-20 18:10:00		1800			2024-06-19 11:30:00		4*3600		2024-06-20 12:00:00		2024-06-20 12:00:00		2024-06-21 18:00:00		24*3600
			2024-06-21 18:10:00		1800			2024-06-20 11:30:00		4*3600		2024-06-21 12:00:00		2024-06-21 12:00:00		2024-06-22 18:00:00		24*3600
			 */
			
			LocalDateTime startTime = LocalDateTime.now();
			LocalDateTime lastTime = dmpCfgInputHistoryEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			Integer overrideTime = dmpCfgInputHistoryEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1, ChronoUnit.SECONDS);
			}
			dmpInputTaskEntity.setStartTime(startTime);
			
			LocalDateTime nextTime = dmpCfgInputHistoryEntity.getNextTime();
			LocalDateTime endTime = nextTime;
			String type = dmpCfgInputHistoryEntity.getType();
			long dealyTime = Long.valueOf(type);
			endTime = LocalDateTimeUtil.offset(endTime, dealyTime * -1, ChronoUnit.SECONDS);
			
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.HISTORY.getCode());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
			dmpCfgInputHistoryEntity.setLastTime(endTime);
			dmpCfgInputHistoryEntity.setNextTime(LocalDateTimeUtil.offset(nextTime, dmpCfgInputHistoryEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		dmpCfgInputHistoryService.updateBatchById(dmpCfgInputHistoryEntityList);
		
		return dmpInputTaskEntityList;
	}

}
