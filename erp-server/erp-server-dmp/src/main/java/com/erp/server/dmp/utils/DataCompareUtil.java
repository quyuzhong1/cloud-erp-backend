package com.erp.server.dmp.utils;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;

import cn.hutool.core.bean.BeanUtil;

public class DataCompareUtil {
	public static void main(String[] args) {
		GyyDeliveryDetailEntity g = new GyyDeliveryDetailEntity();
		GyyDeliveryDetailEntity copyProperties = BeanUtil.copyProperties(g, g.getClass());
		copyProperties.set_id("id");
		System.out.println(compareObject(g, copyProperties));
	}
	
	public static boolean compareObject(Object mongoDatum , Object entity) {
		if (mongoDatum == entity) {
			return true;
		}
		if(mongoDatum != null && entity == null) {
			return false;
		}
		if(mongoDatum == null && entity != null) {
			return false;
		}
		Class<? extends Object> mongoClass = mongoDatum.getClass();
		Class<? extends Object> entityClass = entity.getClass();
		if (mongoClass != entityClass) {
			return false;
		}
		String mongoStr = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum) , mongoClass).toString();
		String entityStr = JSONObject.parseObject(JSONObject.toJSONString(entity) , entityClass).toString();
		return mongoStr.equals(entityStr);
	}
}
