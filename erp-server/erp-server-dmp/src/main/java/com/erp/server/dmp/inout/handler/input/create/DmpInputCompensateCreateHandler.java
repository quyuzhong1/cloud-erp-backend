package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import cn.hutool.core.lang.Pair;
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
	private DmpCfgInputCompensateService dmpCfgInputCompensateService;
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
		List<DmpCfgInputCompensateEntity> dmpCfgInputCompensateEntityList = dmpCfgInputCompensateService.lambdaQuery()
				.in(DmpCfgInputCompensateEntity::getMainId, idNextLevelIdMap.keySet())
				.eq(DmpCfgInputCompensateEntity::getDisabled, Boolean.FALSE)
				.last(" and next_time <= NOW() - (INTERVAL '1 seconds' *  dealy_time) ")
				.list();
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的补偿任务" ,  dmpCfgInputEntity.getCode());
			return null;
		}
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>();
		DmpInputTaskEntity dmpInputTaskEntity = null;
		
		List<Pair<LocalDateTime, LocalDateTime>> timeList = null;
		for(DmpCfgInputCompensateEntity dmpCfgInputCompensateEntity : dmpCfgInputCompensateEntityList) {
			String type = dmpCfgInputCompensateEntity.getType();
			Integer intervalTime = dmpCfgInputCompensateEntity.getIntervalTime();
			LocalDateTime startTime = dmpCfgInputCompensateEntity.getLastTime();
			LocalDateTime endTime = dmpCfgInputCompensateEntity.getNextTime();
			timeList = new ArrayList<>();
			timeList.add(new Pair<>(startTime, endTime));
			if(intervalTime != null && intervalTime > 0) {
				long between = LocalDateTimeUtil.between(startTime, endTime , ChronoUnit.SECONDS);
				if(between > intervalTime) {
					timeList = new ArrayList<>();
					while(between > 0) {
						LocalDateTime offset = LocalDateTimeUtil.offset(startTime, intervalTime, ChronoUnit.SECONDS);
						if(offset.isAfter(endTime)) {
							offset = endTime;
						}
						timeList.add(new Pair<>(startTime, offset));
						startTime = offset;
						between = between - intervalTime;
					}
				}
			}
			
			for(Pair<LocalDateTime, LocalDateTime> time : timeList) {
				dmpInputTaskEntity = new DmpInputTaskEntity();
				dmpInputTaskEntity.setCfgInputId(cfgInputId);
				dmpInputTaskEntity.setNextLevelId(idNextLevelIdMap.get(dmpCfgInputCompensateEntity.getMainId()));
				
				dmpInputTaskEntity.setStartTime(time.getKey());
				dmpInputTaskEntity.setEndTime(time.getValue());
				dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
				dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.COMPENSATE.getCode());
				dmpInputTaskEntity.setExecTimeout(dmpCfgInputCompensateEntity.getExecTimeout());
				dmpInputTaskEntity.setExtendJson(dmpCfgInputCompensateEntity.getExtendJson());
				dmpInputTaskEntity.setExecSystem(dmpCfgInputEntity.getExecSystem());
				
				dmpInputTaskEntityList.add(dmpInputTaskEntity);
			}
			
			dmpCfgInputCompensateEntity.setLastTime(endTime);
			if(DmpCfgInputCompensateTypeEnum.DAY.getCode().equals(type)) {
				dmpCfgInputCompensateEntity.setNextTime(LocalDateTimeUtil.offset(endTime, 1, ChronoUnit.DAYS));
			}else if(DmpCfgInputCompensateTypeEnum.WEEK.getCode().equals(type)) {
				dmpCfgInputCompensateEntity.setNextTime(LocalDateTimeUtil.offset(endTime, 1, ChronoUnit.WEEKS));
			}else if(DmpCfgInputCompensateTypeEnum.MONTH.getCode().equals(type)) {
				dmpCfgInputCompensateEntity.setNextTime(LocalDateTimeUtil.offset(endTime, 1, ChronoUnit.MONTHS));
			}
		}
		if(CollUtil.isNotEmpty(dmpInputTaskEntityList)) {
			dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
		}
		if(CollUtil.isNotEmpty(dmpCfgInputCompensateEntityList)) {
			dmpCfgInputCompensateService.updateBatchById(dmpCfgInputCompensateEntityList);
		}
		
		return dmpInputTaskEntityList;
	}

}
