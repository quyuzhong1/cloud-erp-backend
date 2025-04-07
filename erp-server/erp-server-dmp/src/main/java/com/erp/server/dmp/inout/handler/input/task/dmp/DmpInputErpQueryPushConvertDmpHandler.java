package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入任务dmp数据库字段转换处理器，配置在dmp_cfg_input_convert_mapping表，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputErpQueryPushConvertDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Override
	protected void beforeConvertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		if(CollUtil.isEmpty(dmpInputMongoEntityList)) {
			return;
		}
		List<JSONObject> newEntityList = dmpInputMongoEntityList.stream().map(d -> {
			String pushData = d.get("pushData").toString();
			JSONObject parseObject = JSON.parseObject(pushData);
			parseObject.put(DmpInputMongoHandler.MONGO_BASE_ID, d.get(DmpInputMongoHandler.MONGO_BASE_ID));
			parseObject.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, nextLevelId);
			return parseObject;
		}).collect(Collectors.toList());
		dmpInputMongoEntityList.clear();
		dmpInputMongoEntityList.addAll(newEntityList);
	}
}
