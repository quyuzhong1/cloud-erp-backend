package com.erp.server.dmp.inout.handler.input.task.init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.jitu.service.JituService;
import com.sdk.wms.jitu.utils.AuthUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class JituProductInitHandler extends DmpInputInitHandler{
	
	@Resource
	private JituService jituService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList = FeignQuery.create(OverseasProviderWarehouseEntity.class).eq(OverseasProviderWarehouseEntity::getMainId, nextLevelId).list();
		if(CollUtil.isEmpty(overseasProviderWarehouseEntityList)) {
			return new ArrayList<>();
		}
		
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.JITU.getCode()));
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("极兔对应授权ID信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException("极兔对应授权ID信息不存在");
		}
		
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
		String apiType = dmpCfgApiEntity.getApiType(); 
		
		Map<String, Object> authMap = overseasProviderEntity.getAuthJson();
		
		DmpInputTaskInitDTO result = new DmpInputTaskInitDTO();
		JSONArray jSONArray = new JSONArray();
		Integer pageSize = 100;
		for(OverseasProviderWarehouseEntity overseasProviderWarehouseEntity : overseasProviderWarehouseEntityList) {
			Integer pageNum = 0;
			String warehouseCode = overseasProviderWarehouseEntity.getPlatformWarehouseCode();
			JSONObject request = new JSONObject();
	        request.put("customerid", authMap.get("customerid"));
	        request.put("warehouseCode", warehouseCode);
            request.put("pageSize", pageSize);
            while(true) {
            	pageNum = pageNum + 1;
            	request.put("startPage", pageNum);
            	String requestJson = JSONUtil.toJsonStr(request);
            	try {
				authMap.put("logistics_interface", requestJson);
    				String bodyStr = "";
    				int i = 0;
    				while(true) {
    					try {
    						bodyStr = AuthUtils.doPost(jituService.getPreUrl() + apiType, authMap);
    						break;
    					} catch (Exception e) {
    						String message = e.getMessage();
    						if(StringUtils.isNotBlank(message) && message.contains("timed out")) {
    							i = i + 1;
    							if(i > 5) {
    								throw e;
    							}
    						}else {
    							throw e;
    						}
    					}
    				}
    	            JSONObject responseJson = JSON.parseObject(bodyStr);
    	            JSONArray jsonArray = responseJson.getJSONArray("responseitems");
    	            if (CollUtil.isEmpty(jsonArray)) {
    	                throw new ServiceException("极兔接口返回为空，请求报文：" + request);
    	            }
    	            Object object = jsonArray.get(0);
    	            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(object));
    	            String success = jsonObject.getString("success");
    	            if ("true".equals(success)) {
    	                JSONArray baseList = jsonObject.getJSONArray("baseList");
    	                if(CollUtil.isEmpty(baseList)) {
    	                	break;
    	                }
    	                baseList.forEach(p -> {
    	        			JSONObject j = (JSONObject)p;
    	        			j.put("authId", nextLevelId);
    	        			j.put("ulzWarehouseCode", warehouseCode);
    	        		});
    	                jSONArray.addAll(baseList);
    	            } else {
    	                throw new ServiceException("极兔获取产品接口失败，请求报文：{}，响应报文：{} " , request , bodyStr);
    	            }
    			} catch (IOException e1) {
    				throw new ServiceException("极兔获取产品接口报错" + ExceptionUtil.stacktraceToOneLineString(e1));
    			}
            }
		}
		result.setMsg(jSONArray.toJSONString());
		return Collections.singletonList(result);
	}
}
