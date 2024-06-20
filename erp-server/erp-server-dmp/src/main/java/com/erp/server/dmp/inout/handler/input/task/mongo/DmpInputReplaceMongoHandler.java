package com.erp.server.dmp.inout.handler.input.task.mongo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DmpInputReplaceMongoHandler extends DmpInputBaseMongoHandler{
	
	@Override
	protected String convertKey(String originalKey) {
		return originalKey.replace("-", "").replace("_", "");
	}
}
