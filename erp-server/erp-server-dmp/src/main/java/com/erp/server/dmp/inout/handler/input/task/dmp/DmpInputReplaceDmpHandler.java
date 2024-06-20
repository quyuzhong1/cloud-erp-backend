package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DmpInputReplaceDmpHandler extends DmpInputBaseDmpHandler{
	
	@Override
	protected String convertKey(String originalKey) {
		return originalKey.replace("-", "").replace("_", "");
	}
}
