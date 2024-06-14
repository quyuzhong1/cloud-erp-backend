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
import com.erp.model.dmp.entity.DmpInputTaskStatusrecordEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.erp.server.dmp.service.DmpInputTaskStatusrecordService;

import cn.hutool.core.collection.CollUtil;

@Service
public class DmpInputTaskStatusHandler extends DmpInputHandler{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Autowired
	private DmpInputTaskStatusrecordService dmpInputTaskStatusrecordService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		LocalDateTime now = LocalDateTime.now();
		List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = dmpResponse.getBeforeDmpInputTaskEntityList();
		List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputTaskService.listByIds(beforeDmpInputTaskEntityList.stream().map(DmpInputTaskEntity::getId).collect(Collectors.toList()));
		
		List<DmpInputTaskStatusrecordEntity> dmpInputTaskStatusrecordEntityList = new ArrayList<>();
		DmpInputTaskStatusrecordEntity dmpInputTaskStatusrecordEntity = null;
		if(this.validateDmpInputTaskEntityList(beforeDmpInputTaskEntityList)) {
			Map<String, String> statusbeforeEntityMaps = beforeDmpInputTaskEntityList.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId , DmpInputTaskEntity::getStatus));
			Map<String, String> statusAfterEntityMaps = afterDmpInputTaskEntityList.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId , DmpInputTaskEntity::getStatus));
			for(Map.Entry<String, String> statusbeforeEntityMap : statusbeforeEntityMaps.entrySet()) {
				String id = statusbeforeEntityMap.getKey();
				String beforeStatus = statusbeforeEntityMap.getValue();
				String afterStatus = statusAfterEntityMaps.get(id);
				List<DmpInputTaskStatusrecordEntity> list = dmpInputTaskStatusrecordService.lambdaQuery()
						.eq(DmpInputTaskStatusrecordEntity::getMainId, id)
						.eq(DmpInputTaskStatusrecordEntity::getStatus, beforeStatus)
						.list();
				if(CollUtil.isEmpty(list)) {
					dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusrecordEntity();
					dmpInputTaskStatusrecordEntity.setMainId(id);
					dmpInputTaskStatusrecordEntity.setStatus(beforeStatus);
					dmpInputTaskStatusrecordEntity.setCreateTime(now);
					dmpInputTaskStatusrecordEntity.setUpdateTime(now);
					dmpInputTaskStatusrecordEntityList.add(dmpInputTaskStatusrecordEntity);
				}else {
					dmpInputTaskStatusrecordService.lambdaUpdate()
							.eq(DmpInputTaskStatusrecordEntity::getMainId, id)
							.eq(DmpInputTaskStatusrecordEntity::getStatus, beforeStatus)
							.set(DmpInputTaskStatusrecordEntity::getUpdateTime, now)
							.update();
				}
				
				if(beforeStatus.equals(afterStatus)) {
					afterDmpInputTaskEntityList.removeIf(a -> a.getId().equals(id));
				}
			}
		}
		
		if(this.validateDmpInputTaskEntityList(afterDmpInputTaskEntityList)) {
			for(DmpInputTaskEntity afterDmpInputTaskEntity : afterDmpInputTaskEntityList) {
				dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusrecordEntity();
				dmpInputTaskStatusrecordEntity.setMainId(afterDmpInputTaskEntity.getId());
				dmpInputTaskStatusrecordEntity.setStatus(afterDmpInputTaskEntity.getStatus());
				dmpInputTaskStatusrecordEntity.setCreateTime(now);
				dmpInputTaskStatusrecordEntity.setUpdateTime(now);
				dmpInputTaskStatusrecordEntityList.add(dmpInputTaskStatusrecordEntity);
			}
		}
		if(CollUtil.isNotEmpty(dmpInputTaskStatusrecordEntityList)) {
			dmpInputTaskStatusrecordService.saveBatch(dmpInputTaskStatusrecordEntityList);
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
