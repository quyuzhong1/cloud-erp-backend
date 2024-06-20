package com.erp.server.dmp.inout.handler.input.task.mongo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DmpInputUncapitalizeMongoHandler extends DmpInputBaseMongoHandler{
	
	@Override
	protected String convertKey(String originalKey) {
		return StringUtils.uncapitalize(originalKey);
	}
}
