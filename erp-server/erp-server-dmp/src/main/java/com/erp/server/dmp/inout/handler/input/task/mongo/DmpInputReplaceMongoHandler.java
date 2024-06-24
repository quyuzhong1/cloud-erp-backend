package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DmpInputReplaceMongoHandler extends DmpInputBaseMongoHandler{
	
	@Override
	protected List<String> convertKey(String originalKey) {
		return Collections.singletonList(originalKey.replace("-", "").replace("_", ""));
	}
}
