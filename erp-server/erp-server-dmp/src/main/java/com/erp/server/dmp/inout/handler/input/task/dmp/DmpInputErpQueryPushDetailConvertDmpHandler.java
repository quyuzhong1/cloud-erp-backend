package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入任务dmp数据库字段转换处理器，配置在dmp_cfg_input_convert_mapping表，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputErpQueryPushDetailConvertDmpHandler extends DmpInputDoNextDmpHandler{
	
	@Override
	protected void beforeConvertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		if(CollUtil.isEmpty(dmpInputMongoEntityList)) {
			return;
		}
		List<JSONArray> newEntityList = dmpInputMongoEntityList.stream().map(d -> {
			String pushData = d.get("pushData").toString();
			JSONObject parseObject = JSON.parseObject(pushData);
			if(parseObject.getBooleanValue("isQuerySync")) {
				log.warn("明细查询同步标识存在：{}" , JSON.toJSONString(d));
				return new JSONArray();
			}
			JSONArray jsonArray = parseObject.getJSONArray("detailList");
			if(CollUtil.isEmpty(jsonArray)) {
				log.warn("不存在明细节点：{}" , JSON.toJSONString(d));
				return new JSONArray();
			}
			for(Object j : jsonArray) {
				Map<String, Object> m = (Map<String, Object>)j;
				m.put(DmpInputMongoHandler.MONGO_BASE_ID, d.get(DmpInputMongoHandler.MONGO_BASE_ID));
				m.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, nextLevelId);
			}
			return jsonArray;
		}).filter(Objects::nonNull).collect(Collectors.toList());
		dmpInputMongoEntityList.clear();
		for(JSONArray jSONArray : newEntityList) {
			for(Object j : jSONArray) {
				dmpInputMongoEntityList.add((Map<String, Object>)j);
			}
		}
	}
}
