package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		ZoneId zone = ZoneId.systemDefault();
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
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
}
