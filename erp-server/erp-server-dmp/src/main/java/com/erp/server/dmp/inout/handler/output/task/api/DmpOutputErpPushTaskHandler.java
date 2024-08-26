package com.erp.server.dmp.inout.handler.output.task.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputErpPushTaskHandler extends DmpOutputTaskHandler{

	@Override
	public List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		if(!dmpCfgOutputEntity.getType().equals(DmpCfgOutputTypeEnum.API.getCode())) {
			throw new ServiceException("非api输出类型，请勿配置DmpOutputErpPushTaskHandler");
		}
		List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
		
		String cfgOutputId = dmpCfgOutputEntity.getId();
		String mainId = dmpResponse.getDmpCfgInputConvertEntity().getMainId();
		DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(mainId)).get(0);
		String typeId = dmpCfgInputEntity.getTypeId();
		String apiType = dmpHandlerCache.getDmpCfgApiEntityList(d -> d.getId().equals(typeId)).get(0).getApiType();
		
		String systemId = dmpCfgInputEntity.getSystemId();
		String code = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(systemId)).get(0).getCode();
		
		List<DmpPushMsgEntity> dmpPushMsgEntityList = new ArrayList<>();
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				for(BaseEntity v : value) {
					DmpPushMsgEntity dmpPushMsgEntity = (DmpPushMsgEntity) v;
					if(!dmpPushMsgEntity.getTargetPlatform().equals(code)) {
						continue;
					}
					if(!dmpPushMsgEntity.getSourceType().equals(apiType)) {
						continue;
					}
					if(this.validateDataBlack(dmpPushMsgEntity, cfgOutputId)) {
						continue;
					}
					dmpPushMsgEntityList.add(dmpPushMsgEntity);
				}
			}
		}
		
		if(CollUtil.isNotEmpty(dmpPushMsgEntityList)) {
			dmpPushMsgEntityList.sort((d1 , d2) -> d1.getMessageUpdateTime().compareTo(d2.getMessageUpdateTime()));
			LocalDateTime now = LocalDateTime.now();
			int i = 0;
			for(DmpPushMsgEntity dmpPushMsgEntity : dmpPushMsgEntityList) {
				String dataId = dmpPushMsgEntity.getId();
				String pushData = dmpPushMsgEntity.getPushData();
				DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
				String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
				dmpOutputTaskRecordEntity.setId(id);
				dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
				dmpOutputTaskRecordEntity.setDataId(dataId);
				dmpOutputTaskRecordEntity.setSourceCode(dmpPushMsgEntity.getSourceCode());
				dmpOutputTaskRecordEntity.setRequestData(pushData);
				dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
				LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
				dmpOutputTaskRecordEntity.setCreateTime(insertTime);
				dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
				dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
				i = i + 1;
			}
			dmpOutputTaskRecordService.saveBatch(dmpOutputTaskRecordEntityList);
		}
		
		return dmpOutputTaskRecordEntityList;
	}

	@Override
	protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity,
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		String id = dmpOutputTaskRecordEntity.getId();
		String outputTypeId = dmpCfgOutputEntity.getTypeId();
		DmpCfgApiEntity outputDmpCfgApiEntity = dmpHandlerCache.getDmpCfgApiEntityList(d -> d.getId().equals(outputTypeId)).get(0);
		String apiClass = outputDmpCfgApiEntity.getApiClass();
		Object bean = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(apiClass));
		String outputMethod = outputDmpCfgApiEntity.getApiType();
		Method method = null;
		
		String status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
		String responseData = "";
		String message = "";
		try {
			method = bean.getClass().getMethod(outputMethod, Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
			responseData = "获取" + apiClass + "的" + outputMethod + "方法报错" + ExceptionUtil.stacktraceToOneLineString(e);
			message = "获取" + apiClass + "的" + outputMethod + "方法报错";
		}
		try {
			Object invoke = method.invoke(bean, dmpOutputTaskRecordEntity.getRequestData());
			try {responseData = JSON.toJSONString(invoke);} catch (Exception e) {}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
			responseData = "调用" + apiClass + "的" + outputMethod + "方法报错" + ExceptionUtil.stacktraceToOneLineString(e);
			message = "调用" + apiClass + "的" + outputMethod + "方法报错";
		}
		dmpOutputUtils.updateStatus(id, status, responseData , message);
	}

	
}
