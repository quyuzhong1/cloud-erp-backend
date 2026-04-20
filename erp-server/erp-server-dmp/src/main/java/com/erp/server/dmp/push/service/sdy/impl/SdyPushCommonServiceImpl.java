package com.erp.server.dmp.push.service.sdy.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.dmp.push.service.sdy.SdyCommonService;
import com.erp.server.dmp.push.service.sdy.SdyPushCommonService;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 数帝云推送通用接口
 */
@Slf4j
@Service
public class SdyPushCommonServiceImpl implements SdyPushCommonService {
    @Resource
    private SdyCommonService sdyCommonService;

    @Override
    public ApiResult executeConsumer(List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTO) {

        String url = sdyCommonService.getSdyUrl() + "/openapi/information/save";
        String requestData = "";
        //入参
        HashMap<String, Object> orderParams = new HashMap<>(2);
        orderParams.put("count", shudiyunB2cOrderDTO.size());
        orderParams.put("list", shudiyunB2cOrderDTO);
        orderParams.put("trace_id", TraceContext.traceId());
        requestData = JSONUtil.toJsonStr(orderParams);

        boolean is429 = true;
		String responseData = "";
		int i = 0;
		while(is429) {
//			log.warn("请求地址：{}\n数帝云请求报文：{}" , url , requestData);
			responseData = HttpUtil.post(url, requestData);
//			log.warn("请求数帝云响应报文：{}" , responseData);
			if(StringUtils.isNotBlank(responseData)) {
				Integer code = JSON.parseObject(responseData).getInteger("code");
				if(code != null && 429 == code) {
					try {
						Thread.sleep(1000);
						log.warn("数帝云限流次数={}" , i);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}else {
					is429 = false;
				}
			}
			i = i + 1;
			if(i > 10) {
				is429 = false;
			}
		}
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
