package com.erp.server.dmp.inout.handler.input.task.finish;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputDbNextPageFinishHandler extends DmpInputBaseFinishHandler{
	
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	
	@Autowired
	@Qualifier("dmpInputDbNextPageFinishPool")
	private ExecutorService dmpInputDbNextPageFinishPool;
	
	@Override
	protected void afterToDoStatus(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		super.afterToDoStatus(dmpRequest, dmpResponse);
		String extendJson = dmpCfgInputEntity.getExtendJson();
		Integer limitCount = null;
		String orderBy = "";
		if(StringUtils.isNotBlank(extendJson)) {
			JSONObject parseObject = JSON.parseObject(extendJson);
			if(parseObject != null) {
				String limit = parseObject.getString("limit");
				if(StringUtils.isNotBlank(limit)) {
					limitCount = Integer.parseInt(limit);
				}
				orderBy = parseObject.getString("orderBy");
			}
		}
		if(limitCount == null || limitCount <= 0 || StringUtils.isBlank(orderBy)) {
			return;
		}
		DmpInputFinishResponse dmpInputFinishResponse = (DmpInputFinishResponse)dmpResponse;
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpInputFinishResponse.getConvertInputTaskInitDTOListMaps();
		if(CollUtil.isNotEmpty(convertInputTaskInitDTOListMaps)) {
			for(Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMap : convertInputTaskInitDTOListMaps.entrySet()) {
				List<DmpInputTaskInitDTO> value = convertInputTaskInitDTOListMap.getValue();
				if(CollUtil.isNotEmpty(value)) {
					int resultCount = 0;
					JSONObject lastObject = null;
					for(DmpInputTaskInitDTO v : value) {
						JSONArray parseArray = JSON.parseArray(v.getMsg());
						if(CollUtil.isNotEmpty(parseArray)) {
							int size = parseArray.size();
							lastObject = parseArray.getJSONObject(size - 1);
							resultCount = resultCount + size;
						}
					}
					if(resultCount > 0 && limitCount == resultCount) {
						String lastId = lastObject.getString(orderBy);
						if(StringUtils.isNotBlank(lastId)) {
							DmpInputTaskEntity nextDmpInputTaskEntity = BeanUtil.copyProperties(dmpInputTaskEntity, DmpInputTaskEntity.class);
							nextDmpInputTaskEntity.setId(null);
							String taskExtendJson = nextDmpInputTaskEntity.getExtendJson();
							JSONObject taksJsonObject = new JSONObject();
							if(StringUtils.isNotBlank(taskExtendJson)) {
								taksJsonObject = JSON.parseObject(taskExtendJson);
							}
							taksJsonObject.put("where", " " + orderBy + " >= '" + lastId + "' ");
							nextDmpInputTaskEntity.setExtendJson(taksJsonObject.toJSONString());
							nextDmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
							nextDmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
							nextDmpInputTaskEntity.setCreateTime(LocalDateTime.now());
							nextDmpInputTaskEntity.setUpdateTime(LocalDateTime.now());
							nextDmpInputTaskEntity.setErrorMessage("");
							nextDmpInputTaskEntity.setErrorCount(0);
							dmpInputTaskService.save(nextDmpInputTaskEntity);
							TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
							    @Override
							    public void afterCommit() {
							    	dmpInputDbNextPageFinishPool.execute(() -> {
							    		DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
										dmpInputFinishRequest.setInputTaskId(nextDmpInputTaskEntity.getId());
										dmpInputFinishRequest.setExecTimeout(nextDmpInputTaskEntity.getExecTimeout());
										dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
							    	});
							    }
							});
						}
					}
				}
				if(convertInputTaskInitDTOListMap.getKey() != null) {
					return;
				}
			}
		}
	}
}
