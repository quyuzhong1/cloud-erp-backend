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

import cn.hutool.core.collection.CollUtil;

@Service
public class DmpInputTaskStatusHandler extends DmpInputHandler{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Autowired
	private DmpInputTaskStatusRecordService dmpInputTaskStatusRecordService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = dmpResponse.getBeforeDmpInputTaskEntityList();
		if(CollUtil.isNotEmpty(beforeDmpInputTaskEntityList)) {
			List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputTaskService.listByIds(beforeDmpInputTaskEntityList.stream().map(DmpInputTaskEntity::getId).collect(Collectors.toList()));
			dmpResponse.setAfterDmpInputTaskEntityList(afterDmpInputTaskEntityList);
			List<DmpInputTaskEntity> filterAfterDmpInputTaskEntityList = new ArrayList<>();
			
			LocalDateTime now = LocalDateTime.now();
			List<DmpInputTaskStatusRecordEntity> dmpInputTaskStatusrecordEntityList = new ArrayList<>();
			DmpInputTaskStatusRecordEntity dmpInputTaskStatusrecordEntity = null;
			if(this.validateDmpInputTaskEntityList(beforeDmpInputTaskEntityList)) {
				Map<String, String> statusbeforeEntityMaps = beforeDmpInputTaskEntityList.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId , DmpInputTaskEntity::getStatus));
				Map<String, DmpInputTaskEntity> idAfterEntityMaps = afterDmpInputTaskEntityList.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId , e -> e));
				for(Map.Entry<String, String> statusbeforeEntityMap : statusbeforeEntityMaps.entrySet()) {
					String id = statusbeforeEntityMap.getKey();
					String beforeStatus = statusbeforeEntityMap.getValue();
					DmpInputTaskEntity dmpInputTaskEntity = idAfterEntityMaps.get(id);
					String afterStatus = dmpInputTaskEntity.getStatus();
					List<DmpInputTaskStatusRecordEntity> list = dmpInputTaskStatusRecordService.lambdaQuery()
							.eq(DmpInputTaskStatusRecordEntity::getMainId, id)
							.eq(DmpInputTaskStatusRecordEntity::getStatus, beforeStatus)
							.list();
					if(CollUtil.isEmpty(list)) {
						dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusRecordEntity();
						dmpInputTaskStatusrecordEntity.setMainId(id);
						dmpInputTaskStatusrecordEntity.setStatus(beforeStatus);
						dmpInputTaskStatusrecordEntity.setCreateTime(now);
						dmpInputTaskStatusrecordEntity.setUpdateTime(now);
						dmpInputTaskStatusrecordEntityList.add(dmpInputTaskStatusrecordEntity);
					}else {
						dmpInputTaskStatusRecordService.lambdaUpdate()
								.eq(DmpInputTaskStatusRecordEntity::getMainId, id)
								.eq(DmpInputTaskStatusRecordEntity::getStatus, beforeStatus)
								.set(DmpInputTaskStatusRecordEntity::getUpdateTime, now)
								.update();
					}
					
					if(!beforeStatus.equals(afterStatus)) {
						filterAfterDmpInputTaskEntityList.add(dmpInputTaskEntity);
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
