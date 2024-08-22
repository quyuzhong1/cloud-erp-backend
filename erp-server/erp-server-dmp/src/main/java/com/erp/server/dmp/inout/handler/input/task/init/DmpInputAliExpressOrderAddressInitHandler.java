package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderAddressInitHandler extends DmpInputInitHandler{
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
		
		List<ParamData> paramDataList = new ArrayList<>();
		List<String> orderIdList = findMongoData.stream().map(f -> f.get("order_id").toString()).collect(Collectors.toList());
		paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
		findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}
		
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
		request.addApiParameter("simplify", "true");
		
		List<JSONObject> dataList = new ArrayList<>();
        for(Map<String, Object> findMongo : findMongoData) {
        	String order_id = findMongo.get("order_id").toString();
        	String oaid = findMongo.get("oaid").toString();
        	request.addApiParameter("orderId", order_id);
        	request.addApiParameter("oaid", oaid);
            
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
        	data.put("order_id", order_id);
        	dataList.add(data);
        }
        
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSON.toJSONString(dataList));
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
        JSONObject data = body.getJSONObject("result_obj");
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
