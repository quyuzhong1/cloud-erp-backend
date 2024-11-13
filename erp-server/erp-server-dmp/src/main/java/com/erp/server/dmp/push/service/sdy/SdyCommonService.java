package com.erp.server.dmp.push.service.sdy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Administrator
 *
 */
@Slf4j
@Service
public class SdyCommonService {
	public static final String REQUEST_URL = "requestUrl";
	public static final String REQUEST_DATA = "requestData";
	public static final String REQUEST_SDY = "requestSdy";
	
	@Value("${dmp.url:http://localhost:8080}")
    private String sdyUrl;
	
	public ApiResult<?> requestSdy(Object ext) {
		JSONObject parseObject = JSON.parseObject(ext.toString());
		String url = parseObject.getString(REQUEST_URL);
		if(!url.startsWith("/")) {
			url = "/" + url;
		}
		url = sdyUrl + url;
		
		String requestData = parseObject.getString(REQUEST_DATA);
		log.warn("请求地址：{}，请求数帝云请求报文：{}" , url , requestData);
		String responseData = HttpUtil.post(url, requestData);
		log.warn("请求数帝云响应报文：{}" , responseData);
		JSONObject responseObject = null;
		try {
			responseObject = JSON.parseObject(responseData);
			Integer errno = responseObject.getInteger("errno");
			if(0 == errno) {
				return ApiResult.success(responseObject);
			}else {
				return ApiResult.error("" , responseObject);
			}
		} catch (Exception e) {
		}
		return ApiResult.error(responseData);
	}
}