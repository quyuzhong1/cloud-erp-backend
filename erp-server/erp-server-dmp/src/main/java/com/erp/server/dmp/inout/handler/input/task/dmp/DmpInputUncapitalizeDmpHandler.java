package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DmpInputUncapitalizeDmpHandler extends DmpInputBaseDmpHandler{
	
	@Override
	protected String convertKey(String originalKey) {
		return StringUtils.uncapitalize(originalKey);
	}
}
