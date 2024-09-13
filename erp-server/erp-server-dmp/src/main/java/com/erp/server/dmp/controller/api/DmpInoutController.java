package com.erp.server.dmp.controller.api;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.common.business.dto.base.BaseIdsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.erp.server.dmp.service.DmpPushMsgService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 输入输出任务
 * @author Administrator
 *
 */
@Slf4j
@RestController
@RequestMapping("/dmpInout")
public class DmpInoutController extends BaseController {

	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@Autowired
	private DmpHandlerCache dmpHandlerCache;

	@Autowired
	private DmpCfgInputConvertService dmpCfgInputConvertService;
	
	@Autowired
	private DmpCfgMqService dmpCfgMqService;
	
	@Autowired
	private DmpOutputCreateFactory dmpOutputCreateFactory;

	@Autowired
	private DmpCfgInputConvertMappingService dmpCfgInputConvertMappingService;
	
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	
    @PostMapping("doInputTask")
    public ApiResult<?> doInputTask(@RequestBody DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
    	return success(dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest));
    }
    
    @PostMapping("doOutputTask")
    public ApiResult<?> doOutputTask(@RequestBody DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest) {
    	List<QueryParam> queryParams = dmpOutputHotfixCreateRequest.getQueryParams();
    	if(CollUtil.isEmpty(queryParams)) {
    		throw new ServiceException("过滤条件queryParams不能为空");
    	}
    	return success(dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest));
    }
    
    @PostMapping("getCache")
    public ApiResult<?> getCache() {
    	Map<String, Object> typeCacheMap = new HashMap<>();
    	typeCacheMap.put("dmpBasicSystem", dmpHandlerCache.getDmpBasicSystemEntityList(d -> true));
		typeCacheMap.put("dmpCfgInputConvert", dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> true));
		
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getDisabled, false).list();


		Map<String, Map<String, List<String>>> convertMappingCache = new HashMap<>();
    	for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
    		String id = dmpCfgInputConvertEntity.getId();
    		convertMappingCache.put(id, dmpHandlerCache.getDmpCfgInputConvertMapping(id));

    	}

		Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> convertValueCache = new HashMap<>();
		List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingEntityList = dmpCfgInputConvertMappingService.lambdaQuery().eq(DmpCfgInputConvertMappingEntity::getDisabled, Boolean.FALSE).list();
		for (DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity : dmpCfgInputConvertMappingEntityList) {
			String id = dmpCfgInputConvertMappingEntity.getId();
			convertValueCache.put(id, dmpHandlerCache.getDmpCfgInputConvertValue(id));
		}

		typeCacheMap.put("dmpCfgInputConvertMapping" , convertMappingCache);
		typeCacheMap.put("dmpCfgInputConvertValue" , convertValueCache);

    	typeCacheMap.put("dmpCfgInputDetail", dmpHandlerCache.getDmpCfgInputDetailEntityList(d -> true));
    	typeCacheMap.put("dmpCfgInput", dmpHandlerCache.getDmpCfgInputEntityList(d -> true));
    	typeCacheMap.put("dmpCfgOutputBlack", dmpHandlerCache.getDmpCfgOutputBlackEntityList(d -> true));
    	
    	List<DmpCfgMqEntity> dmpCfgMqEntityList = dmpCfgMqService.lambdaQuery()
			.eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
			.eq(DmpCfgMqEntity::getDisabled, false).list();
    	Map<String , DmpCfgMqEntity> rocketMQDmpCfgMqCache = new HashMap<>();
    	Map<String , String> rocketMQTemplateMap = new HashMap<>();
    	for(DmpCfgMqEntity dmpCfgMqEntity : dmpCfgMqEntityList) {
    		String mqId = dmpCfgMqEntity.getId();
    		rocketMQDmpCfgMqCache.put(mqId, dmpHandlerCache.getRocketMQDmpCfgMqCache(mqId));
    		rocketMQTemplateMap.put(mqId, dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getNamesrvAddr() + "@" + dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getProducerGroup());
    	}
    	typeCacheMap.put("dmpCfgMqEntity", rocketMQDmpCfgMqCache);
    	typeCacheMap.put("rocketMQTemplate", rocketMQTemplateMap);
    	typeCacheMap.put("overseasProviderEntity", dmpHandlerCache.getOverseasProviderEntityList(d -> true));
    	typeCacheMap.put("dmpCfgApiEntity", dmpHandlerCache.getDmpCfgApiEntityList(d -> true));
    	
    	return success(typeCacheMap);
    }
    
    @PostMapping("initCache")
    public ApiResult<?> initCache() {
    	dmpHandlerCache.initCache(false);
    	return success();
    }

	/**
	 * 查询同步
	 * @param dto
	 * @return
	 */
	@PostMapping("querySyncByIds")
    public ApiResult<?> querySyncIds(@RequestBody BaseIdsDTO.IdsDTO dto) {
    	return this.querySync(Arrays.asList(new QueryParam(QueryTypeEnum.IN, "id", dto.getIds())));
    }
    
    @PostMapping("querySync")
    public ApiResult<?> querySync(@RequestBody List<QueryParam> queryParams) {
    	QueryWrapper<?> queryWrapper = QueryParam.getQueryWrapper(queryParams);
    	queryWrapper.eq("status", DmpOutputTaskRecordStatusEnum.ERROR.getCode());
    	ServiceImpl serviceImpl = ApplicationContextUtils.getBean("dmpOutputTaskRecordServiceImpl" , ServiceImpl.class);
    	List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = (List<DmpOutputTaskRecordEntity>)serviceImpl.list(queryWrapper);
    	if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
    		Map<String, String> cfgOutputIdEntityMaps = dmpOutputTaskService.lambdaQuery()
	    			.in(DmpOutputTaskEntity::getId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getMainId).collect(Collectors.toSet()))
	    			.select(DmpOutputTaskEntity::getId , DmpOutputTaskEntity::getCfgOutputId)
	    			.list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId));
	    	
	    	Map<String, DmpCfgOutputEntity> outputIdEntityMaps = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, cfgOutputIdEntityMaps.values())
	    			.list().stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, d -> d));
	    	
	    	Map<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMaps = new HashMap<>();
	    	for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
	    		String cfgOutputId = cfgOutputIdEntityMaps.get(dmpOutputTaskRecordEntity.getMainId());
	    		List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMaps.get(cfgOutputId);
	    		if(CollUtil.isEmpty(list)) {
	    			list = new ArrayList<>();
	    		}
	    		list.add(dmpOutputTaskRecordEntity);
	    		cfgOutputRecordEntityListMaps.put(cfgOutputId, list);
	    	}
    		
	    	for(Map.Entry<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMap : cfgOutputRecordEntityListMaps.entrySet()) {
	    		String cfgOutputId = cfgOutputRecordEntityListMap.getKey();
				DmpCfgOutputEntity dmpCfgOutputEntity = outputIdEntityMaps.get(cfgOutputId);
	    		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
	    		String cfgInputId = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0).getMainId();
	    		DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(cfgInputId)).get(0);
	    		String systemId = dmpCfgInputEntity.getSystemId();
	    		String systemCode = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(systemId)).get(0).getCode();
	    		
	    		List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMap.getValue();
	    		if(DmpBasicSystemCodeEnum.ERP.getCode().equals(systemCode)) {
	    			List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, list);
	    			if(CollUtil.isNotEmpty(erpQuerySync)) {
	    				dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordService.listByIds(erpQuerySync.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList())));
	    			}
	    		}else {
	    			List<String> dataIds = list.stream().map(DmpOutputTaskRecordEntity::getDataId).collect(Collectors.toList());
		    		List<String> ids = list.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList());
	    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
	    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
	    			dmpOutputHotfixCreateRequest.setQueryParams(Arrays.asList(new QueryParam(QueryTypeEnum.IN, "id", dataIds)));
	    			try {
						dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
						dmpOutputTaskRecordService.lambdaUpdate()
							.in(DmpOutputTaskRecordEntity::getId, ids)
							.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
							.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
							.set(DmpOutputTaskRecordEntity::getIsNeedSync, false)
							.update();
					} catch (Exception e) {
						log.error("查询同步调用dmp报错" , e);
					}
	    		}
	    	}
	    	
    	}
    	return success(dmpOutputTaskRecordEntityList);
    }
}
