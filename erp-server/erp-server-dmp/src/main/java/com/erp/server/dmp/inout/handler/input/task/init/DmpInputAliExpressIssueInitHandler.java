package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressIssueInitHandler extends DmpInputInitHandler{
	@Resource
    private AliExpressOrderService aliExpressOrderService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<Map<String, Object>> findMongoData = null;
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if(StringUtils.isNotBlank(parentStorageName)) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
			findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
		}
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}
		AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		
		String appKey = aliExpressShopInfoDTO.getClientId();
        String appSecret = aliExpressShopInfoDTO.getClientSecret();
        String baseUrl = aliExpressShopInfoDTO.getBaseUrl();
        String token = aliExpressShopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
		
        IopRequest request = new IopRequest();
        String typeId = dmpCfgInputEntity.getTypeId();
        
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        
        String apiType = dmpCfgApiEntity.getApiType();
		request.setApiName(apiType);
		
		Map<String, String> orderIssueStatusMaps = new HashMap<>();
		for(Map<String, Object> f : findMongoData) {
			Object product_list_obj = f.get("product_list");
			if(product_list_obj != null) {
				List<Map<String, Object>> product_list = (List<Map<String, Object>>) product_list_obj;
				for(Map<String, Object> p : product_list) {
					Object child_id = p.get("child_id");
					Object issue_status_obj = p.get("issue_status");
					if(issue_status_obj != null) {
						String issue_status = issue_status_obj.toString();
						if("IN_ISSUE".equals(issue_status) || "END_ISSUE".equals(issue_status)) {
							if(child_id != null) {
								orderIssueStatusMaps.put(child_id.toString(), issue_status);
							}
						}
					}
				}
			}
		}
		
		JSONArray result = new JSONArray();
		for(Map.Entry<String, String> orderIssueStatusMap : orderIssueStatusMaps.entrySet()) {
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("current_page", 1);
			paramMap.put("page_size", 50);
			paramMap.put("order_no", orderIssueStatusMap.getKey());
			if("END_ISSUE".equals(orderIssueStatusMap.getValue())) {
				paramMap.put("issue_status", "finish");
			}
	        request.addApiParameter("query_dto", JSON.toJSONString(paramMap));
	        
	        JSONObject data = null;
	    	long sleepTime = 1000;
	    	int count = 0;
	    	while(data == null) {
	    		data = this.execute(client, request, token, apiType);
	    		if(data == null) {
	    			if(count == 10) {
        				throw new ServiceException("调用速卖通" + apiType + "接口重试" + count + "失败");
        			}
	    			try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
	    			sleepTime = sleepTime + 1000;
	    			count = count + 1;
	    		}
	    	}
	    	
	    	JSONObject data_list = data.getJSONObject("data_list");
	    	if(data_list != null) {
	    		JSONArray issue_api_issue_dto = data_list.getJSONArray("issue_api_issue_dto");
	    		if(issue_api_issue_dto != null) {
	    			result.addAll(issue_api_issue_dto);
	    		}
	    	}
		}
        
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(result.toJSONString());
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		
		return dmpInputTaskInitDTOList;
	}
	
	private JSONObject execute(IopClient client , IopRequest request , String token , String apiType){
		IopResponse response = null;
		try {
			response = client.execute(request, token, Protocol.TOP);
		} catch (ApiException e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		JSONObject body = JSON.parseObject(response.getBody());
		JSONObject data = body.getJSONObject("aliexpress_issue_issuelist_get_response");
        if(data == null) {
        	JSONObject errorResponse = body.getJSONObject("error_response");
        	if(errorResponse == null) {
        		return null;
        	}
        	String code = errorResponse.getString("code");
        	if(!"ApiCallLimit".equals(code) && !"15".equals(code) && !"UnknownRuntimeException".equals(code)) {
        		throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + errorResponse.getString("msg"));
        	}
        }
		return data;
	}
}
