package com.erp.server.dmp.inout.handler.input.task.dmp;

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

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		// 明细信息
		List<Map<String, Object>> detailMongoData = new ArrayList<>();
		// 获取配送信息
		List<Map<String, Object>> shipmentMongoData = new ArrayList<>();
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
		}
		Map<String, Map<String, Object>> orderSnShipmentMaps = shipmentMongoData.stream().collect(Collectors.toMap(f -> f.get("order_sn").toString(), f -> f));
		Map<String, Map<String, Object>> orderSnDetailMaps = detailMongoData.stream().collect(Collectors.toMap(f -> f.get("order_sn").toString(), f -> f));


		ZoneId zone = ZoneId.systemDefault();
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				Map<String, Object> detailMaps = orderSnDetailMaps.get(dmpDataMap.getOrDefault("thirdCode", "").toString());
				if (null == detailMaps){
					ServiceException.runError("明细信息为空");
				}

				// 标签json
				Map<String, Object> labelMap = new HashMap<>();
				// 配送方式
				String fulfillmentFlagStr = detailMaps.getOrDefault("fulfillment_flag", "").toString();
				// 是否平台仓
				boolean isPlatformWarehouseOrder = false;
				// ERP配送类型
				String logisticType = "";
				if ("fulfilled_by_shopee".equalsIgnoreCase(fulfillmentFlagStr)) {
					// 平台仓订单
					isPlatformWarehouseOrder = true;
					logisticType = "platformWarehouse";
				} else if ("fulfilled_by_cb_seller".equalsIgnoreCase(fulfillmentFlagStr) || ("fulfilled_by_local_seller".equalsIgnoreCase(fulfillmentFlagStr))){
					// 自发货配送
					// 解析ERP配送类型
					logisticType = parseLogisticType(dmpDataMap, orderSnShipmentMaps, logisticType, fulfillmentFlagStr);
				} else {
					ServiceException.runError("未知配送方式fulfillment_flag=" + fulfillmentFlagStr);
				}
				labelMap.put("logisticType", logisticType);
				labelMap.put("isPlatformWarehouseOrder", isPlatformWarehouseOrder);

				// 订单状态
				Object order_status = dmpDataMap.get("order_status");
				// 原始状态
				labelMap.put("sourceOrderStatus", order_status);
				// 原始配送
				labelMap.put("fulfillmentFlag", fulfillmentFlagStr);

				dmpDataMap.put("extendData", JSON.toJSONString(labelMap));

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
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
			        	orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
			        } else if (OrderStatusEnum.PROCESSED.getCode().equals(platformOriginalStatus) || OrderStatusEnum.RETRY_SHIP.getCode().equals(platformOriginalStatus)) {
			        	deliveryStatus = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
			            orderStatus = ApproveStatusEnum.APPROVE.getCode();
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
			        	orderStatus = ApproveStatusEnum.WAIT_SUBMIT.getCode();
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
		            JSONObject jsonObject = new JSONObject();
		            jsonObject.put("package_number", String.join(",", collect));
		            dmpDataMap.put("extendData", jsonObject.toJSONString());
				}
				
				Object ship_by_date = dmpDataMap.get("ship_by_date");
				if(ship_by_date != null) {
					Long shipByDate = Long.valueOf(ship_by_date.toString());
					if(shipByDate.compareTo(0L) > 0) {
						Instant instant = Instant.ofEpochSecond(shipByDate);
						dmpDataMap.put("deliveryTime", LocalDateTime.ofInstant(instant, zone));
					}
				}
			}
		}
	}

	/**
	 * 解析物流类型
	 */
	private static String parseLogisticType(TreeMap<String, Object> dmpDataMap, Map<String, Map<String, Object>> orderSnShipmentMaps, String logisticType, String fulfillmentFlagStr) {
		// 自发货（跨境卖家） fulfilled_by_cb_seller
		// 自发货（本地卖家）fulfilled_by_local_seller
		Map<String, Object> shipmentData = orderSnShipmentMaps.get(dmpDataMap.getOrDefault("thirdCode", "").toString());
		if (shipmentData.isEmpty()){
			ServiceException.runError("配送信息为空");
		}
		Object infoNeededObj = shipmentData.get("info_needed");
		if (null == infoNeededObj){
			ServiceException.runError("配送信息info_needed为空");
		}
		String infoNeededJsonString = JSON.toJSONString(infoNeededObj);
		JSONObject infoNeededjsonObject = JSON.parseObject(infoNeededJsonString);
		JSONArray dropoffjsonArray = infoNeededjsonObject.getJSONArray("dropoff");
		JSONArray pickupjsonArray = infoNeededjsonObject.getJSONArray("pickup");
		JSONArray nonIntegratedjsonArray = infoNeededjsonObject.getJSONArray("non_integrated");
		// 字段存在判断类型
		if (null != dropoffjsonArray){
			logisticType = "transitWarehouse";
		}
		if (null != pickupjsonArray){
			logisticType = "transitWarehouse";
		}
		if (null != nonIntegratedjsonArray){
			logisticType = "selfShipment";
		}
		// 数组有值优先
		if (CollectionUtils.isNotEmpty(dropoffjsonArray)){
			logisticType = "transitWarehouse";
		}
		if (CollectionUtils.isNotEmpty(pickupjsonArray)){
			logisticType = "transitWarehouse";
		}
		if (CollectionUtils.isNotEmpty(nonIntegratedjsonArray)){
			logisticType = "selfShipment";
		}
		if (StringUtils.isBlank(infoNeededJsonString)){
			ServiceException.runError("未知配送信息infoNeeded=" + fulfillmentFlagStr);
		}
		return logisticType;
	}
}
