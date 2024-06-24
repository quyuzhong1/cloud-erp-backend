package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputDbConvertMongoHandler extends DmpInputBaseMongoHandler{
	
	@Autowired
	private DmpCfgInputConvertMappingService dmpCfgInputConvertMappingService;
	
	@Override
	protected List<String> convertKey(String originalKey) {
		Map<String, List<String>> keyMapping = dmpCfgInputConvertMappingService.getMapping(convertId);
		List<String> convertKey = keyMapping.get(originalKey);
		if(CollUtil.isEmpty(convertKey)) {
			convertKey = new ArrayList<>();
		}
		return convertKey;
	}
}
