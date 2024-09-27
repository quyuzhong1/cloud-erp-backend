package com.erp.server.dmp.inout.handler.output.task.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpPushMsgService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputErpPushTaskHandler extends DmpOutputTaskHandler{

	@Autowired
	private DmpPushMsgService dmpPushMsgService;
	
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
		List<String> apiTypeList = Arrays.asList(apiType.split(","));
		
		String systemId = dmpCfgOutputEntity.getSystemId();
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
					if(!apiTypeList.contains(dmpPushMsgEntity.getSourceType())) {
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
		}
		
		return dmpOutputTaskRecordEntityList;
	}

	@Override
	protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity,
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		String dataId = dmpOutputTaskRecordEntity.getDataId();
		DmpPushMsgEntity dmpPushMsgEntity = dmpPushMsgService.getById(dataId);
		
		String systemCode = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(dmpCfgOutputEntity.getSystemId())).get(0).getCode();
		String sourceId = dmpPushMsgEntity.getSourceId();
		List<DmpPushMsgEntity> sourceList = dmpPushMsgService.lambdaQuery()
				.eq(DmpPushMsgEntity::getSourceId, sourceId)
				.eq(DmpPushMsgEntity::getTargetPlatform, systemCode)
				.le(DmpPushMsgEntity::getMessageUpdateTime, dmpPushMsgEntity.getMessageUpdateTime())
				.ne(DmpPushMsgEntity::getId, dataId)
				.select(DmpPushMsgEntity::getId)
				.list();
		if(CollUtil.isNotEmpty(sourceList)) {
			Integer count = dmpOutputTaskRecordService.lambdaQuery()
					.in(DmpOutputTaskRecordEntity::getDataId, sourceList.stream().map(DmpPushMsgEntity::getId).collect(Collectors.toList()))
					.ne(DmpOutputTaskRecordEntity::getId , dmpOutputTaskRecordEntity.getId())
					.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.count();
			if(count > 0) {
				dmpOutputTaskRecordService.lambdaUpdate()
					.set(DmpOutputTaskRecordEntity::getResponseData, "单据上一步操作未推送成功")
					.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
					.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
					.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.update();
				return;
			}
		}
		
		String parentId = dmpPushMsgEntity.getParentId();
		String requestData = dmpOutputTaskRecordEntity.getRequestData();
		if(StringUtils.isNotBlank(parentId) && "operateApprove".equals(dmpPushMsgEntity.getSyncOperate())) {
			String[] split = parentId.split(",");
			for(String s : split) {
				List<DmpPushMsgEntity> list = dmpPushMsgService.lambdaQuery()
						.eq(DmpPushMsgEntity::getSourceId, s)
						.eq(DmpPushMsgEntity::getTargetPlatform, systemCode)
						.eq(DmpPushMsgEntity::getSyncOperate, dmpPushMsgEntity.getSyncOperate())
						.orderByDesc(DmpPushMsgEntity::getMessageUpdateTime)
						.list();
				if(CollUtil.isEmpty(list)) {
					try {
						JSONObject parseObject = JSON.parseObject(requestData);
						String poSyncKingdeeId = parseObject.getString("poSyncKingdeeId");
						if(StringUtils.isNotBlank(poSyncKingdeeId)) {
							break;
						}
						String soKingdeeDetailIds = parseObject.getString("soKingdeeDetailIds");
						if(StringUtils.isNotBlank(soKingdeeDetailIds)) {
							break;
						}
						String code = parseObject.getString("code");
						if(StringUtils.isNotBlank(code) && code.startsWith("CGTJ")) {
							JSONArray jsonArray = parseObject.getJSONArray("list");
							if(CollUtil.isNotEmpty(jsonArray)) {
								if(jsonArray.stream().allMatch(j -> {
									JSONObject JSONObject = (JSONObject)j;
									String kingdeeDetailId = JSONObject.getString("kingdeeDetailId");
									return StringUtils.isNotBlank(kingdeeDetailId);
								})) {
									break;
								}
							}
						}
					} catch (Exception e) {
					}
					dmpOutputTaskRecordService.lambdaUpdate()
						.set(DmpOutputTaskRecordEntity::getResponseData, "上游单据未拉取到")
						.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
						.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
						.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
						.update();
					return;
				}
				
				List<DmpOutputTaskRecordEntity> parentOutputList = dmpOutputTaskRecordService.lambdaQuery()
						.in(DmpOutputTaskRecordEntity::getDataId, list.stream().map(DmpPushMsgEntity::getId).collect(Collectors.toList()))
						.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
						.list();
				if(CollUtil.isNotEmpty(parentOutputList)) {
					dmpOutputTaskRecordService.lambdaUpdate()
						.set(DmpOutputTaskRecordEntity::getResponseData, "上游单据未推送成功")
						.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
						.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
						.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
						.update();
					return;
				}
			}
			List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, Arrays.asList(dmpOutputTaskRecordEntity));
			if(CollUtil.isNotEmpty(erpQuerySync)) {
				requestData = erpQuerySync.get(0).getRequestData();
			}else {
				return;
			}
		}
		
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
		if(StringUtils.isNotBlank(requestData) && !"null".equals(requestData)) {
			try {
				method = bean.getClass().getMethod(outputMethod, Object.class);
			} catch (NoSuchMethodException | SecurityException e) {
				status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
				responseData = "获取" + apiClass + "的" + outputMethod + "方法报错" + ExceptionUtil.stacktraceToOneLineString(e);
				message = "获取" + apiClass + "的" + outputMethod + "方法报错";
			}
			try {
				Object invoke = method.invoke(bean, requestData);
				if(invoke instanceof ApiResult) {
					ApiResult apiResult = (ApiResult)invoke;
					if(!apiResult.isSuccess()) {
						status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
						responseData = apiResult.getMsg();
//						if(systemCode.equals(DmpBasicSystemCodeEnum.WDT.getCode()) && responseData != null && responseData.startsWith("单据推送成功，当前状态：")) {
//							dmpOutputTaskRecordService.lambdaUpdate()
//								.eq(DmpOutputTaskRecordEntity::getId, id)
//								.set(DmpOutputTaskRecordEntity::getResponseData, responseData)
//								.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
//								.update();
//							return;
//						}
					}
				}
				try {responseData = JSON.toJSONString(invoke);} catch (Exception e) {}
			} catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
				responseData = "调用" + apiClass + "的" + outputMethod + "方法报错" + ExceptionUtil.stacktraceToOneLineString(targetException);
				message = "调用" + apiClass + "的" + outputMethod + "方法报错" + targetException.getMessage();
			} catch (IllegalAccessException | IllegalArgumentException e) {
				status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
				responseData = "调用" + apiClass + "的" + outputMethod + "方法报错" + ExceptionUtil.stacktraceToOneLineString(e);
				message = "调用" + apiClass + "的" + outputMethod + "方法报错";
			}
			if(!status.equals(DmpOutputTaskRecordStatusEnum.FINISH.getCode())) {
				responseData = "traceId=【" + MDC.get("traceId") + "】" + responseData;
			}
		}
		dmpOutputUtils.updateStatus(id, status, responseData , message);
	}

	@Override
	protected List<String> getSourceCodeKeys() {
		return Arrays.asList("sourceCode");
	}
	
}
