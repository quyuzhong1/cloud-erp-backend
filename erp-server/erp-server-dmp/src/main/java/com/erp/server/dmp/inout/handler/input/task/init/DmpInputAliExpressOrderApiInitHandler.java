package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
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

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderApiInitHandler extends DmpInputInitHandler{

	@Resource
    private AliExpressOrderService aliExpressOrderService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(nextLevelId);
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
		int pageNo = 1;
		Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("page_size", 50);
        
        String taskType = dmpInputTaskEntity.getTaskType();
        String startTime = dmpInputTaskEntity.getStartTime().atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = dmpInputTaskEntity.getEndTime().atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        String extendJson = dmpInputTaskEntity.getExtendJson();
    	boolean finishFlag = false;
    	if(StringUtils.isNotBlank(extendJson)) {
    		JSONObject parseObject = JSON.parseObject(extendJson);
    		if(parseObject != null) {
    			finishFlag = parseObject.getBooleanValue("finish");
    		}
    	}
        if(DmpInputTaskTaskTypeEnum.HISTORY.getCode().equals(taskType)){
        	paramMap.put("create_date_start", startTime);
    		paramMap.put("create_date_end", endTime);
        	if(finishFlag) {
        		paramMap.put("order_status", "FINISH");
        	}
        }else {
        	if(finishFlag) {
        		paramMap.put("modified_date_start", startTime);
        		paramMap.put("modified_date_end", endTime);
        		paramMap.put("create_date_start", LocalDateTimeUtil.offset(LocalDateTime.now(), -29, ChronoUnit.DAYS).atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                        .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                        .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        		paramMap.put("create_date_end", LocalDateTime.now().atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                        .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                        .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        		paramMap.put("order_status", "FINISH");
        	}else {
        		paramMap.put("modified_date_start", startTime);
        		paramMap.put("modified_date_end", endTime);
        		paramMap.put("create_date_start", LocalDateTimeUtil.offset(LocalDateTime.now(), -179, ChronoUnit.DAYS).atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                        .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                        .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        		paramMap.put("create_date_end", LocalDateTime.now().atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                        .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                        .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        	}
        }
        
        JSONArray order = new JSONArray();
        boolean firstFlag = true;
		while(true) {
			paramMap.put("current_page", pageNo);
			request.addApiParameter("param_aeop_order_query", JSON.toJSONString(paramMap));
			
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
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
	    			sleepTime = sleepTime + 1000;
	    			count = count + 1;
	    		}
	    	}
			if(firstFlag) {
				pageNo = data.getInteger("total_page");
				firstFlag = false;
			}else {
				pageNo = pageNo - 1;
			}
			order.addAll(data.getJSONArray("target_list"));
			
			if(pageNo <= 1) {
				break;
			}
			
		}
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		order.forEach(o -> {
			JSONObject j = (JSONObject) o;
			j.put("order_id", j.get("order_id").toString());
		});
		dmpInputTaskInitDTO.setMsg(order.toJSONString());
		
		return Collections.singletonList(dmpInputTaskInitDTO);
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
        JSONObject data = body.getJSONObject("result");
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
