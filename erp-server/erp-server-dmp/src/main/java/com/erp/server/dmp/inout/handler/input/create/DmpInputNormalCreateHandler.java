package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
public class DmpInputNormalCreateHandler extends DmpInputBaseCreateHandler{

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
		
		Set<String> nextLevelIdSet = dmpInputTaskService.lambdaQuery()
				.eq(DmpInputTaskEntity::getCfgInputId, cfgInputId)
				.in(DmpInputTaskEntity::getNextLevelId, dmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getNextLevelId).collect(Collectors.toList()))
				.eq(DmpInputTaskEntity::getTaskType, DmpInputTaskTaskTypeEnum.NORMAL.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
				.ne(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.FINISH.getCode())
				.select(DmpInputTaskEntity::getNextLevelId)
				.list().stream().map(DmpInputTaskEntity::getNextLevelId).collect(Collectors.toSet());
		if(CollUtil.isNotEmpty(nextLevelIdSet)) {
			Iterator<DmpCfgInputDetailEntity> iterator = dmpCfgInputDetailEntityList.iterator();
			while(iterator.hasNext()) {
				String nextLevelId = iterator.next().getNextLevelId();
				if(nextLevelIdSet.contains(nextLevelId)) {
					iterator.remove();
					log.info("输入信息数据代码【{}】下，明细任务下一层级【{}】还有正在执行中的正常任务，此次不生成任务" ,  dmpCfgInputEntity.getCode() , nextLevelId);
				}
			}
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(dmpCfgInputDetailEntityList.size());
		DmpInputTaskEntity dmpInputTaskEntity = null;
		for(DmpCfgInputDetailEntity dmpCfgInputDetailEntity : dmpCfgInputDetailEntityList) {
			dmpInputTaskEntity = new DmpInputTaskEntity();
			dmpInputTaskEntity.setCfgInputId(cfgInputId);
			dmpInputTaskEntity.setNextLevelId(dmpCfgInputDetailEntity.getNextLevelId());
			/**
			currTime	        	overrideTime	StartTime				dealyTime	EndTime					LastTime				NextTime				IntervalTime
			2024-06-19 18:10:00		1800			2024-06-18 17:30:00		4*3600		2024-06-19 12:00:00		2024-06-19 12:00:00		2024-06-20 18:00:00		24*3600
			2024-06-20 18:10:00		1800			2024-06-19 11:30:00		4*3600		2024-06-20 12:00:00		2024-06-20 12:00:00		2024-06-21 18:00:00		24*3600
			2024-06-21 18:10:00		1800			2024-06-20 11:30:00		4*3600		2024-06-21 12:00:00		2024-06-21 12:00:00		2024-06-22 18:00:00		24*3600
			 */
			// 2024-06-19 18:10:00
			LocalDateTime startTime = LocalDateTime.now();
			// 2024-06-18 18:00:00
			LocalDateTime lastTime = dmpCfgInputDetailEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			// 1800
			Integer overrideTime = dmpCfgInputDetailEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1, ChronoUnit.SECONDS);
			}
			// 2024-06-18 17:30:00
			dmpInputTaskEntity.setStartTime(startTime);
			
			// 2024-06-19 18:00:00
			LocalDateTime nextTime = dmpCfgInputDetailEntity.getNextTime();
			LocalDateTime endTime = nextTime;
			// 4 * 3600
			Integer dealyTime = dmpCfgInputDetailEntity.getDealyTime();
			if(dealyTime != null && dealyTime != 0) {
				endTime = LocalDateTimeUtil.offset(endTime, dealyTime * -1, ChronoUnit.SECONDS);
			}
			// 2024-06-19 12:00:00
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
			// 2024-06-19 12:00:00
			dmpCfgInputDetailEntity.setLastTime(endTime);
			
			// 2024-06-20 18:00:00
			dmpCfgInputDetailEntity.setNextTime(LocalDateTimeUtil.offset(nextTime, dmpCfgInputDetailEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		dmpCfgInputDetailService.updateBatchById(dmpCfgInputDetailEntityList);
		
		return dmpInputTaskEntityList;
	}

}
