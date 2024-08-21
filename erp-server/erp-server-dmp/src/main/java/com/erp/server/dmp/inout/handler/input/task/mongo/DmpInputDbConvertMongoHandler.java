package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入任务mongo数据库字段转换处理器，配置在dmp_cfg_input_convert_mapping表，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputDbConvertMongoHandler extends DmpInputBaseMongoHandler{
	
	@Override
	protected List<String> convertKey(String originalKey) {
		Map<String, List<String>> keyMapping = dmpHandlerCache.getDmpCfgInputConvertMapping(convertId);
		List<String> convertKey = Collections.singletonList(originalKey);
		if(keyMapping != null) {
			List<String> dbKeyMapping = keyMapping.get(originalKey);
			if(CollUtil.isNotEmpty(dbKeyMapping)) {
				convertKey = dbKeyMapping;
			}
		}
		return convertKey;
	}
}
