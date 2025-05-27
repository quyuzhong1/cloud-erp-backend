package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputFeishuApprovalsRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
    	
    	Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMaps = dmpRequest.getChangeConvertInputMongoEntityListMaps();
    	
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        if(CollUtil.isNotEmpty(changeConvertInputMongoEntityListMaps)) {
        	for(Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMap : changeConvertInputMongoEntityListMaps.entrySet()) {
        		List<Map<String, Object>> value = changeConvertInputMongoEntityListMap.getValue();
        		if(CollUtil.isNotEmpty(value)) {
        			for(Map<String, Object> v : value) {
        				if(this.validateDataBlack(v, cfgOutputId)) {
        					continue;
        				}
        				map.put(v.get("_id").toString(), JSON.toJSONString(v));
        			}
        		}
        	}
        }
        return map;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("ulanzi_approval_code");
    }

}
