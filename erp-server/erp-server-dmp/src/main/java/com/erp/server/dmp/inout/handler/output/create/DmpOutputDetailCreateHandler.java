package com.erp.server.dmp.inout.handler.output.create;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输出创建dmp_cfg_output_detail明细表任务处理器
 *
 */
@Slf4j
@Service
public abstract class DmpOutputDetailCreateHandler extends DmpOutputBaseCreateHandler {

	@Resource
	private DmpCfgOutputDetailService dmpCfgOutputDetailService;
	@Resource
	private DmpOutputTaskService dmpOutputTaskService;
	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest, DmpOutputCreateResponse dmpResponse) {
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		String cfgOutputId = dmpCfgOutputEntity.getId();
		
		DmpOutputTaskTypeEnum taskType = this.getDmpOutputTaskTypeEnum();
		
		List<DmpCfgOutputDetailEntity> dmpCfgOutputDetailEntityList = dmpCfgOutputDetailService.lambdaQuery()
				.eq(DmpCfgOutputDetailEntity::getMainId, cfgOutputId)
				.eq(DmpCfgOutputDetailEntity::getDisabled, Boolean.FALSE)
				.last(" and next_time <= NOW() - (INTERVAL '1 seconds' *  dealy_time) ")
				.list();
		if(CollUtil.isEmpty(dmpCfgOutputDetailEntityList)) {
			log.info("Output输出信息数据代码【{}】没有符合条件的明细任务" ,  dmpCfgOutputEntity.getId());
			return null;
		}
		
		Set<String> nextLevelIdSet = dmpOutputTaskService.lambdaQuery()
				.eq(DmpOutputTaskEntity::getCfgOutputId, cfgOutputId)
				.in(DmpOutputTaskEntity::getNextLevelId, dmpCfgOutputDetailEntityList.stream().map(DmpCfgOutputDetailEntity::getNextLevelId).collect(Collectors.toList()))
				.in(DmpOutputTaskEntity::getTaskType, this.getIngTaskType().stream().map(DmpOutputTaskTypeEnum::getCode).collect(Collectors.toSet()))
				.ne(DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.ERROR.getCode())
				.ne(DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.FINISH.getCode())
				.select(DmpOutputTaskEntity::getNextLevelId)
				.list().stream().map(DmpOutputTaskEntity::getNextLevelId).collect(Collectors.toSet());
		if(CollUtil.isNotEmpty(nextLevelIdSet)) {
			Iterator<DmpCfgOutputDetailEntity> iterator = dmpCfgOutputDetailEntityList.iterator();
			while(iterator.hasNext()) {
				String nextLevelId = iterator.next().getNextLevelId();
				if(nextLevelIdSet.contains(nextLevelId)) {
					iterator.remove();
					log.info("Output输出信息数据代码【{}】下，明细任务下一层级【{}】还有正在执行中的{}，此次不生成任务" ,  dmpCfgOutputEntity.getInputConvertId() , nextLevelId , taskType.getName());
				}
			}
		}
		
		List<DmpOutputTaskEntity> dmpOutputTaskEntityList = new ArrayList<>(dmpCfgOutputDetailEntityList.size());
		DmpOutputTaskEntity dmpOutputTaskEntity = null;
		for(DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity : dmpCfgOutputDetailEntityList) {
			dmpOutputTaskEntity = new DmpOutputTaskEntity();
			dmpOutputTaskEntity.setCfgOutputId(cfgOutputId);
			dmpOutputTaskEntity.setNextLevelId(dmpCfgOutputDetailEntity.getNextLevelId());
			/**
			currTime	        	overrideTime	StartTime				dealyTime	EndTime					LastTime				NextTime				IntervalTime
			2024-06-19 18:10:00		1800			2024-06-18 17:30:00		4*3600		2024-06-19 12:00:00		2024-06-19 12:00:00		2024-06-20 18:00:00		24*3600
			2024-06-20 18:10:00		1800			2024-06-19 11:30:00		4*3600		2024-06-20 12:00:00		2024-06-20 12:00:00		2024-06-21 18:00:00		24*3600
			2024-06-21 18:10:00		1800			2024-06-20 11:30:00		4*3600		2024-06-21 12:00:00		2024-06-21 12:00:00		2024-06-22 18:00:00		24*3600
			 */
			// 2024-06-19 18:10:00
			LocalDateTime startTime = LocalDateTime.now();
			// 2024-06-18 18:00:00
			LocalDateTime lastTime = dmpCfgOutputDetailEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			// 1800
			Integer overrideTime = dmpCfgOutputDetailEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1L, ChronoUnit.SECONDS);
			}
			// 2024-06-18 17:30:00
			dmpOutputTaskEntity.setStartTime(startTime);
			
			// 2024-06-19 18:00:00
			LocalDateTime endTime = dmpCfgOutputDetailEntity.getNextTime();
			// 2024-06-19 12:00:00
			dmpOutputTaskEntity.setEndTime(endTime);
			dmpOutputTaskEntity.setStatus(DmpOutputTaskStatusEnum.INIT.getCode());
			dmpOutputTaskEntity.setTaskType(taskType.getCode());
			dmpOutputTaskEntity.setExecTimeout(dmpCfgOutputDetailEntity.getExecTimeout());
			
			dmpOutputTaskEntityList.add(dmpOutputTaskEntity);
			
			// 2024-06-19 12:00:00
			dmpCfgOutputDetailEntity.setLastTime(endTime);
			
			// 2024-06-20 18:00:00
			dmpCfgOutputDetailEntity.setNextTime(LocalDateTimeUtil.offset(endTime, dmpCfgOutputDetailEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpOutputTaskService.saveBatch(dmpOutputTaskEntityList);
		
		dmpCfgOutputDetailService.updateBatchById(dmpCfgOutputDetailEntityList);
		
		return dmpOutputTaskEntityList;
	}

	public abstract DmpOutputTaskTypeEnum getDmpOutputTaskTypeEnum();
	
	protected Set<DmpOutputTaskTypeEnum> getIngTaskType(){
		return Collections.singleton(getDmpOutputTaskTypeEnum());
	}
}
