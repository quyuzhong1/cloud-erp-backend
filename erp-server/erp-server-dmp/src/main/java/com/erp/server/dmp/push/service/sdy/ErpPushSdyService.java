package com.erp.server.dmp.push.service.sdy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

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
			requestData = object.toJSONString();
		} else {
			requestData = data;
		}

		log.info("请求地址：{}\n数帝云请求报文：{}" , url , requestData);
		String responseData = HttpUtil.post(url, requestData);
		log.info("请求数帝云响应报文：{}" , responseData);
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