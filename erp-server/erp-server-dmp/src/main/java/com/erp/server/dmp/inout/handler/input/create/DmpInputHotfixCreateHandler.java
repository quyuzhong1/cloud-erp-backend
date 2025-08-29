package com.erp.server.dmp.inout.handler.input.create;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import cn.hutool.core.lang.Pair;
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
				.eq(DmpCfgInputDetailEntity::getTaskType, DmpInputTaskTaskTypeEnum.NORMAL.getCode())
				.in(CollUtil.isNotEmpty(cfgInputDetailIdList) , DmpCfgInputDetailEntity::getId, cfgInputDetailIdList)
				.list();
		String msg = "";
		if(CollUtil.isEmpty(dmpCfgInputDetailEntityList)) {
			msg = "输入信息数据代码【"+ dmpCfgInputEntity.getCode() +"】没有符合条件的明细任务";
			log.warn(msg);
			throw new ServiceException(msg);
		}
	
		List<DmpInputTaskEntity> dmpInputTaskEntityList = new ArrayList<>(dmpCfgInputDetailEntityList.size());

		DmpInputTaskEntity dmpInputTaskEntity = null;
		LocalDateTime startTime = dmpInputHotfixCreateRequest.getStartTime();
		LocalDateTime endTime = dmpInputHotfixCreateRequest.getEndTime();
		boolean splitFlag = dmpInputHotfixCreateRequest.isSplitFlag() && (startTime != null && endTime != null && endTime.isAfter(startTime));
		List<Pair<LocalDateTime, LocalDateTime>> timeList = null;
		for(DmpCfgInputDetailEntity dmpCfgInputDetailEntity : dmpCfgInputDetailEntityList) {
			timeList = new ArrayList<>();
			timeList.add(new Pair<>(startTime, endTime));
			if(splitFlag) {
				Integer intervalTime = dmpCfgInputDetailEntity.getIntervalTime();
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
			}
			
			for(Pair<LocalDateTime, LocalDateTime> time : timeList) {
				dmpInputTaskEntity = new DmpInputTaskEntity();
				dmpInputTaskEntity.setCfgInputId(cfgInputId);
				dmpInputTaskEntity.setNextLevelId(dmpCfgInputDetailEntity.getNextLevelId());
				
				dmpInputTaskEntity.setStartTime(time.getKey());
				dmpInputTaskEntity.setEndTime(time.getValue());
				dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
				dmpInputTaskEntity.setTaskType(dmpInputHotfixCreateRequest.checkAndGetTaskType());
				dmpInputTaskEntity.setExecSystem(dmpCfgInputEntity.getExecSystem());
				dmpInputTaskEntity.setExecTimeout(dmpInputHotfixCreateRequest.getExecTimeout());
				// 请求参数明细ExtendJson覆盖
				String detailExtendJson = dmpCfgInputDetailEntity.getExtendJson();
				if (StringUtils.isNotBlank(dmpInputHotfixCreateRequest.getDetailExtendJson())){
					detailExtendJson = dmpInputHotfixCreateRequest.getDetailExtendJson();
				}
				dmpInputTaskEntity.setExtendJson(detailExtendJson);
				dmpInputTaskEntity.setNextExecTime(dmpInputHotfixCreateRequest.getNextExecTime());
				
				dmpInputTaskEntityList.add(dmpInputTaskEntity);
			}
		}
		dmpInputTaskService.saveBatch(dmpInputTaskEntityList);
	
		
		return dmpInputTaskEntityList;
	}

}
