package com.erp.server.dmp.inout.handler.etl.create;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.model.dmp.enums.DmpEtlTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpEtlCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpEtlCreateResponse;
import com.erp.server.dmp.service.DmpCfgEtlService;
import com.erp.server.dmp.service.DmpEtlTaskService;

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
public class DmpEtlCreateHandler extends DmpEtlBaseCreateHandler{

	@Autowired
	private DmpCfgEtlService dmpCfgEtlService;
	
	@Autowired
	private DmpEtlTaskService dmpEtlTaskService;
	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpEtlTaskEntity> createEtlTask(DmpEtlCreateRequest dmpRequest, DmpEtlCreateResponse dmpResponse) {
		DmpCfgEtlEntity dmpCfgEtlEntity = dmpResponse.getDmpCfgEtlEntity();
		String cfgEtlId = dmpCfgEtlEntity.getId();
		
		
		List<DmpCfgEtlEntity> dmpCfgEtlEntityList = dmpCfgEtlService.lambdaQuery()
				.eq(DmpCfgEtlEntity::getId, cfgEtlId)
				.eq(DmpCfgEtlEntity::getDisabled, Boolean.FALSE)
				.last(" and next_time <= NOW() - (INTERVAL '1 seconds' *  dealy_time) ")
				.list();
		if(CollUtil.isEmpty(dmpCfgEtlEntityList)) {
			log.info("输入信息数据代码【{}】没有符合条件的任务" ,  dmpCfgEtlEntity.getProcessCode());
			return null;
		}
		
		Set<String> cfgEtlIdSet = dmpEtlTaskService.lambdaQuery()
				.eq(DmpEtlTaskEntity::getCfgEtlId, cfgEtlId)
				.ne(DmpEtlTaskEntity::getStatus, DmpEtlTaskStatusEnum.ERROR.getCode())
				.ne(DmpEtlTaskEntity::getStatus, DmpEtlTaskStatusEnum.FINISH.getCode())
				.select(DmpEtlTaskEntity::getCfgEtlId)
				.list().stream().map(DmpEtlTaskEntity::getCfgEtlId).collect(Collectors.toSet());
		if(CollUtil.isNotEmpty(cfgEtlIdSet)) {
			Iterator<DmpCfgEtlEntity> iterator = dmpCfgEtlEntityList.iterator();
			while(iterator.hasNext()) {
				String id = iterator.next().getId();
				if(cfgEtlIdSet.contains(id)) {
					iterator.remove();
					log.info("ETL信息代码【{}】，名称【{}】下，还有正在执行中的{}，此次不生成任务" ,  dmpCfgEtlEntity.getProcessCode() , dmpCfgEtlEntity.getProcessName());
				}
			}
		}
		
		List<DmpEtlTaskEntity> dmpEtlTaskEntityList = new ArrayList<>(dmpCfgEtlEntityList.size());
		DmpEtlTaskEntity dmpEtlTaskEntity = null;
		for(DmpCfgEtlEntity dbDmpCfgEtlEntity : dmpCfgEtlEntityList) {
			dmpEtlTaskEntity = new DmpEtlTaskEntity();
			dmpEtlTaskEntity.setCfgEtlId(dbDmpCfgEtlEntity.getId());
			/**
			currTime	        	overrideTime	StartTime				dealyTime	EndTime					LastTime				NextTime				IntervalTime
			2024-06-19 18:10:00		1800			2024-06-18 17:30:00		4*3600		2024-06-19 12:00:00		2024-06-19 12:00:00		2024-06-20 18:00:00		24*3600
			2024-06-20 18:10:00		1800			2024-06-19 11:30:00		4*3600		2024-06-20 12:00:00		2024-06-20 12:00:00		2024-06-21 18:00:00		24*3600
			2024-06-21 18:10:00		1800			2024-06-20 11:30:00		4*3600		2024-06-21 12:00:00		2024-06-21 12:00:00		2024-06-22 18:00:00		24*3600
			 */
			// 2024-06-19 18:10:00
			LocalDateTime startTime = LocalDateTime.now();
			// 2024-06-18 18:00:00
			LocalDateTime lastTime = dbDmpCfgEtlEntity.getLastTime();
			if(lastTime != null) {
				startTime = lastTime;
			}
			// 1800
			Integer overrideTime = dbDmpCfgEtlEntity.getOverrideTime();
			if(overrideTime != null && overrideTime != 0) {
				startTime = LocalDateTimeUtil.offset(lastTime, overrideTime * -1, ChronoUnit.SECONDS);
			}
			// 2024-06-18 17:30:00
			dmpEtlTaskEntity.setStartTime(startTime);
			
			// 2024-06-19 18:00:00
			LocalDateTime endTime = dbDmpCfgEtlEntity.getNextTime();
			// 正常任务最大允许拉取的时间获取
			endTime = checkAndGetMaxFetchTime(endTime, dbDmpCfgEtlEntity);

			// 2024-06-19 12:00:00
			dmpEtlTaskEntity.setEndTime(endTime);
			dmpEtlTaskEntity.setStatus(DmpEtlTaskStatusEnum.INIT.getCode());
			dmpEtlTaskEntity.setExecTimeout(dbDmpCfgEtlEntity.getExecTimeout());
			dmpEtlTaskEntity.setExtendJson(dbDmpCfgEtlEntity.getExtendJson());
			
			dmpEtlTaskEntityList.add(dmpEtlTaskEntity);
			
			// 2024-06-19 12:00:00
			dbDmpCfgEtlEntity.setLastTime(endTime);
			
			// 2024-06-20 18:00:00
			dbDmpCfgEtlEntity.setNextTime(LocalDateTimeUtil.offset(endTime, dbDmpCfgEtlEntity.getIntervalTime(), ChronoUnit.SECONDS));
		}
		dmpEtlTaskService.saveBatch(dmpEtlTaskEntityList);
		
		dmpCfgEtlService.updateBatchById(dmpCfgEtlEntityList);
		
		return dmpEtlTaskEntityList;
	}

	/**
	 * 检查最大拉取时间
	 */
	protected LocalDateTime checkAndGetMaxFetchTime(LocalDateTime curEndTime, DmpCfgEtlEntity dbDmpCfgEtlEntity) {
		Integer maxIntervalTime = dbDmpCfgEtlEntity.getMaxIntervalTime();
		if (null == maxIntervalTime || 0 == maxIntervalTime || null == dbDmpCfgEtlEntity.getLastTime()){
			return curEndTime;
		}
		// 小于按当前时间获取
		if (0 > maxIntervalTime){
			// 全量拉取按当前时间-延迟时间
			return LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(dbDmpCfgEtlEntity.getDealyTime());
		}

		// 配置允许的最大时间
		LocalDateTime cfgMaxDateTime = dbDmpCfgEtlEntity.getLastTime().plusSeconds(dbDmpCfgEtlEntity.getMaxIntervalTime());
		// 当前时间-延时时间
		LocalDateTime nowDelayTime = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(dbDmpCfgEtlEntity.getDealyTime());
		// 和当前时间对比
		LocalDateTime allowMaxDateTime = cfgMaxDateTime.isAfter(nowDelayTime) ? nowDelayTime : cfgMaxDateTime;

		// 比较取最大时间
		return allowMaxDateTime.isAfter(curEndTime) ? allowMaxDateTime : curEndTime;
	}

}
