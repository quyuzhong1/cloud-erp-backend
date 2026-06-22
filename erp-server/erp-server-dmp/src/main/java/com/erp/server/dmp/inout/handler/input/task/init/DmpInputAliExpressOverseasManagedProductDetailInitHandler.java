package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.AliExpressOverseasManagedProductHelper;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * 速卖通海外托管商品详情拉取。
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOverseasManagedProductDetailInitHandler extends DmpInputInitHandler {

	@Resource
	private AliExpressOrderService aliExpressOrderService;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<Map<String, Object>> parentProducts = getParentProducts();
		if (CollUtil.isEmpty(parentProducts)) {
			return new ArrayList<>();
		}
		String shopId = String.valueOf(parentProducts.get(0).get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
		AliExpressShopInfoDTO shopInfo = aliExpressOrderService.getShopInfoByShopId(shopId);
		IopClient client = new IopClientImpl(shopInfo.getBaseUrl(), shopInfo.getClientId(), shopInfo.getClientSecret());
		String token = shopInfo.getToken();
		AliExpressOverseasManagedProductHelper.SellerRelation relation =
				AliExpressOverseasManagedProductHelper.resolveSellerRelation(client, token);

		List<DmpInputTaskInitDTO> result = new ArrayList<>();
		for (Map<String, Object> product : parentProducts) {
			String productId = String.valueOf(product.get("product_id"));
			if (StringUtils.isBlank(productId) || "null".equalsIgnoreCase(productId)) {
				continue;
			}
			IopRequest request = buildRequest(relation, productId);
			IopResponse response = AliExpressOverseasManagedProductHelper.execute(client, request, token, AliExpressOverseasManagedProductHelper.PRODUCT_DETAIL_API);
			JSONObject payload = AliExpressOverseasManagedProductHelper.unwrapData(response.getBody(), AliExpressOverseasManagedProductHelper.PRODUCT_DETAIL_RESPONSE_KEY);
			AliExpressOverseasManagedProductHelper.assertSuccess(payload, AliExpressOverseasManagedProductHelper.PRODUCT_DETAIL_API);
			JSONObject detail = AliExpressOverseasManagedProductHelper.findObject(payload, "local_service_product_dto", "localServiceProductDto");
			if (detail == null) {
				detail = payload;
			}
			detail.put("productTitle", product.get("title"));
			detail.put("parentProductId", productId);
			result.add(DmpInputTaskInitDTO.initMsg(detail.toJSONString()));
		}
		return result;
	}

	private List<Map<String, Object>> getParentProducts() {
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if (StringUtils.isBlank(parentStorageName)) {
			return new ArrayList<>();
		}
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
				DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
		return mongoService.findMongoData(paramDataList, parentStorageName);
	}

	private IopRequest buildRequest(AliExpressOverseasManagedProductHelper.SellerRelation relation, String productId) {
		IopRequest request = new IopRequest();
		request.setApiName(AliExpressOverseasManagedProductHelper.PRODUCT_DETAIL_API);
		request.addApiParameter("simplify", "true");
		request.addApiParameter("channel_seller_id", relation.getChannelSellerId());
		request.addApiParameter("channel", relation.getChannel());
		request.addApiParameter("product_id", productId);
		return request;
	}
}
