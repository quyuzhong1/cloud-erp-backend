package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class AntuReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler{

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
		Object detailListObj = dmpInputMongoEntity.get("detail");
		if (null == detailListObj) {
			return Collections.emptyList();
		}
		// 退货/退款信息
		JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
		if (CollectionUtils.isEmpty(jsonArray)) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> resultList = new LinkedList<>();

		for (Object detailObj : jsonArray) {
			JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
			resultList.add(jsonObject);
		}
		return resultList;
	}
}
