package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressProductApiInitHandler implements DmpInputApiInitHandler{

	@Resource
    private AliExpressOrderService aliExpressOrderService;
	
	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
		AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(dmpInputApiInitRequest.getNextLevelId());
		String appKey = aliExpressShopInfoDTO.getClientId();
        String appSecret = aliExpressShopInfoDTO.getClientSecret();
        String baseUrl = aliExpressShopInfoDTO.getBaseUrl();
        String token = aliExpressShopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        
        IopRequest request = new IopRequest();
		String apiType = dmpInputApiInitRequest.getApiType();
		request.setApiName(apiType);
		request.addApiParameter("simplify", "true");
		int pageNo = 1;
		Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("page_size", 200);
        paramMap.put("product_status_type", AliexpressConstants.ON_SELLING);
        paramMap.put("gmt_modified_start", dmpInputApiInitRequest.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        paramMap.put("gmt_modified_end", dmpInputApiInitRequest.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        JSONArray order = new JSONArray();
		while(true) {
			paramMap.put("current_page", pageNo);
			request.addApiParameter("aeop_a_e_product_list_query", JSON.toJSONString(paramMap));
			
			IopResponse response = null;
			try {
				response = client.execute(request, token, Protocol.TOP);
			} catch (ApiException e) {
				throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
			}
			JSONObject body = JSON.parseObject(response.getBody());
			JSONObject result = body.getJSONObject("result");
			Integer total = result.getInteger("total_page");
			boolean success = result.getBooleanValue("success");
			if(success) {
				JSONArray jsonArray = result.getJSONArray("aeop_a_e_product_display_d_t_o_list");
				if(CollUtil.isNotEmpty(jsonArray)) {
					order.addAll(jsonArray);
				}
			}else {
				throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + result.toJSONString());
			}
			
			if(pageNo >= total) {
				break;
			}
			pageNo = pageNo + 1;
		}
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(order.toJSONString());
		
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

}
