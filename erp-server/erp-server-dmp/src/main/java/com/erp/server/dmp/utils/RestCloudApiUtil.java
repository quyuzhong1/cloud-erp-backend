package com.erp.server.dmp.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class RestCloudApiUtil {
	private RestCloudApiUtil() {}

    private static String restcloudUrl = SpringUtil.getProperty("restcloud.url");
	
	private static String restcloudPort = SpringUtil.getProperty("restcloud.port");
	
	public static boolean reCreate(String checkMonth , String ... urls) {
		boolean resultBool = false;
		for(String url : urls) {
			Map<String, Object> map = new HashMap<>();
			map.put("data", Arrays.asList());
			map.put("yearMonth", checkMonth);
			String restUrl = "http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/" + url;
			HttpResponse response = HttpRequest.post(restUrl)
	                .header("Content-Type", "application/json")
	                .body(JSON.toJSONString(map))
	                .timeout(60000)
	                .execute();
			if (200 != response.getStatus()) {
				throw new ServiceException("调用谷云地址：" + restUrl + "状态码"+ response.getStatus() +"错误，请联系实施");
			}else {
				String body = response.body();
				JSONObject responseJson = JSON.parseObject(body);
				Integer resultCode = responseJson.getInteger("resultCode");
	            // 判断结果异常:ETLProcessRunResultCode
	            if (null != resultCode && 1 == resultCode) {
	            	JSONArray jsonArray = responseJson.getJSONArray("data");
	            	if(CollUtil.isNotEmpty(jsonArray)) {
	            		resultBool = true;
	            	}else {
	            		String dataTotal = responseJson.getString("dataTotal");
	            		if(StringUtils.isNotBlank(dataTotal) && Stream.of(dataTotal.split(",")).anyMatch(s -> !s.trim().equals("0"))) {
	            			resultBool = true;
	            		}
	            	}
	            }else {
	            	throw new ServiceException("调用谷云地址：" + restUrl + "返回报文："+ body +"错误，请联系实施");
	            }
			}
		}
		return resultBool;
	} 
}
