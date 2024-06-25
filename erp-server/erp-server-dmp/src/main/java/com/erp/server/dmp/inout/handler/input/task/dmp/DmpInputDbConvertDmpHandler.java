package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputDbConvertDmpHandler extends DmpInputBaseDmpHandler{
	
	@Override
	protected List<String> convertKey(String originalKey) {
		Map<String, List<String>> keyMapping = dmpHandlerCache.getDmpCfgInputConvertMapping(convertId);
		List<String> convertKey = keyMapping.get(originalKey);
		if(CollUtil.isEmpty(convertKey)) {
			convertKey = Collections.singletonList(originalKey);
		}
		return convertKey;
	}
}
