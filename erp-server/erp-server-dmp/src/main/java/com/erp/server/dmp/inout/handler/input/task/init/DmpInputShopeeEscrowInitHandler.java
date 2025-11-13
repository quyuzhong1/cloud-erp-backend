package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.service.ShopeeOrderService;

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
public class DmpInputShopeeEscrowInitHandler extends DmpInputInitHandler{

	@Resource
	private CfgAppClientService cfgAppClientService;
	@Resource
    private ShopeeOrderService shopeeOrderService;
	
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
		
		List<String> itemIds = findMongoData.stream().map(f -> f.get("order_sn").toString()).collect(Collectors.toList());
		
		AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
		List<CfgAppClientEntity> cfgAppClientEntityList = cfgAppClientService.lambdaQuery()
			.eq(CfgAppClientEntity::getBusinessType, appClientEnum.getBusinessType())
			.eq(CfgAppClientEntity::getDictPlatform, appClientEnum.getPlatform())
			.eq(CfgAppClientEntity::getPlatformType, appClientEnum.getPlatformType())
			.list();
		if(CollUtil.isEmpty(cfgAppClientEntityList)) {
			throw new ServiceException("shopee应用未配置");
		}
		
		CfgAppClientEntity cfgAppClientEntity = cfgAppClientEntityList.get(0);
		List<ShopAuthEntity> shopAuthEntityList = FeignQuery.create(ShopAuthEntity.class).eq(ShopAuthEntity::getShopId, findMongoData.get(0).get("nextLevelId").toString()).list();
		if(CollUtil.isEmpty(shopAuthEntityList)) {
			throw new ServiceException("shopee授权未配置");
		}
		ShopAuthEntity shopAuthEntity = shopAuthEntityList.get(0);
		OrderRequest orderRequest = OrderRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .offset(0)
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .timeFrom(null)
                .timeTo(null)
                .build();
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		List<JSONObject> resultList = new ArrayList<>();

		for(String itemId : itemIds) {
			orderRequest.setOrderSns(itemId);
			ShopeeResponse data = null;
	    	long sleepTime = 1000;
	    	int count = 0;
	    	while(data == null) {
	    		data = this.execute(orderRequest);
	    		if(data == null) {
	    			if(count == 10) {
	    				throw new ServiceException("调用shopee订单费用接口重试" + count + "失败");
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
	    	JSONObject result = data.getResponse();
	    	resultList.add(result);
		}
    	
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONUtil.toJsonStr(resultList));
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
    
		return dmpInputTaskInitDTOList;
	}
	
	private ShopeeResponse execute(OrderRequest orderRequest){
		ShopeeResponse response = null;
		try {
			response = shopeeOrderService.getEscrowDetail(orderRequest);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用shopee订单费用接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		
		if(response != null) {
			String error = response.getError();
			if(StringUtils.isNotBlank(error)) {
//				response = JSON.parseObject("{\"error\":\"\",\"message\":\"\",\"request_id\":\"9c68e1a0b3d413cc86c9dd5435a3e521\",\"response\":{\"buyer_payment_info\":{\"buyer_payment_method\":\"ShopeePay Linked Bank Account\",\"buyer_service_fee\":0,\"buyer_tax_amount\":0,\"buyer_total_amount\":48440,\"credit_card_promotion\":0,\"icms_tax_amount\":0,\"import_tax_amount\":0,\"initial_buyer_txn_fee\":0,\"insurance_premium\":0,\"iof_tax_amount\":0,\"is_paid_by_credit_card\":false,\"merchant_subtotal\":52600,\"seller_voucher\":0,\"shipping_fee\":2000,\"shipping_fee_sst_amount\":0,\"shopee_coins_redeemed\":-900,\"shopee_voucher\":-5260},\"buyer_user_name\":\"ng_phuong48\",\"order_income\":{\"actual_shipping_fee\":0,\"buyer_paid_shipping_fee\":2000,\"buyer_payment_method\":\"ShopeePay Linked Bank Account\",\"buyer_total_amount\":48440,\"buyer_transaction_fee\":0,\"campaign_fee\":0,\"coins\":400,\"commission_fee\":0,\"cost_of_goods_sold\":23700,\"credit_card_promotion\":0,\"credit_card_transaction_fee\":848,\"cross_border_tax\":0,\"delivery_seller_protection_fee_premium_amount\":0,\"drc_adjustable_refund\":25510,\"escrow_amount\":37719,\"escrow_amount_after_adjustment\":37719,\"escrow_tax\":0,\"estimated_shipping_fee\":17000,\"final_escrow_product_gst\":0,\"final_escrow_shipping_gst\":0,\"final_product_protection\":0,\"final_product_vat_tax\":0,\"final_return_to_seller_shipping_fee\":0,\"final_shipping_fee\":15000,\"final_shipping_vat_tax\":0,\"fsf_seller_protection_fee_claim_amount\":0,\"instalment_plan\":\"N/A\",\"items\":[{\"activity_id\":0,\"activity_type\":\"\",\"ams_commission_fee\":0,\"discount_from_coin\":300,\"discount_from_voucher_seller\":0,\"discount_from_voucher_shopee\":1470,\"discounted_price\":14700,\"is_b2c_shop_item\":false,\"is_main_item\":false,\"item_id\":24173211461,\"item_name\":\"HANBOLI Dụng cụ uốn mi mini cầm tay bằng thép không gỉ\",\"item_sku\":\"SXY472-08\",\"model_id\":99542389678,\"model_name\":\"Đen\",\"model_sku\":\"SXY472001\",\"original_price\":21000,\"quantity_purchased\":1,\"seller_discount\":6300,\"selling_price\":14700,\"shopee_discount\":0},{\"activity_id\":0,\"activity_type\":\"\",\"ams_commission_fee\":0,\"discount_from_coin\":0,\"discount_from_voucher_seller\":0,\"discount_from_voucher_shopee\":300,\"discounted_price\":3000,\"is_b2c_shop_item\":false,\"is_main_item\":false,\"item_id\":24822331570,\"item_name\":\"HANBOLI Phim hoạt hình tình yêu lược nhỏ\",\"item_sku\":\"HZMBB0198-08\",\"model_id\":245581210110,\"model_name\":\"Hồng\",\"model_sku\":\"HZMBB0198001\",\"original_price\":4000,\"quantity_purchased\":1,\"seller_discount\":1000,\"selling_price\":3000,\"shopee_discount\":0},{\"activity_id\":0,\"activity_type\":\"\",\"ams_commission_fee\":0,\"discount_from_coin\":500,\"discount_from_voucher_seller\":0,\"discount_from_voucher_shopee\":2890,\"discounted_price\":28900,\"is_b2c_shop_item\":false,\"is_main_item\":false,\"item_id\":25819910186,\"item_name\":\"HANBOLI bộ cọ trang điểm 13 cây\",\"item_sku\":\"SXY482-08\",\"model_id\":232615005081,\"model_name\":\"Hồng\",\"model_sku\":\"SXY482006\",\"original_price\":41000,\"quantity_purchased\":1,\"seller_discount\":12100,\"selling_price\":28900,\"shopee_discount\":0},{\"activity_id\":0,\"activity_type\":\"\",\"ams_commission_fee\":0,\"discount_from_coin\":100,\"discount_from_voucher_seller\":0,\"discount_from_voucher_shopee\":600,\"discounted_price\":6000,\"is_b2c_shop_item\":false,\"is_main_item\":false,\"item_id\":25823200241,\"item_name\":\"HANBOLI Đơn đa chức năng cọ môi tròn\",\"item_sku\":\"SXY481-08\",\"model_id\":147257500156,\"model_name\":\"Cọ môi\",\"model_sku\":\"SXY481001\",\"original_price\":8000,\"quantity_purchased\":1,\"seller_discount\":2000,\"selling_price\":6000,\"shopee_discount\":0}],\"order_ams_commission_fee\":0,\"order_chargeable_weight\":700,\"order_discounted_price\":23700,\"order_original_price\":74000,\"order_seller_discount\":50300,\"order_selling_price\":23700,\"original_cost_of_goods_sold\":52600,\"original_price\":33000,\"original_shopee_discount\":0,\"overseas_return_service_fee\":0,\"payment_promotion\":"+ RandomUtil.randomDouble(100) +",\"prorated_coins_value_offset_return_items\":0,\"prorated_payment_channel_promo_bank_offset_return_items\":0,\"prorated_payment_channel_promo_shopee_offset_return_items\":0,\"prorated_seller_voucher_offset_return_items\":0,\"prorated_shopee_voucher_offset_return_items\":0,\"reverse_shipping_fee\":0,\"reverse_shipping_fee_sst\":0,\"rsf_seller_protection_fee_claim_amount\":0,\"sales_tax_on_lvg\":0,\"seller_coin_cash_back\":0,\"seller_discount\":9300,\"seller_lost_compensation\":0,\"seller_return_refund\":-28900,\"seller_shipping_discount\":0,\"seller_transaction_fee\":848,\"seller_voucher_code\":[],\"service_fee\":2133,\"shipping_fee_discount_from_3pl\":0,\"shipping_fee_sst\":0,\"shipping_seller_protection_fee_amount\":0,\"shopee_discount\":0,\"shopee_shipping_rebate\":15000,\"total_adjustment_amount\":0,\"vat_on_imported_goods\":0,\"voucher_from_seller\":0,\"voucher_from_shopee\":2370,\"withholding_tax\":0},\"order_sn\":\""+ orderRequest.getOrderSns() +"\",\"return_order_sn_list\":[\"2405060KVC6EQB3\"]}}"
//						, ShopeeResponse.class);
				throw new ServiceException("调用shopee订单费用接口报错，错误原因：" + response.getMessage());
			}
		}
		
		return response;
	}
}
