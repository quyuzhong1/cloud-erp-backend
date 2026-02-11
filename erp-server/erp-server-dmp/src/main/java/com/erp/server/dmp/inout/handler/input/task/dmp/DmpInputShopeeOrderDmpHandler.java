package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.sdk.oms.shopee.enums.OrderStatusEnum;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp处理子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputShopeeOrderDmpHandler extends DmpInputChildDataToParentDmpHandler{

	public static final String SHOPEE_ORDER_SHIPPING_DATA = "Shopee_orderShipping_data";

	public static final String SHOPEE_ORDER_DETAIL_DATA = "Shopee_orderDetail_data";
	
	public static final String SHOPEE_ESCROW_DATA = "Shopee_escrow_data";

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		// 明细信息
		List<Map<String, Object>> detailMongoData = new ArrayList<>();
		// 获取配送信息
		List<Map<String, Object>> shipmentMongoData = new ArrayList<>();
		
		List<Map<String, Object>> paymentMongoData = new ArrayList<>();
		List<ParamData> paramDataList = new ArrayList<>();
		Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
		if(CollUtil.isNotEmpty(keySet)) {
			List<String> orderIdList = new ArrayList<>();
			for(List<Map<String, Object>> key : keySet) {
				orderIdList.addAll(key.stream().map(f -> f.get("order_sn").toString()).collect(Collectors.toList()));
			}
			paramDataList.add(new ParamData("order_sn", "order_sn", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			shipmentMongoData = mongoService.findMongoData(paramDataList, SHOPEE_ORDER_SHIPPING_DATA);
			detailMongoData = mongoService.findMongoData(paramDataList, SHOPEE_ORDER_DETAIL_DATA);
			paymentMongoData = mongoService.findMongoData(paramDataList, SHOPEE_ESCROW_DATA);
		}
		Map<String, List<Map<String, Object>>> orderSnShipmentMaps = shipmentMongoData.stream().collect(Collectors.groupingBy(f -> f.get("order_sn").toString()));
		Map<String, List<Map<String, Object>>> orderSnDetailMaps = detailMongoData.stream().collect(Collectors.groupingBy(f -> f.get("order_sn").toString()));
		Map<String, List<Map<String, Object>>> orderSnEscrowMaps = paymentMongoData.stream().collect(Collectors.groupingBy(f -> f.get("order_sn").toString()));


		ZoneId zone = ZoneId.systemDefault();
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				List<Map<String, Object>> detailMapsList = orderSnDetailMaps.get(dmpDataMap.getOrDefault("thirdCode", "").toString());
				if (CollectionUtils.isEmpty(detailMapsList)){
					ServiceException.runError("明细信息为空");
				}
				// 获取 mongoUpdateTime 最大时间的 Map
				Map<String, Object> detailMaps = detailMapsList.stream()
						.max(Comparator.comparing(map -> (String) map.get(DmpInputMongoHandler.MONGO_BASE_MONGOUPDATETIME)))
						.orElse(null);
				if (null == detailMaps){
					ServiceException.runError("最新明细信息为空");
				}
				
				// 标签json
				JSONObject labelJsonObject = new JSONObject();
				// 配送方式
				String fulfillmentFlagStr = detailMaps.getOrDefault("fulfillment_flag", "").toString();
				// 是否平台仓
				boolean isPlatformWarehouseOrder = false;
				// ERP配送类型
				String logisticType = "";
				String deliveryType = "";
				if ("fulfilled_by_shopee".equalsIgnoreCase(fulfillmentFlagStr)) {
					// 平台仓订单
					isPlatformWarehouseOrder = true;
					logisticType = "platformWarehouse";
				} else if ("fulfilled_by_cb_seller".equalsIgnoreCase(fulfillmentFlagStr) || ("fulfilled_by_local_seller".equalsIgnoreCase(fulfillmentFlagStr))){
					// 自发货配送
					// 解析ERP配送类型
					// 虾皮 fulfillment_flag =fulfilled_by_cb_seller/fulfilled _by_local_seller时  v2.logistics.get_shipping_partameter 接口 返回  dropoff / pickup 时为中转仓标识transitWarehouse
					//虾皮 fulfillment_flag =fulfilled_by_cb_seller/fulfilled _by_local_seller时  v2.logistics.get_shipping_partameter 接口 返回 non_intergrated  时为 自发货标识 selfShipment
					deliveryType = parseDeliveryTypeType(dmpDataMap, orderSnShipmentMaps);
					if(StringUtils.isNotBlank(deliveryType)){
						logisticType = "non_integrated".equalsIgnoreCase(deliveryType) ? "selfShipment" : "transitWarehouse";
					}
				} else {
					ServiceException.runError("未知配送方式fulfillment_flag=" + fulfillmentFlagStr);
				}
				labelJsonObject.put("logisticType", logisticType);
				labelJsonObject.put("deliveryType", deliveryType);
				labelJsonObject.put("isPlatformWarehouseOrder", isPlatformWarehouseOrder);
				// 原始配送
				labelJsonObject.put("logisticsWarehouseType", fulfillmentFlagStr);
				dmpDataMap.put("extendData", labelJsonObject.toJSONString());

				// 订单状态
				Object order_status = dmpDataMap.get("order_status");
				if(order_status != null) {
					boolean isCancel = Boolean.FALSE;
					String deliveryStatus = "";
					String orderStatus = "";
					boolean invalidStatus = Boolean.FALSE;
					String platformOriginalStatus = order_status.toString();
					if (OrderStatusEnum.UNPAID.getCode().equals(platformOriginalStatus)) {
						deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
						orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
			        } else if (OrderStatusEnum.READY_TO_SHIP.getCode().equals(platformOriginalStatus)){
						if (isPlatformWarehouseOrder) {
							// 平台仓
							deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
							orderStatus = ApproveStatusEnum.APPROVE.getCode();
						} else {
							// 自发货
							deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
							orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
						}
			        } else if (OrderStatusEnum.PROCESSED.getCode().equals(platformOriginalStatus) || OrderStatusEnum.RETRY_SHIP.getCode().equals(platformOriginalStatus)) {
			        	if (isPlatformWarehouseOrder) {
							deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
							orderStatus = ApproveStatusEnum.APPROVE.getCode();
						} else {
							deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
							orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
						}
			        } else if (OrderStatusEnum.SHIPPED.getCode().equals(platformOriginalStatus) || OrderStatusEnum.TO_CONFIRM_RECEIVE.getCode().equals(platformOriginalStatus)) {
			            //已完成之前 全为待发货
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
			        	orderStatus = ApproveStatusEnum.APPROVE.getCode();
			        }  else if (OrderStatusEnum.IN_CANCEL.getCode().equals(platformOriginalStatus)) {
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_FROZEN.getCode();
			        	orderStatus = ApproveStatusEnum.REJECT.getCode();
			            isCancel = Boolean.TRUE;
			        } else if (OrderStatusEnum.CANCELLED.getCode().equals(platformOriginalStatus)) {
			            // 作废状态（false未作废，true已作废）
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
						if (isPlatformWarehouseOrder){
							orderStatus = ApproveStatusEnum.APPROVE.getCode();
						} else {
							orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
						}
			        	invalidStatus = Boolean.TRUE;
			            isCancel = Boolean.TRUE;
			        } else if (OrderStatusEnum.INVOICE_PENDING.getCode().equals(platformOriginalStatus)) {
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
			        	orderStatus = ApproveStatusEnum.APPROVE.getCode();
			        }else if (OrderStatusEnum.TO_RETURN.getCode().equals(platformOriginalStatus) || OrderStatusEnum.COMPLETED.getCode().equals(platformOriginalStatus)) {
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
			        	orderStatus = ApproveStatusEnum.APPROVE.getCode();
			        }
					dmpDataMap.put("isCancel", isCancel);
					dmpDataMap.put("deliveryStatus", deliveryStatus);
					dmpDataMap.put("orderStatus", orderStatus);
					dmpDataMap.put("invalidStatus", invalidStatus);
					dmpDataMap.put("platformOriginalStatus", platformOriginalStatus);
				}
				Object pay_time = dmpDataMap.get("pay_time");
				if(pay_time != null) {
					Long payTime = Long.valueOf(pay_time.toString());
					if (Objects.nonNull(payTime) && payTime.compareTo(0L) > 0) {
			            Instant instant2 = Instant.ofEpochSecond(payTime);
			            // 付款时间
			            dmpDataMap.put("payTime", LocalDateTime.ofInstant(instant2, zone));
			            dmpDataMap.put("payStatus", true);
			        }
				}else {
					dmpDataMap.put("payStatus", false);
				}
				
				Object package_list = dmpDataMap.get("package_list");
				if(package_list != null) {
					List<Map<String, Object>> packageList = (List<Map<String, Object>>)package_list;
					List<String> collect = packageList.stream().map(p -> p.get("package_number").toString()).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
					labelJsonObject.put("package_number", String.join(",", collect));
		            dmpDataMap.put("extendData", labelJsonObject.toJSONString());
				}
				
				Object ship_by_date = dmpDataMap.get("ship_by_date");
				if(ship_by_date != null) {
					Long shipByDate = Long.valueOf(ship_by_date.toString());
					if(shipByDate.compareTo(0L) > 0) {
						Instant instant = Instant.ofEpochSecond(shipByDate);
						dmpDataMap.put("deliveryTime", LocalDateTime.ofInstant(instant, zone));
					}
				}
				
				Object create_time = dmpDataMap.get("create_time");
				if(create_time != null) {
					Long createTime = Long.valueOf(create_time.toString());
					if(createTime.compareTo(0L) > 0) {
						Instant instant = Instant.ofEpochSecond(createTime);
						dmpDataMap.put("platformCreateTime", LocalDateTime.ofInstant(instant, zone));
					}
				}else{
					create_time = detailMaps.get("create_time");
					if(create_time != null) {
						Long createTime = Long.valueOf(create_time.toString());
						if(createTime.compareTo(0L) > 0) {
							Instant instant = Instant.ofEpochSecond(createTime);
							dmpDataMap.put("platformCreateTime", LocalDateTime.ofInstant(instant, zone));
						}
					}
				}
				Object update_time = dmpDataMap.get("update_time");
				if(update_time != null) {
					Long updateTime = Long.valueOf(update_time.toString());
					if(updateTime.compareTo(0L) > 0) {
						Instant instant = Instant.ofEpochSecond(updateTime);
						dmpDataMap.put("platformUpdateTime", LocalDateTime.ofInstant(instant, zone));
					}
				}else{
					update_time = detailMaps.get("update_time");
					if(update_time != null) {
						Long updateTime = Long.valueOf(update_time.toString());
						if(updateTime.compareTo(0L) > 0) {
							Instant instant = Instant.ofEpochSecond(updateTime);
							dmpDataMap.put("platformUpdateTime", LocalDateTime.ofInstant(instant, zone));
						}
					}
				}
				
				List<Map<String, Object>> escrowMapsList = orderSnEscrowMaps.get(dmpDataMap.getOrDefault("thirdCode", "").toString());
				if(CollUtil.isNotEmpty(escrowMapsList)) {
					Map<String, Object> escrowMaps = escrowMapsList.stream()
							.max(Comparator.comparing(map -> (String) map.get(DmpInputMongoHandler.MONGO_BASE_MONGOUPDATETIME)))
							.orElse(null);
					if(escrowMaps != null) {
						Object order_income = escrowMaps.get("order_income");
						if(order_income != null) {
							Map<String, Object> orderIncome = (Map<String, Object>) order_income;
							dmpDataMap.put("totalDiscount", orderIncome.get("payment_promotion"));
						}
					}
				}
				dmpDataMap.put("nextLevelId", nextLevelId);
				Object item_list_obj = dmpDataMap.get("item_list");
				if(item_list_obj != null) {
					BigDecimal allAmount = BigDecimal.ZERO;
					List<Map<String, Object>> item_list = (List<Map<String, Object>>)item_list_obj;
					for(Map<String, Object> item : item_list) {
						Object model_original_price_obj = item.get("model_original_price");
						Object model_quantity_purchased_obj = item.get("model_quantity_purchased");
						if(model_original_price_obj != null && model_quantity_purchased_obj != null) {
							allAmount = allAmount.add(new BigDecimal(model_original_price_obj.toString()).multiply(new BigDecimal(model_quantity_purchased_obj.toString())));
						}
					}
					dmpDataMap.put("allAmount", allAmount);
				}
			}
		}
	}

	/**
	 * 解析发货类型
	 */
	private static String parseDeliveryTypeType(TreeMap<String, Object> dmpDataMap, Map<String, List<Map<String, Object>>> orderSnShipmentMapsList) {

		// 自发货（跨境卖家） fulfilled_by_cb_seller
		// 自发货（本地卖家）fulfilled_by_local_seller
		List<Map<String, Object>> shipmentDataList = orderSnShipmentMapsList.get(dmpDataMap.getOrDefault("thirdCode", "").toString());
		if (CollectionUtils.isEmpty(shipmentDataList)){
			return "";
		}
		// 获取 mongoUpdateTime 最大时间的 Map
		Map<String, Object> shipmentData = shipmentDataList.stream()
				.max(Comparator.comparing(map -> (String) map.get(DmpInputMongoHandler.MONGO_BASE_MONGOUPDATETIME)))
				.orElse(null);
		if (null == shipmentData){
			ServiceException.runError("最新配送信息为空为空");
		}

		Object infoNeededObj = shipmentData.get("info_needed");
		if (null == infoNeededObj){
			ServiceException.runError("配送信息info_needed为空");
		}
		String deliveryType = "";
		String infoNeededJsonString = JSON.toJSONString(infoNeededObj);
		JSONObject infoNeededjsonObject = JSON.parseObject(infoNeededJsonString);
		if (StringUtils.isBlank(infoNeededJsonString)){
			ServiceException.runError("未知配送信息infoNeeded=" + infoNeededJsonString);
		}
		JSONArray dropoffjsonArray = infoNeededjsonObject.getJSONArray("dropoff");
		JSONArray pickupjsonArray = infoNeededjsonObject.getJSONArray("pickup");
		JSONArray nonIntegratedjsonArray = infoNeededjsonObject.getJSONArray("non_integrated");
		// 字段存在判断类型
		if (null != dropoffjsonArray){
			deliveryType = "dropoff";
		}
		if (null != pickupjsonArray){
			deliveryType = "pickup";
		}
		if (null != nonIntegratedjsonArray){
			deliveryType = "non_integrated";
		}
		// 数组有值优先
		if (CollectionUtils.isNotEmpty(dropoffjsonArray)){
			deliveryType = "dropoff";
		}
		if (CollectionUtils.isNotEmpty(pickupjsonArray)){
			deliveryType = "pickup";
		}
		if (CollectionUtils.isNotEmpty(nonIntegratedjsonArray)){
			deliveryType = "non_integrated";
		}
		if (StringUtils.isBlank(deliveryType)){
			ServiceException.runError("解析到未知的配送信息deliveryType=" + infoNeededJsonString);
		}
		return deliveryType;
	}
}
