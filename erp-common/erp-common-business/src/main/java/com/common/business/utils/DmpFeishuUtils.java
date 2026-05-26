package com.common.business.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;

public class DmpFeishuUtils {
	private static String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");
	
	public static void sendFeiShuMsg(String message) {
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put("msg_type", "text");
		Map<String, String> contentMap = new HashMap<String, String>();

		contentMap.put("text", "中台【"+ namespace +"】环境告警：" + message);
		bodyMap.put("content", contentMap);
		String url = "https://open.feishu.cn/open-apis/bot/v2/hook/8002a820-b24d-4ed3-87e0-8b5b867cc9e3";
		if("prod".equals(namespace)) {
			url = "https://open.feishu.cn/open-apis/bot/v2/hook/c76b72f8-0bf9-4967-a9ce-0728767c1ccc";
		}
		HttpUtil.post(url, JSON.toJSONString(bodyMap));
	}
}
