package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;

@Service
@Scope("prototype")
public class DmpInputDbConvertDmpHandler extends DmpInputBaseDmpHandler{
	
	@Autowired
	private DmpCfgInputConvertMappingService dmpCfgInputConvertMappingService;
	
	@Override
	protected String convertKey(String originalKey) {
		Map<String, String> keyMapping = dmpCfgInputConvertMappingService.getMapping(convertId);
		String convertKey = keyMapping.get(originalKey);
		if(StringUtils.isBlank(convertKey)) {
			convertKey = originalKey;
		}
		return convertKey;
	}
}
