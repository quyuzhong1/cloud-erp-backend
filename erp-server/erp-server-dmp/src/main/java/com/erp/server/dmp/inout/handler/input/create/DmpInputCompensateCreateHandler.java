package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpCfgInputCompensateEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgInputCompensateTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.service.DmpCfgInputCompensateService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建历史任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputCompensateCreateHandler extends DmpInputBaseCreateHandler{

	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpCfgInputCompensateService DmpCfgInputCompensateService;
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
				.eq(DmpCfgInputDetailEntity::getTaskType, DmpInputTaskTaskTypeEnum.NORMAL.getCode())
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的明细任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		Map<String, String> idNextLevelIdMap = dmpCfgInputDetailEntityList.stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getId, DmpCfgInputDetailEntity::getNextLevelId));
		List<DmpCfgInputCompensateEntity> dmpCfgInputCompensateEntityList = DmpCfgInputCompensateService.lambdaQuery()
				.in(DmpCfgInputCompensateEntity::getMainId, idNextLevelIdMap.keySet())
				.eq(DmpCfgInputCompensateEntity::getDisabled, Boolean.FALSE)
				.le(DmpCfgInputCompensateEntity::getNextTime, new Date())
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的补偿任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>();
		DmpInputTaskEntity dmpInputTaskEntity = null;
		for(DmpCfgInputCompensateEntity dmpCfgInputCompensateEntity : dmpCfgInputCompensateEntityList) {
			String type = dmpCfgInputCompensateEntity.getType();
			Integer intervalTime = dmpCfgInputCompensateEntity.getIntervalTime();
			if(DmpCfgInputCompensateTypeEnum.DAY.getCode().equals(type)) {
				
			}else if(DmpCfgInputCompensateTypeEnum.WEEK.getCode().equals(type)) {
				
			}else if(DmpCfgInputCompensateTypeEnum.MONTH.getCode().equals(type)) {
				
			}
			
			dmpInputTaskEntity = new DmpInputTaskEntity();
			dmpInputTaskEntity.setCfgInputId(cfgInputId);
			dmpInputTaskEntity.setNextLevelId(idNextLevelIdMap.get(dmpCfgInputCompensateEntity.getMainId()));
			/**
			currTime	        	overrideTime	StartTime				dealyTime	EndTime					LastTime				NextTime				IntervalTime
			2024-06-19 18:10:00		1800			2024-06-18 17:30:00		4*3600		2024-06-19 12:00:00		2024-06-19 12:00:00		2024-06-20 18:00:00		24*3600
			2024-06-20 18:10:00		1800			2024-06-19 11:30:00		4*3600		2024-06-20 12:00:00		2024-06-20 12:00:00		2024-06-21 18:00:00		24*3600
			2024-06-21 18:10:00		1800			2024-06-20 11:30:00		4*3600		2024-06-21 12:00:00		2024-06-21 12:00:00		2024-06-22 18:00:00		24*3600
			 */
			
			LocalDateTime startTime = LocalDateTime.now();
			LocalDateTime lastTime = dmpCfgInputCompensateEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			Integer overrideTime = dmpCfgInputCompensateEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1, ChronoUnit.SECONDS);
			}
			dmpInputTaskEntity.setStartTime(startTime);
			
			LocalDateTime nextTime = dmpCfgInputCompensateEntity.getNextTime();
			LocalDateTime endTime = nextTime;
			long dealyTime = Long.valueOf(type);
			endTime = LocalDateTimeUtil.offset(endTime, dealyTime * -1, ChronoUnit.SECONDS);
			
			dmpInputTaskEntity.setEndTime(endTime);
			dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
			dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.COMPENSATE.getCode());
			dmpInputTaskEntity.setExecTimeout(dmpCfgInputCompensateEntity.getExecTimeout());
			
			dmpInputTaskEntityList.add(dmpInputTaskEntity);
			
			dmpCfgInputCompensateEntity.setLastTime(endTime);
			dmpCfgInputCompensateEntity.setNextTime(LocalDateTimeUtil.offset(nextTime, intervalTime, ChronoUnit.SECONDS));
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		
		DmpCfgInputCompensateService.updateBatchById(dmpCfgInputCompensateEntityList);
		
		return dmpInputTaskEntityList;
	}

}
