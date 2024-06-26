package com.erp.server.dmp.inout.handler.input.all;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskStatusRecordEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.erp.server.dmp.service.DmpInputTaskStatusRecordService;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollUtil;

/**
 * 记录任务状态handler，主要操作com.erp.model.dmp.entity.DmpInputTaskStatusRecordEntity，通用型handler
 * @author Administrator
 *
 */
@Service
public class DmpInputTaskStatusHandler extends DmpInputHandler{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Autowired
	private DmpInputTaskStatusRecordService dmpInputTaskStatusRecordService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		List<DmpInputTaskEntity> beforeDmpInputTaskEntityListAll = dmpResponse.getBeforeDmpInputTaskEntityList();
		if(CollUtil.isNotEmpty(beforeDmpInputTaskEntityListAll)) {
			List<List<DmpInputTaskEntity>> partition = Lists.partition(beforeDmpInputTaskEntityListAll, 10000);
			for(List<DmpInputTaskEntity> beforeDmpInputTaskEntityList : partition) {
				List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputTaskService.listByIds(beforeDmpInputTaskEntityList.stream().map(DmpInputTaskEntity::getId).collect(Collectors.toList()));
				dmpResponse.setAfterDmpInputTaskEntityList(afterDmpInputTaskEntityList);
				List<DmpInputTaskEntity> filterAfterDmpInputTaskEntityList = new ArrayList<>();
				
				LocalDateTime now = LocalDateTime.now();
				List<DmpInputTaskStatusRecordEntity> dmpInputTaskStatusrecordEntityList = new ArrayList<>();
				DmpInputTaskStatusRecordEntity dmpInputTaskStatusrecordEntity = null;
				if(this.validateDmpInputTaskEntityList(beforeDmpInputTaskEntityList)) {
					Map<String, List<DmpInputTaskEntity>> statusGroupMap = beforeDmpInputTaskEntityList.stream().collect(Collectors.groupingBy(DmpInputTaskEntity::getStatus));
					for(Map.Entry<String, List<DmpInputTaskEntity>> statusGroup : statusGroupMap.entrySet()) {
						List<String> idList = statusGroup.getValue().stream().map(DmpInputTaskEntity::getId).collect(Collectors.toList());
						String beforeStatus = statusGroup.getKey();
						Map<String, DmpInputTaskEntity> idAfterEntityMaps = afterDmpInputTaskEntityList.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId , e -> e));
						List<String> list = dmpInputTaskStatusRecordService.lambdaQuery()
								.in(DmpInputTaskStatusRecordEntity::getMainId, idList)
								.eq(DmpInputTaskStatusRecordEntity::getStatus, beforeStatus)
								.list().stream().map(DmpInputTaskStatusRecordEntity::getMainId).collect(Collectors.toList());
						List<String> updateIdList = new ArrayList<>();
						for(String id : idList) {
							DmpInputTaskEntity dmpInputTaskEntity = idAfterEntityMaps.get(id);
							String afterStatus = dmpInputTaskEntity.getStatus();
							if(!list.contains(id)) {
								dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusRecordEntity();
								dmpInputTaskStatusrecordEntity.setMainId(id);
								dmpInputTaskStatusrecordEntity.setStatus(beforeStatus);
								dmpInputTaskStatusrecordEntity.setCreateTime(now);
								dmpInputTaskStatusrecordEntity.setUpdateTime(now);
								dmpInputTaskStatusrecordEntityList.add(dmpInputTaskStatusrecordEntity);
							}else {
								updateIdList.add(id);
							}
							if(!beforeStatus.equals(afterStatus)) {
								filterAfterDmpInputTaskEntityList.add(dmpInputTaskEntity);
							}
						}
						if(CollUtil.isNotEmpty(updateIdList)) {
							dmpInputTaskStatusRecordService.lambdaUpdate()
								.in(DmpInputTaskStatusRecordEntity::getMainId, updateIdList)
								.eq(DmpInputTaskStatusRecordEntity::getStatus, beforeStatus)
								.set(DmpInputTaskStatusRecordEntity::getUpdateTime, now)
								.update();
						}
					}
				}
				
				if(this.validateDmpInputTaskEntityList(filterAfterDmpInputTaskEntityList)) {
					for(DmpInputTaskEntity afterDmpInputTaskEntity : afterDmpInputTaskEntityList) {
						dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusRecordEntity();
						dmpInputTaskStatusrecordEntity.setMainId(afterDmpInputTaskEntity.getId());
						dmpInputTaskStatusrecordEntity.setStatus(afterDmpInputTaskEntity.getStatus());
						dmpInputTaskStatusrecordEntity.setCreateTime(now);
						dmpInputTaskStatusrecordEntity.setUpdateTime(now);
						dmpInputTaskStatusrecordEntityList.add(dmpInputTaskStatusrecordEntity);
					}
				}
				if(CollUtil.isNotEmpty(dmpInputTaskStatusrecordEntityList)) {
					dmpInputTaskStatusRecordService.saveBatch(dmpInputTaskStatusrecordEntityList);
				}
			}
		}
		
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	private boolean validateDmpInputTaskEntityList(List<DmpInputTaskEntity> dmpInputTaskEntityList) {
		if(CollUtil.isEmpty(dmpInputTaskEntityList)) {
			return false;
		}
		dmpInputTaskEntityList.removeIf(d -> {
			if(d == null) {
				return true;
			}
			if(StringUtils.isBlank(d.getId())) {
				return true;
			}
			if(StringUtils.isBlank(d.getStatus())) {
				return true;
			}
			return false;
		});
		return CollUtil.isNotEmpty(dmpInputTaskEntityList);
	}
}
