package com.erp.server.dmp.inout.handler.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputHistoryCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputHotfixCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputInputCreateHandler;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputNormalCreateHandler;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpCfgOutputService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 输出任务创建工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Component
@Slf4j
public class DmpOutputCreateFactory{
	@Autowired
	private DmpOutputInputCreateHandler dmpOutputInputCreateHandler;
	@Autowired
	private DmpOutputHotfixCreateHandler dmpOutputHotfixCreateHandler;
	@Autowired
	private DmpOutputTaskFactory dmpOutputTaskFactory;
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	@Resource
	private DmpOutputNormalCreateHandler dmpOutputNormalCreateHandler;
	@Resource
	private DmpOutputHistoryCreateHandler dmpOutputHistoryCreateHandler;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	protected MongoService mongoService;
	
	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createInputOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputInputCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建输入任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doInputOutputTask(DmpOutputInputCreateRequest dmpRequest) {
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createInputOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = dmpRequest.getDmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}
	
	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createHotfixOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputHotfixCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建快速任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doHotfixOutputTask(DmpOutputHotfixCreateRequest dmpRequest) {
		dmpRequest.setThrowException(true);
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createHotfixOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = new DmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setNotValidate(dmpRequest.isNotValidate());
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		dmpOutputTaskRequest.setRetryPush(dmpRequest.isRetryPush());
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpOutputCreateResponse.getDmpCfgOutputEntity();
		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
		DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0);
		String inputStatus = dmpCfgInputConvertEntity.getInputStatus();
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache
				.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId()) && d.getInputStatus().equals(inputStatus));
		String extendJson = dmpCfgOutputEntity.getExtendJson();
		if(StringUtils.isNotBlank(extendJson)) {
			JSONObject extendJsonObject = JSON.parseObject(extendJson);
			String orders = extendJsonObject.getString("orders");
			if(StringUtils.isNotBlank(orders)) {
				List<Integer> orderList = Stream.of(orders.split(",")).map(Integer::valueOf).collect(Collectors.toList());
				dmpCfgInputConvertEntityList.removeIf(d -> !orderList.contains(d.getOrder()));
			}
		}
		dmpCfgInputConvertEntityList.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
		if(DmpInputTaskStatusEnum.FDS.getCode().equals(inputStatus)) {
			throw new ServiceException("推送fds数据未实现");
		}else if(DmpInputTaskStatusEnum.MONGO.getCode().equals(inputStatus)) {
			throw new ServiceException("推送mongo数据未实现");
		}else if(DmpInputTaskStatusEnum.DMP.getCode().equals(inputStatus)) {
			List<QueryParam> queryParams = dmpRequest.getQueryParams();
			QueryWrapper<?> queryWrapper = QueryParam.getQueryWrapper(queryParams);
			
			DmpCfgInputConvertEntity mainDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(mainDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
			List<BaseEntity> list = serviceImpl.list(queryWrapper);
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			if(CollUtil.isNotEmpty(list)) {
				String outputClass = dmpCfgOutputEntity.getOutputClass();
				DmpOutputTaskHandler dmpOutputTaskHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass) , DmpOutputTaskHandler.class);
				dmpOutputTaskHandler.getRetryPushSourceData(dmpCfgInputConvertEntityList, dmpOutputTaskRequest);
			}
		}
		
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}


	/**
	 * 创建正常任务
	 */
	public void createNormalOutputTask(DmpOutputCreateRequest dmpOutputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputNormalCreateHandler);
		bean.doDmpHandler(dmpOutputCreateRequest, new DmpOutputCreateResponse());
	}

	/**
	 * 创建历史任务
	 */
	public void createHistoryOutputTask(DmpOutputCreateRequest dmpOutputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputHistoryCreateHandler);
		bean.doDmpHandler(dmpOutputCreateRequest, new DmpOutputCreateResponse());
	}
	
	/**
	 * 获取查询同步数据
	 * @param dmpRequest
	 * @return
	 */
	public Map<String, String> getQueryPushData(DmpOutputHotfixCreateRequest dmpRequest) {
		dmpRequest.setThrowException(true);
		DmpOutputTaskRequest dmpOutputTaskRequest = new DmpOutputTaskRequest();
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpCfgOutputService.getById(dmpRequest.getCfgOutputId());
		
		DmpOutputTaskResponse dmpResponse = new DmpOutputTaskResponse();
		dmpResponse.setDmpCfgOutputEntity(dmpCfgOutputEntity);
		
		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
		DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0);
		String inputStatus = dmpCfgInputConvertEntity.getInputStatus();
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache
				.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId()) && d.getInputStatus().equals(inputStatus));
		String extendJson = dmpCfgOutputEntity.getExtendJson();
		if(StringUtils.isNotBlank(extendJson)) {
			JSONObject extendJsonObject = JSON.parseObject(extendJson);
			String orders = extendJsonObject.getString("orders");
			if(StringUtils.isNotBlank(orders)) {
				List<Integer> orderList = Stream.of(orders.split(",")).map(Integer::valueOf).collect(Collectors.toList());
				dmpCfgInputConvertEntityList.removeIf(d -> !orderList.contains(d.getOrder()));
			}
		}
		dmpCfgInputConvertEntityList.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
		List<QueryParam> queryParams = dmpRequest.getQueryParams();
		DmpCfgInputConvertEntity mainDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
		if(DmpInputTaskStatusEnum.FDS.getCode().equals(inputStatus)) {
			throw new ServiceException("推送fds数据未实现");
		}else if(DmpInputTaskStatusEnum.MONGO.getCode().equals(inputStatus)) {
			List<ParamData> paramDataList = new ArrayList<>();
			if(CollUtil.isNotEmpty(queryParams)) {
				ParamData paramData = null;
				EnumMap<QueryTypeEnum, PannoEnum> dbSignMap = new EnumMap<>(QueryTypeEnum.class);
				dbSignMap.put(QueryTypeEnum.EQ, PannoEnum.EQ);
				dbSignMap.put(QueryTypeEnum.LIKE, PannoEnum.LIKE);
				dbSignMap.put(QueryTypeEnum.IN, PannoEnum.IN);
				dbSignMap.put(QueryTypeEnum.LT, PannoEnum.LT);
				dbSignMap.put(QueryTypeEnum.LE, PannoEnum.LTE);
				dbSignMap.put(QueryTypeEnum.GT, PannoEnum.GT);
				dbSignMap.put(QueryTypeEnum.GE, PannoEnum.GTE);
				for(QueryParam queryParam : queryParams) {
					String name = queryParam.getName();
					QueryTypeEnum type = queryParam.getType();
					PannoEnum pannoEnum = dbSignMap.get(type);
					if(pannoEnum == null) {
						throw new ServiceException("mongo运算符不支持");
					}
					Object value = queryParam.getValue();
					if(value != null) {
						paramData = new ParamData(name, name, pannoEnum, value);
					}
					List<Object> values = queryParam.getValues();
					if(CollUtil.isNotEmpty(values)) {
						paramData = new ParamData(name, name, pannoEnum, values);
					}
					if(paramData == null) {
						throw new ServiceException("转换mongo查询条件失败");
					}
					paramDataList.add(paramData);
				}
				DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(mainDmpCfgInputConvertEntity.getId())).get(0);
				DmpBasicSystemEntity dmpBasicSystemEntity = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(dmpCfgInputEntity.getId())).get(0);
				List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputEntity
						, mainDmpCfgInputConvertEntity));
				dmpOutputTaskRequest.getConvertInputMongoEntityListMaps().put(mainDmpCfgInputConvertEntity, findMongoData);
				dmpOutputTaskRequest.getChangeConvertInputMongoEntityListMaps().put(mainDmpCfgInputConvertEntity, findMongoData);
			}
		}else if(DmpInputTaskStatusEnum.DMP.getCode().equals(inputStatus)) {
			List<BaseEntity> list = null;
			if(StringUtils.isNotBlank(extendJson)) {
				JSONObject parseObject = JSON.parseObject(extendJson);
				String tableName = parseObject.getString("tableName");
				if(StringUtils.isNotBlank(tableName)) {
					ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(tableName, true) + "ServiceImpl" , ServiceImpl.class);
					list = serviceImpl.list(QueryParam.getQueryWrapper(queryParams));
					if(CollUtil.isEmpty(list)) {
						return null;
					}
					String parentPropertie = parseObject.getString("parentPropertie");
					if(StringUtils.isBlank(parentPropertie)) {
						parentPropertie = "id";
					}
					String childPropertie = parseObject.getString("childPropertie");
					if(StringUtils.isBlank(childPropertie)) {
						childPropertie = "mainId";
					}
					queryParams = new ArrayList<>();
					List<Object> values = new ArrayList<>();
					for(BaseEntity l : list) {
						values.add(BeanUtil.beanToMap(l, childPropertie).get(childPropertie));
					}
					queryParams.add(new QueryParam(QueryTypeEnum.IN, parentPropertie, values));
				}
			}
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(mainDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
			list = serviceImpl.list(QueryParam.getQueryWrapper(queryParams));
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
		}
		String outputClass = dmpCfgOutputEntity.getOutputClass();
		DmpOutputTaskHandler dmpOutputTaskHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass) , DmpOutputTaskHandler.class);
		dmpOutputTaskHandler.getRetryPushSourceData(dmpCfgInputConvertEntityList, dmpOutputTaskRequest);
		return dmpOutputTaskHandler.getPushJsonDataMap(dmpOutputTaskRequest, dmpResponse);
	}
}
