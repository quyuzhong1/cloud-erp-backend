package com.erp.server.dmp.push.service.sdy;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Administrator
 *
 */
@Slf4j
@Service
public class ErpPushSdyService {
	public static final String REQUEST_URL = "requestUrl";
	public static final String REQUEST_DATA = "requestData";
	public static final String REQUEST_SDY = "requestSdy";

	@Resource
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	
	@Value("${sdy.url:http://localhost:8080}")
    private String sdyUrl;

	public String getSdyUrl() {
		return sdyUrl;
	}

	public ApiResult<?> requestSdy(Object ext) {
		JSONObject parseObject = JSON.parseObject(ext.toString());
		String url = parseObject.getString(REQUEST_URL);
		if(!url.startsWith("/")) {
			url = "/" + url;
		}
		url = sdyUrl + url;

		String requestData = "";
		String data = parseObject.getString(REQUEST_DATA);
		if (data.startsWith("[") && data.endsWith("]")) {
			// 如果 ext 是数组
			JSONArray extArray = JSON.parseArray(data);
			JSONObject object = new JSONObject();
			object.put("count", extArray.size());
			object.put("list", extArray);
			object.put("trace_id", TraceContext.traceId());
			requestData = object.toJSONString();
		} else {
			requestData = data;
		}

		boolean is429 = true;
		String responseData = "";
		int i = 0;
		while(is429) {
			log.warn("请求地址：{}\n数帝云请求报文：{}" , url , requestData);
			responseData = HttpUtil.post(url, requestData);
			log.warn("请求数帝云响应报文：{}" , responseData);
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

	/**
	 * 校验数据是否删除处理
	 */
	private String isDeletedHandler(ShudiyunB2cOrderDTO dto) {
		String sourceCode = dto.getBiz_no() + "_" + dto.getSku_code();
		//查询是否之前有推送过数帝云
		List<DmpOutputTaskRecordEntity> list = dmpOutputTaskRecordService.lambdaQuery()
				.eq(DmpOutputTaskRecordEntity::getSourceCode, sourceCode)
				.orderByDesc(DmpOutputTaskRecordEntity::getCreateTime)
				.list();
		for (DmpOutputTaskRecordEntity recordEntity : list) {
			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = JSON.parseObject(recordEntity.getRequestData(), ShudiyunB2cOrderDTO.class);
			if ("已删除".equals(shudiyunB2cOrderDTO.getStatus())) {
				DmpOutputTaskRecordEntity taskRecordEntity = list.stream().filter(req -> DmpOutputTaskRecordStatusEnum.FINISH.getCode().equals(req.getStatus())).findFirst().orElse(null);
				if (ObjectUtil.isNotEmpty(taskRecordEntity)) {
					ShudiyunB2cOrderDTO shudiyunB2cOrderDTO1 = JSON.parseObject(recordEntity.getRequestData(), ShudiyunB2cOrderDTO.class);
					shudiyunB2cOrderDTO1.setStatus("已删除");
					return JSON.toJSONString(shudiyunB2cOrderDTO1);
				} else {
					dmpOutputTaskRecordService.lambdaUpdate()
							.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
							.eq(DmpOutputTaskRecordEntity::getSourceCode, sourceCode)
							.update();
					return "";
				}
			}
		}
		return JSON.toJSONString(dto);
	}
}