package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpOutputRocketMQPushUtils;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public abstract class DmpOutputRocketMQTaskHandler extends DmpOutputTaskHandler{

	@Autowired
	private DmpOutputRocketMQPushUtils dmpOutputRocketMQPushUtils;
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	private IdentifierGenerator identifierGenerator;
	
	@Override
	public List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		if(!dmpCfgOutputEntity.getType().equals(DmpCfgOutputTypeEnum.MQ.getCode())) {
			throw new ServiceException("非MQ输出类型，请勿配置DmpOutputRocketMQTaskHandler");
		}
		Map<String, String> pushJsonDataMap = this.getPushJsonDataMap(dmpRequest, dmpResponse);
		
		List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
		
		List<String> sourceCodeKeys = this.getSourceCodeKeys();
		for(Map.Entry<String, String> pushJsonData : pushJsonDataMap.entrySet()) {
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
			String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
			dmpOutputTaskRecordEntity.setId(id);
			dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
			String key = pushJsonData.getKey();
			dmpOutputTaskRecordEntity.setDataId(key);
			String value = pushJsonData.getValue();
			JSONObject parseObject = JSON.parseObject(value);
			parseObject.put("dmpOutputTaskRecordId", id);
			parseObject.put("dmpOutputTaskRecordDataId", key);
			if(CollUtil.isNotEmpty(sourceCodeKeys)) {
				dmpOutputTaskRecordEntity.setSourceCode(sourceCodeKeys.stream().map(s -> {
					String string = parseObject.getString(s);
					if(string == null) {
						string = "";
					}
					return string;
				}).collect(Collectors.joining("_")));
			}
			dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(parseObject));
			dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
			dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
		}
		
		if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
			dmpOutputTaskRecordService.saveBatch(dmpOutputTaskRecordEntityList);
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			    @Override
			    public void afterCommit() {
			    	dmpOutputRocketMQPushUtils.dealDmpOutputTaskRecordEntityList(dmpOutputTaskRecordEntityList);
			    }
			});
		}
		return dmpOutputTaskRecordEntityList;
	}
	
	
	public abstract Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse);
	
	protected List<String> getSourceCodeKeys() {
		return null;
	}
}
