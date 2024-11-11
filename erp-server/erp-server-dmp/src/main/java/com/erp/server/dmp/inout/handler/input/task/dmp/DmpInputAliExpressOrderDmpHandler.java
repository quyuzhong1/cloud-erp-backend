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
import com.common.core.utils.MathUtil;
import org.apache.commons.collections4.CollectionUtils;
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

				// 平台取消
		        boolean isCancel = sourceOrder.convertCancel();
		        dmpDataMap.put("isCancel", isCancel);
		        // 平台冻结
		        boolean isFrozen = sourceOrder.convertFrozen();
		        dmpDataMap.put("invalidStatus", isCancel && !isFrozen);
				
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
							dmpDataMap.put("totalDiscount", MathUtil.valueOf(amount).subtract(payAmount));
						}
					}

					//退款
					Object refundInfoObj = detailData.get("refund_info");
					if(refundInfoObj != null) {
						Map<String, Object> refundInfoMap = (Map) refundInfoObj;
						if (refundInfoMap.get("refund_cash_amt") != null) {
							Map<String, Object> refundCashAmtMap = (Map) refundInfoMap.get("refund_cash_amt");
							refundCashAmtMap.get("amount");
							refundCashAmtMap.get("currency_code");
						}
					}

					// 标签json
			        Map<String, Object> labelMap = new HashMap<>();
			        //订单明细
			        List<OrderItemDetail> orderItemDetailList = JSON.parseArray(JSON.toJSONString(detailData.get("child_order_list")), OrderItemDetail.class);
			        Boolean isAliexpressPlatformWarehouseOrder = Boolean.FALSE;
			        if (CollectionUtils.isNotEmpty(orderItemDetailList)) {
			            long count = orderItemDetailList.stream().
			                    filter(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType())).count();
			            isAliexpressPlatformWarehouseOrder = count > 0;
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
			        dmpDataMap.put("orderStatus", sourceOrder.convertBillStatus(isAliexpressPlatformWarehouseOrder));
			        dmpDataMap.put("payStatus", sourceOrder.convertPayStatus().equals(SoB2cPayStatusEnum.ENUM_PAID.getCode()));
			        // 审核状态状态
			        // （ApproveStatus字典类型）
			        dmpDataMap.put("approveStatus", sourceOrder.convertApproveStatus(isAliexpressPlatformWarehouseOrder));
				}
				
			}
		}
	}
	
}
