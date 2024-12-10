package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressInventoryWarehouseInitHandler extends DmpInputInitHandler{
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
		// 库存类型（1 良品，101 残品）
		Integer inventoryType = 1;
		if (StringUtils.isNotBlank(dmpCfgInputEntity.getExtendJson())){
			JSONObject jsonObject = JSON.parseObject(dmpCfgInputEntity.getExtendJson());
			inventoryType = jsonObject.getInteger("inventory_type");
		}

		String apiType = dmpCfgApiEntity.getApiType();
		request.setApiName(apiType);
		request.addApiParameter("simplify", "true");

		int pageNo = 1;
		Map<String, Object> paramMap = new HashMap<>();
		// 账套编码
		paramMap.put("biz_type", 288000);
		//分页大小，最大30
		paramMap.put("page_size", 30);
		// 库存类型（1 良品，101 残品）
		paramMap.put("inventory_type", inventoryType);

		JSONArray order = new JSONArray();
		boolean firstFlag = true;
		while(true) {
			paramMap.put("current_page", pageNo);
			request.addApiParameter("result", JSON.toJSONString(paramMap));

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
			j.put("shopId", nextLevelId);
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
