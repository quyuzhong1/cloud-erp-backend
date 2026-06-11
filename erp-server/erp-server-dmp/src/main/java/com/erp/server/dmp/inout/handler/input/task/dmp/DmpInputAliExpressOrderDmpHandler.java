package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.SoB2cNfeStatusEnum;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.AmountInfo;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderDmpHandler extends DmpInputDbConvertDmpHandler{

	@Autowired
	private DmpSoInfoService dmpSoInfoService;
	
	@Autowired
	private DmpSoDetailService dmpSoDetailService;
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		
		List<Map<String, Object>> findMongoData = new ArrayList<>();
		List<ParamData> paramDataList = new ArrayList<>();
		Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
		if(CollUtil.isNotEmpty(keySet)) {
			List<String> orderIdList = new ArrayList<>();
			for(List<Map<String, Object>> key : keySet) {
				orderIdList.addAll(key.stream().map(f -> f.get("order_id").toString()).collect(Collectors.toList()));
			}
			paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
			
			List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
					.in(DmpSoInfoEntity::getThirdCode, orderIdList)
					.in(DmpSoInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode() , DmpBasicSystemCodeEnum.MABANG.getCode()))
					.select(DmpSoInfoEntity::getId)
					.list();
			if(CollUtil.isNotEmpty(list)) {
				List<String> ids = list.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
				dmpSoInfoService.removeByIds(ids);
				dmpSoDetailService.lambdaUpdate()
						.in(DmpSoDetailEntity::getMainId, ids)
						.eq(DmpSoDetailEntity::getIsDeleted, false)
						.set(DmpSoDetailEntity::getIsDeleted, true)
						.update();
			}
		}
		
		Map<String, Map<String, Object>> orderIdDetailMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
		BigDecimal payAmount = BigDecimal.ZERO;
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
			AliExpressOrder sourceOrder = JSON.parseObject(JSON.toJSONString(mongoDataMap), AliExpressOrder.class);
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				dmpDataMap.put("shopId", nextLevelId);
				Object payAmountObj = mongoDataMap.get("pay_amount");
				if(payAmountObj != null) {
					Map<String, Object> payAmountMap = (Map)payAmountObj;
					if (payAmountMap.get("amount") != null) {
						payAmount = MathUtil.valueOf(payAmountMap.get("amount"));
						dmpDataMap.put("payAmount", payAmount);
					}
					dmpDataMap.put("currencyCode", payAmountMap.get("currency_code"));
				}

				Object logisticInfoListObj = null;
				Map<String, Object> detailData = orderIdDetailMaps.get(dmpDataMap.get("thirdCode"));
				if(detailData != null) {
					Object logistics_amount_obj = detailData.get("logistics_amount");
					if(logistics_amount_obj != null) {
						Map<String , Object> logistics_amount = (Map)logistics_amount_obj;
						Object amountObj = logistics_amount.get("amount");
						if(amountObj != null) {
							dmpDataMap.put("shippingAmount", amountObj);
						}
					}
					Object memo = detailData.get("memo");
					if(memo != null) {
						dmpDataMap.put("buyerRemark", memo);
					}

					Object orderAmountObj = detailData.get("order_amount");
					if(orderAmountObj != null) {
						Map<String, Object> orderAmountMap = (Map) orderAmountObj;
						Object amount = orderAmountMap.get("amount");
						if (amount != null) {
//							dmpDataMap.put("totalDiscount", MathUtil.valueOf(amount).subtract(payAmount));
							dmpDataMap.put("allAmount", amount);
						}
					}

					// 总优惠金额
					Object promotionFeeObj = detailData.get("promotion_fee");
					if(promotionFeeObj != null) {
						Map<String, Object> promotionFeeMap = (Map) promotionFeeObj;
						Object promotionFee = promotionFeeMap.get("amount");
						if (promotionFee != null) {
							dmpDataMap.put("totalDiscount",promotionFee);
						}
					}

					// 税后支付金额
					Object newSellerOrderAmountObj = detailData.get("new_seller_order_amount");
					if(newSellerOrderAmountObj != null) {
						Map<String, Object> promotionFeeMap = (Map) newSellerOrderAmountObj;
						Object newSellerOrderAmount = promotionFeeMap.get("amount");
						if (newSellerOrderAmount != null) {
							dmpDataMap.put("afterTaxAmount", newSellerOrderAmount);
						}
					}

					// 买家视角订单金额
					Object actualFeeObj = detailData.get("actual_fee");
					if(actualFeeObj != null) {
						Map<String, Object> actualFeeMap = (Map) actualFeeObj;
						Object actualFee = actualFeeMap.get("amount");
						if (actualFee != null) {
							dmpDataMap.put("actualAmount",actualFee);
						}
						Object actualFeeCurrency = actualFeeMap.get("currency_code");
						if (actualFeeCurrency != null) {
							dmpDataMap.put("actualCurrency",actualFeeCurrency);
						}
					}

					//退款
					Object refundInfoObj = detailData.get("refund_info");
					if(refundInfoObj != null) {
						Map<String, Object> refundInfoMap = (Map) refundInfoObj;
						if (refundInfoMap.get("refund_cash_amt") != null) {
							Map<String, Object> refundCashAmtMap = (Map) refundInfoMap.get("refund_cash_amt");
							if (ObjectUtil.isNotEmpty(refundCashAmtMap)) {
								dmpDataMap.put("totalCancelGoodsAmount", refundCashAmtMap.get("amount"));
								dmpDataMap.put("cancelGoodsCurrency", refundCashAmtMap.get("currency_code"));
							}
						}
					}

					// 标签json
			        Map<String, Object> labelMap = new HashMap<>();
			        //订单明细
			        List<OrderItemDetail> orderItemDetailList = JSON.parseObject(JSON.toJSONString(detailData.get("child_order_list")),new TypeReference<List<OrderItemDetail>>() {}.getType());
			        if (CollectionUtils.isEmpty(orderItemDetailList)) {
			            orderItemDetailList = new ArrayList<>();
			        }
			        Boolean isAliexpressPlatformWarehouseOrder = Boolean.FALSE;
			        if (CollectionUtils.isNotEmpty(orderItemDetailList)) {
			            long count = orderItemDetailList.stream().
			                    filter(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType())).count();
			            if (dmpBasicSystemEntity != null && DmpBasicSystemCodeEnum.ALI_EXPRESS_OVERSEAS_MANAGED.getCode().equals(dmpBasicSystemEntity.getCode())) {
			                isAliexpressPlatformWarehouseOrder = count == orderItemDetailList.size();
			            } else {
			                isAliexpressPlatformWarehouseOrder = count > 0;
			            }
			            
			            dmpDataMap.put("allAmount", orderItemDetailList.stream().map(o -> {
				        	Integer productCount = o.getProductCount();
				        	if(productCount == null || productCount == 0) {
				        		return BigDecimal.ZERO;
				        	}
				        	AmountInfo productPrice = o.getProductPrice();
				        	if(productPrice == null) {
				        		return BigDecimal.ZERO;
				        	}
				        	String amount = productPrice.getAmount();
				        	if(StringUtils.isBlank(amount)) {
				        		return BigDecimal.ZERO;
				        	}
				        	return new BigDecimal(amount).multiply(new BigDecimal(productCount));
				        }).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));

						//NFE发票状态
						Boolean isNeedNfe = false;
						for (OrderItemDetail orderItemDetail : orderItemDetailList) {
							String productAttributes = orderItemDetail.getProductAttributes();
							OrderItemDetail.ChildAttributes childSkus = JSON.parseObject(productAttributes,new TypeReference<OrderItemDetail.ChildAttributes>() {}.getType());
							if(childSkus != null) {
								List<OrderItemDetail.ChildSku> skuList = childSkus.getChildSkus();
								if (CollUtil.isNotEmpty(skuList)) {
									for (OrderItemDetail.ChildSku sku : skuList) {
                                        if ("Ships From".equals(sku.getPName()) && "brazil".equals(sku.getPValue())) {
                                            isNeedNfe = true;
                                            break;
                                        }
									}
								}
							}
						}
						if(isNeedNfe){
							dmpDataMap.put("nfeInvoiceStatus", SoB2cNfeStatusEnum.PENDING.getCode());
						}
			        }
			        labelMap.put("logisticsWarehouseType", orderItemDetailList.stream().map(OrderItemDetail::getLogisticsWarehouseType).collect(Collectors.joining(",")));
			        labelMap.put("isPlatformWarehouseOrder", isAliexpressPlatformWarehouseOrder);
			        String orderStatus = sourceOrder.getOrderStatus();
			        if ("RISK_CONTROL".equals(orderStatus)
			                || "IN_CANCEL".equals(orderStatus)
			                || "IN_FROZEN".equals(orderStatus)
			        ) {
			            labelMap.put("aliexpressStatus", orderStatus);
			        }
			        
			        dmpDataMap.put("extendData", JSON.toJSONString(labelMap));
			        dmpDataMap.put("payStatus", sourceOrder.convertPayStatus().equals(SoB2cPayStatusEnum.ENUM_PAID.getCode()));
					// 订单物流信息
					logisticInfoListObj = detailData.get("logistic_info_list");
					// 发货状态
					dmpDataMap.put("deliveryStatus", sourceOrder.convertBillStatus(isAliexpressPlatformWarehouseOrder, logisticInfoListObj));
			        // 审核状态状态
			        // （ApproveStatus字典类型）
			        dmpDataMap.put("orderStatus", sourceOrder.convertApproveStatus(isAliexpressPlatformWarehouseOrder, logisticInfoListObj));
				}

				// 平台取消
				boolean isCancel = sourceOrder.convertNewCancel(logisticInfoListObj);
				dmpDataMap.put("isCancel", isCancel);
				// 平台冻结
				boolean isFrozen = sourceOrder.convertFrozen();
				dmpDataMap.put("invalidStatus", isCancel && !isFrozen);
			}
		}
	}

	public static void main(String[] args) {
		String a = "[{\"lot_num\":1,\"loan_info\":{\"loan_amount\":{\"amount\":\"1648.53\",\"cent\":164853,\"cent_factor\":100,\"currency\":{\"symbol\":\"￥\",\"numeric_code\":156,\"default_fraction_digits\":2,\"display_name\":\"人民币\",\"currency_code\":\"CNY\"},\"amount_str\":\"1648.53\",\"currency_code\":\"CNY\"},\"loan_time\":\"2025-03-31 20:57:14\"},\"product_count\":2,\"frozen_status\":\"NO_FROZEN\",\"product_price\":{\"amount\":\"1102.01\",\"cent\":110201,\"cent_factor\":100,\"currency\":{\"symbol\":\"￥\",\"numeric_code\":156,\"default_fraction_digits\":2,\"display_name\":\"人民币\",\"currency_code\":\"CNY\"},\"currency_code\":\"CNY\"},\"child_order_id\":\"3051871576433573\",\"order_status\":\"WAIT_BUYER_ACCEPT_GOODS\",\"snapshot_small_photo_path\":\"http://ae01.alicdn.com/kf/A8af77a7fea9a461db4d71b181f8c91924.jpg\",\"product_snap_url\":\"//www.aliexpress.com/snapshot/null.html?orderId=3051871576433573\",\"product_id\":1005008584278402,\"init_order_amt\":{\"amount\":\"2204.02\",\"cent\":220402,\"cent_factor\":100,\"currency\":{\"symbol\":\"￥\",\"numeric_code\":156,\"default_fraction_digits\":2,\"display_name\":\"人民币\",\"currency_code\":\"CNY\"},\"currency_code\":\"CNY\"},\"id\":3051871576433573,\"logistics_type\":\"CAINIAO_STANDARD\",\"product_img_url\":\"A8af77a7fea9a461db4d71b181f8c91924.jpg\",\"logistics_service_name\":\"AliExpress Standard Shipping\",\"goods_prepare_time\":7,\"afflicate_fee_rate\":\"0.03\",\"fund_status\":\"PAY_SUCCESS\",\"product_attributes\":\"{\\\"sku\\\":[{\\\"skuImg\\\":\\\"S9969c61dc922404d9465d294a17ac779B.jpg\\\",\\\"selfDefineValue\\\":\\\"ML100Bi Version\\\",\\\"pName\\\":\\\"Color\\\",\\\"pValueId\\\":29,\\\"pValue\\\":\\\"WHITE\\\",\\\"pId\\\":14,\\\"order\\\":2},{\\\"pName\\\":\\\"Plug Type\\\",\\\"pValueId\\\":200660850,\\\"pValue\\\":\\\"EU\\\",\\\"pId\\\":200009209,\\\"order\\\":3},{\\\"pName\\\":\\\"Ships From\\\",\\\"pValueId\\\":201336100,\\\"pValue\\\":\\\"CHINA\\\",\\\"pId\\\":200007763,\\\"order\\\":1}]}\",\"issue_status\":\"NO_ISSUE\",\"product_name\":\"Ulanzi ML100Bi/ML100RGB 100W COB Video Light with 100W PD Fast Charging for Portrait Photography Livestreaming Short Video Vlog\",\"product_unit\":\"piece\",\"tags\":[],\"escrow_fee_rate\":\"0.025\",\"send_goods_operator\":\"SELLER_SEND_GOODS\",\"logistics_amount\":{\"amount\":\"0.00\",\"cent\":0,\"cent_factor\":100,\"currency\":{\"symbol\":\"￥\",\"numeric_code\":156,\"default_fraction_digits\":2,\"display_name\":\"人民币\",\"currency_code\":\"CNY\"},\"amount_str\":\"0.00\",\"currency_code\":\"CNY\"},\"extend_map\":\"{\\\"rs\\\":\\\"{\\\\\\\"s_c_d\\\\\\\":{\\\\\\\"timeoutType\\\\\\\":72,\\\\\\\"newGood\\\\\\\":true,\\\\\\\"lowSellingGood\\\\\\\":true,\\\\\\\"qualifiedTime\\\\\\\":1743243797464}}\\\",\\\"siteLanguage\\\":\\\"pl_PL\\\",\\\"hscode\\\":\\\"9405423990\\\",\\\"wc\\\":\\\"ae_marketplace\\\"}\",\"order_sort_id\":1,\"sku_code\":\"L174\"}]";

//		List<OrderItemDetail> orderItemDetailList = JSON.parseArray(JSON.toJSONString(a), OrderItemDetail.class);
		List<OrderItemDetail> orderItemDetailList = JSON.parseObject(a,new TypeReference<List<OrderItemDetail>>() {}.getType());
		String b = orderItemDetailList.get(0).getProductAttributes();
//		b = b.substring(1, b.length() - 1);
		OrderItemDetail.ChildAttributes childSkus = JSON.parseObject(b,new TypeReference<OrderItemDetail.ChildAttributes>() {}.getType());

		System.out.println(orderItemDetailList.get(0));
	}
	
}
