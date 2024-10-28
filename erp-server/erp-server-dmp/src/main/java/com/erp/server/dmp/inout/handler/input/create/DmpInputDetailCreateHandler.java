package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.enums.DmpInputTaskMaxTimeTypeEnum;
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

/**
 * dmp输入创建dmp_cfg_input_detail明细表任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpInputDetailCreateHandler extends DmpInputBaseCreateHandler{

	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse) {
		DmpCfgInputEntity dmpCfgInputEntity = dmpResponse.getDmpCfgInputEntity();
		String cfgInputId = dmpCfgInputEntity.getId();
		
		DmpInputTaskTaskTypeEnum taskType = this.getDmpInputTaskTaskTypeEnum();
		
		List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityList = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
				.eq(DmpCfgInputDetailEntity::getTaskType, taskType.getCode())
				.eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
				.last(" and next_time <= NOW() - (INTERVAL '1 seconds' *  dealy_time) ")
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的明细任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		Set<String> nextLevelIdSet = dmpInputTaskService.lambdaQuery()
				.eq(DmpInputTaskEntity::getCfgInputId, cfgInputId)
				.in(DmpInputTaskEntity::getNextLevelId, dmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getNextLevelId).collect(Collectors.toList()))
				.in(DmpInputTaskEntity::getTaskType, this.getIngTaskType().stream().map(DmpInputTaskTaskTypeEnum::getCode).collect(Collectors.toSet()))
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
					log.info("输入信息数据代码【{}】下，明细任务下一层级【{}】还有正在执行中的{}，此次不生成任务" ,  dmpCfgInputEntity.getCode() , nextLevelId , taskType.getName());
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
			LocalDateTime endTime = dmpCfgInputDetailEntity.getNextTime();
			// 正常任务最大允许拉取的时间获取
			if (DmpInputTaskTaskTypeEnum.NORMAL.getCode().equalsIgnoreCase(getDmpInputTaskTaskTypeEnum().getCode())){
				endTime = checkAndGetMaxFetchTime(endTime, dmpCfgInputEntity, dmpCfgInputDetailEntity);
			}

			// 2024-06-19 12:00:00
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(taskType.getCode());
			dmpInputTaskEntity.setExecTimeout(dmpCfgInputDetailEntity.getExecTimeout());
			dmpInputTaskEntity.setExtendJson(dmpCfgInputDetailEntity.getExtendJson());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
			// 2024-06-19 12:00:00
			dmpCfgInputDetailEntity.setLastTime(endTime);
			
			// 2024-06-20 18:00:00
			dmpCfgInputDetailEntity.setNextTime(LocalDateTimeUtil.offset(endTime, dmpCfgInputDetailEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		dmpCfgInputDetailService.updateBatchById(dmpCfgInputDetailEntityList);
		
		return dmpInputTaskEntityList;
	}

	/**
	 * 检查最大拉取时间
	 */
	protected LocalDateTime checkAndGetMaxFetchTime(LocalDateTime curEndTime, DmpCfgInputEntity dmpCfgInputEntity, DmpCfgInputDetailEntity dmpCfgInputDetailEntity) {
		if (StringUtils.isNotBlank(dmpCfgInputEntity.getExtendJson())){
			JSONObject extendJsonObj = JSONObject.parseObject(dmpCfgInputEntity.getExtendJson());
			if (null != extendJsonObj){
				// 当前时间优先
				String maxAllowedTimeTypeStr = extendJsonObj.getString("maxTimeType");
				if (DmpInputTaskMaxTimeTypeEnum.NOW.getCode().equalsIgnoreCase(maxAllowedTimeTypeStr)){
					// 全量拉取按当前时间-延时时间
					return LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(dmpCfgInputDetailEntity.getDealyTime());
				}
			}
		}

		Integer maxIntervalTime = dmpCfgInputDetailEntity.getMaxIntervalTime();
		if (null == maxIntervalTime || 0 == maxIntervalTime || null == dmpCfgInputDetailEntity.getLastTime()){
			return curEndTime;
		}
		// 配置允许的最大时间
		LocalDateTime cfgMaxDateTime = dmpCfgInputDetailEntity.getLastTime().plusSeconds(dmpCfgInputDetailEntity.getMaxIntervalTime());
		// 当前时间-延时时间
		LocalDateTime nowDelayTime = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(dmpCfgInputDetailEntity.getDealyTime());
		// 和当前时间对比
		LocalDateTime allowMaxDateTime = cfgMaxDateTime.isAfter(nowDelayTime) ? nowDelayTime : cfgMaxDateTime;

		// 比较取最大时间
		return allowMaxDateTime.isAfter(curEndTime) ? allowMaxDateTime : curEndTime;
	}

	public abstract DmpInputTaskTaskTypeEnum getDmpInputTaskTaskTypeEnum();
	
	protected Set<DmpInputTaskTaskTypeEnum> getIngTaskType(){
		return Collections.singleton(getDmpInputTaskTaskTypeEnum());
	}
}
