package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;

import java.util.List;

/**
 * 速卖通海外托管商品列表拉取。
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOverseasManagedProductApiInitHandler implements DmpInputApiInitHandler {

	private static final int PAGE_SIZE = 20;

	@Resource
	private AliExpressOrderService aliExpressOrderService;

	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
		AliExpressShopInfoDTO shopInfo = aliExpressOrderService.getShopInfoByShopId(dmpInputApiInitRequest.getNextLevelId());
		IopClient client = new IopClientImpl(shopInfo.getBaseUrl(), shopInfo.getClientId(), shopInfo.getClientSecret());
		String token = shopInfo.getToken();
		AliExpressOverseasManagedProductHelper.SellerRelation relation =
				AliExpressOverseasManagedProductHelper.resolveSellerRelation(client, token);

		JSONArray products = new JSONArray();
		int pageNo = 1;
		while (true) {
			IopRequest request = buildRequest(dmpInputApiInitRequest, relation, pageNo);
			IopResponse response = AliExpressOverseasManagedProductHelper.execute(client, request, token, AliExpressOverseasManagedProductHelper.PRODUCT_LIST_API);
			JSONObject payload = AliExpressOverseasManagedProductHelper.unwrapResult(response.getBody(), AliExpressOverseasManagedProductHelper.PRODUCT_LIST_RESPONSE_KEY);
			AliExpressOverseasManagedProductHelper.assertSuccess(payload, AliExpressOverseasManagedProductHelper.PRODUCT_LIST_API);
			JSONObject data = payload.getJSONObject("data");
			if (data == null) {
				data = payload;
			}
			JSONArray currentProducts = AliExpressOverseasManagedProductHelper.findArray(data, "product_list", "productList");
			if (currentProducts.isEmpty()) {
				break;
			}
			products.addAll(currentProducts);
			Integer totalPage = firstInteger(data, payload, "total_page", "totalPage");
			if (totalPage == null || pageNo >= totalPage) {
				break;
			}
			pageNo++;
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(products.toJSONString());
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	private IopRequest buildRequest(DmpInputApiInitRequest requestDTO,
			AliExpressOverseasManagedProductHelper.SellerRelation relation, int pageNo) {
		IopRequest request = new IopRequest();
		request.setApiName(AliExpressOverseasManagedProductHelper.PRODUCT_LIST_API);
		request.addApiParameter("simplify", "true");
		request.addApiParameter("channel_seller_id", relation.getChannelSellerId());
		request.addApiParameter("channel", relation.getChannel());
		request.addApiParameter("page_size", String.valueOf(PAGE_SIZE));
		request.addApiParameter("current_page", String.valueOf(pageNo));

		Map<String, Object> searchCondition = new HashMap<>();
		searchCondition.put("product_status", "ONLINE");
		String startTime = AliExpressOverseasManagedProductHelper.formatTime(requestDTO.getStartTime());
		String endTime = AliExpressOverseasManagedProductHelper.formatTime(requestDTO.getEndTime());
		if (org.apache.commons.lang3.StringUtils.isNotBlank(startTime)) {
			searchCondition.put("update_after", startTime);
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(endTime)) {
			searchCondition.put("update_before", endTime);
		}
		request.addApiParameter("search_condition_do", JSON.toJSONString(searchCondition));
		return request;
	}

	private Integer firstInteger(JSONObject first, JSONObject second, String... keys) {
		for (String key : keys) {
			Integer value = first == null ? null : first.getInteger(key);
			if (value != null) {
				return value;
			}
			value = second == null ? null : second.getInteger(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
