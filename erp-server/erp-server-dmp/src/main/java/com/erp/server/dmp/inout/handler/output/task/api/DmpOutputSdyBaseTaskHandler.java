package com.erp.server.dmp.inout.handler.output.task.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpSoOriginalInfoEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DmpSoOriginalInfoService;
import com.erp.server.dmp.service.DmpSoReturnDetailService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public abstract class DmpOutputSdyBaseTaskHandler extends DmpOutputTaskHandler {

	@Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
	
	@Resource
	private DmpSoOriginalInfoService dmpSoOriginalInfoService;
	
	@Resource
	private DmpSoReturnDetailService dmpSoReturnDetailService;
	
	@Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, String> map = this.getPushJsonDataMap(dmpRequest, dmpResponse);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            List<String> sourceCodeKeys = this.getSourceCodeKeys();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                String value = entry.getValue();
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                dmpOutputTaskRecordEntity.setId(id);
                dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                dmpOutputTaskRecordEntity.setDataId(dataId);
                JSONObject parseObject = JSON.parseObject(value);
                if(CollUtil.isNotEmpty(sourceCodeKeys)) {
    				dmpOutputTaskRecordEntity.setSourceCode(sourceCodeKeys.stream().map(s -> {
    					String string = parseObject.getString(s);
    					if(string == null) {
    						string = "";
    					}
    					return string;
    				}).collect(Collectors.joining("_")));
    			}
                dmpOutputTaskRecordEntity.setRequestData(value);
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
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        String id = dmpOutputTaskRecordEntity.getId();
        String status = "";
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        ApiResult handle = sdyDeliveryOrderConsumer.handle(requestData);
        if (200 == handle.getCode()) {
            status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        } else {
            status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()), handle.getMsg());
    }
	
	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no" , "msku_code");
    }
	
	protected void dealWdtRootNodeNoInitial(Map<String, String> map) {
		if(CollUtil.isEmpty(map)) {
			return;
		}
		
		List<com.erp.model.oms.entity.DictBasicEntity> dictList = FeignQuery.create(DictBasicEntity.class)
				.eq(DictBasicEntity::getType, "sdySubPlatform")
				.eq(DictBasicEntity::getSubType, DmpBasicSystemCodeEnum.WDT.getCode()).list();
		Set<String> wdtPlatformSet = dictList.stream().map(DictBasicEntity::getRemark).collect(Collectors.toSet());
		if(CollUtil.isEmpty(wdtPlatformSet)) {
			return;
		}
		
		Map<String, ShudiyunB2cOrderDTO> wdtMap = new HashMap<>();
		for(Map.Entry<String, String>  m : map.entrySet()) {
			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = JSON.parseObject(m.getValue(), ShudiyunB2cOrderDTO.class);
			String platform_id = shudiyunB2cOrderDTO.getPlatform_id();
			String root_node_no_initial = shudiyunB2cOrderDTO.getRoot_node_no_initial();
			if(wdtPlatformSet.contains(platform_id) && StringUtils.isNotBlank(root_node_no_initial)) {
				wdtMap.put(m.getKey(), shudiyunB2cOrderDTO);
			}
    	}
		
		if(CollUtil.isEmpty(wdtMap)) {
			return;
		}
		
		Set<String> dbPlatformCodeSet = dmpSoOriginalInfoService.lambdaQuery().in(DmpSoOriginalInfoEntity::getPlatformCode, 
				wdtMap.values().stream().map(ShudiyunB2cOrderDTO::getRoot_node_no_initial).collect(Collectors.toSet()))
				.select(DmpSoOriginalInfoEntity::getPlatformCode).list()
				.stream().map(DmpSoOriginalInfoEntity::getPlatformCode).collect(Collectors.toSet());
		Map<String, ShudiyunB2cOrderDTO> firstWdtMap = new HashMap<>();
		for(Map.Entry<String, ShudiyunB2cOrderDTO> wdt : wdtMap.entrySet()) {
			String root_node_no_initial = wdt.getValue().getRoot_node_no_initial();
			if(!dbPlatformCodeSet.contains(root_node_no_initial)) {
				firstWdtMap.put(wdt.getKey(), wdt.getValue());
			}
		}
		
		while(CollUtil.isNotEmpty(firstWdtMap)) {
			Map<String, String> tidRawMaps = dmpSoReturnDetailService.lambdaQuery().in(DmpSoReturnDetailEntity::getTid, 
					firstWdtMap.values().stream().map(ShudiyunB2cOrderDTO::getRoot_node_no_initial).collect(Collectors.toSet()))
				.isNotNull(DmpSoReturnDetailEntity::getRawRefundNos)
				.ne(DmpSoReturnDetailEntity::getRawRefundNos, "")
				.isNotNull(DmpSoReturnDetailEntity::getTid)
				.ne(DmpSoReturnDetailEntity::getTid, "")
				.list().stream().collect(Collectors.toMap(DmpSoReturnDetailEntity::getTid, DmpSoReturnDetailEntity::getRawRefundNos , (m1 , m2) -> m1));
			Map<String, ShudiyunB2cOrderDTO> newWdtMap = new HashMap<>();
			
			Collection<String> values = tidRawMaps.values();
			if(CollUtil.isNotEmpty(values)) {
				dbPlatformCodeSet = new HashSet<>();
			}else {
				dbPlatformCodeSet = dmpSoOriginalInfoService.lambdaQuery().in(DmpSoOriginalInfoEntity::getPlatformCode, values)
						.select(DmpSoOriginalInfoEntity::getPlatformCode).list()
						.stream().map(DmpSoOriginalInfoEntity::getPlatformCode).collect(Collectors.toSet());
			}
			for(Map.Entry<String, ShudiyunB2cOrderDTO> wdt : firstWdtMap.entrySet()) {
				String key = wdt.getKey();
				ShudiyunB2cOrderDTO value = wdt.getValue();
				String root_node_no_initial = value.getRoot_node_no_initial();
				if(tidRawMaps.containsKey(root_node_no_initial)) {
					String rawNo = tidRawMaps.get(root_node_no_initial);
					if(value.getRoot_node_no_initial().equals(value.getRoot_node_no())) {
						value.setRoot_node_no(rawNo);
					}
					value.setRoot_node_no_initial(rawNo);
					if(dbPlatformCodeSet.contains(rawNo)) {
						map.put(key, JSON.toJSONString(value));
					}else {
						newWdtMap.put(key, value);
					}
				}
			}
			firstWdtMap = newWdtMap;
		}
	}
}
