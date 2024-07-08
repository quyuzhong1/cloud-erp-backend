package com.erp.server.dmp.controller.api;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgMqService;


/**
 * 输入输出任务
 * @author Administrator
 *
 */
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
	
    @PostMapping("doInputTask")
    public ApiResult<?> doInputTask(@RequestBody DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
    	return success(dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest));
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
    	typeCacheMap.put("dmpCfgInputConvert" , convertMappingCache);
    	
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
    	
    	return success(typeCacheMap);
    }
    
    @PostMapping("initCache")
    public ApiResult<?> initCache() {
    	dmpHandlerCache.initCache(false);
    	return success();
    }
    
}
