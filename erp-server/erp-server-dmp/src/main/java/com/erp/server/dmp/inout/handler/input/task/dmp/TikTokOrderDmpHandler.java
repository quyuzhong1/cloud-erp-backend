package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangOriginalOrderStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoReceiverService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokOrderDmpHandler extends DmpInputDbConvertDmpHandler {
    private static final String TIKTOK_SHIPPING_TYPE_SELLER = "SELLER";
    private static final String TIKTOK_FULFILLMENT_BY_TIKTOK = "FULFILLMENT_BY_TIKTOK";
    private static final String TIKTOK_FULFILLMENT_BY_SELLER = "FULFILLMENT_BY_SELLER";

    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private DmpSoReceiverService dmpSoReceiverService;
    @Resource
    private DmpSoDetailService dmpSoDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isNotEmpty(keySet)) {
            List<String> orderIdList = new ArrayList<>();
            for (List<Map<String, Object>> key : keySet) {
                orderIdList.addAll(key.stream().map(f -> f.get("fid").toString()).collect(Collectors.toList()));
            }

            List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
                    .in(DmpSoInfoEntity::getThirdCode, orderIdList)
                    .in(DmpSoInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode(), DmpBasicSystemCodeEnum.MABANG.getCode()))
                    .select(DmpSoInfoEntity::getId)
                    .list();
            if (CollUtil.isNotEmpty(list)) {
                List<String> ids = list.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
                dmpSoInfoService.removeByIds(ids);
                dmpSoReceiverService.lambdaUpdate()
                        .in(DmpSoReceiverEntity::getMainId, ids)
                        .remove();
                dmpSoDetailService.lambdaUpdate()
                        .in(DmpSoDetailEntity::getMainId, ids)
                        .remove();
            }
        }

        String parentTaskId = dmpInputTaskEntity.getParentTaskId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, parentTaskId).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", list.get(0).getNextLevelId());
                dmpDataMap.put("shopId", list.get(0).getNextLevelId());
                //销售平台
                Object statusObj = dmpDataMap.get("platformOriginalStatus");
                String status = statusObj == null ? "" : String.valueOf(statusObj);
                if (statusObj != null) {
                    if ("ON_HOLD".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
                        dmpDataMap.put("remark", "ON_HOLD");
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("AWAITING_SHIPMENT".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("AWAITING_COLLECTION".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("PARTIALLY_SHIPPING".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("IN_TRANSIT".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("DELIVERED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("COMPLETED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.FALSE);
                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        dmpDataMap.put("isCancel", Boolean.TRUE);
                    }
                }
                Map<String, Object> lableMap = new HashMap<>();
                if (statusObj != null) {
                    lableMap.put("tikTokStatus", statusObj.toString());
                }

                String fulfillmentType = getStringValue(dmpDataMap, "fulfillmentType", "fulfillment_type");
                String rawLogisticType = getStringValue(dmpDataMap, "logisticType");
                String shippingType = getStringValue(dmpDataMap, "shippingType", "shipping_type", "logisticType");
                boolean isPlatformWarehouseOrder = isTikTokPlatformWarehouseOrder(fulfillmentType);
                boolean hasDeliveryType = StringUtils.isNotBlank(fulfillmentType) || StringUtils.isNotBlank(shippingType);
                String normalizedLogisticType = normalizeTikTokLogisticType(fulfillmentType, shippingType, rawLogisticType);
                if (StringUtils.isNotBlank(fulfillmentType)) {
                    lableMap.put("fulfillmentType", fulfillmentType);
                }
                if (StringUtils.isNotBlank(shippingType)) {
                    lableMap.put("shippingType", shippingType);
                }
                lableMap.put("isPlatformWarehouseOrder", isPlatformWarehouseOrder);
                dmpDataMap.put("logisticType", normalizedLogisticType);
                normalizeTikTokPlatformWarehouseStatus(dmpDataMap, status, isPlatformWarehouseOrder);

                //渠道Id
                Object shippingProviderIdObj = dmpDataMap.get("shippingProviderId");
                if (shippingProviderIdObj != null && hasDeliveryType) {
                    String shippingProviderId = String.valueOf(shippingProviderIdObj);
                    dmpDataMap.put("logisticsChannelId", isSelfDeliveryOrder(fulfillmentType, shippingType) ? "" : shippingProviderId);
                }
                //渠道名称
                Object shippingProviderObj = dmpDataMap.get("shippingProvider");
                if (shippingProviderObj != null && hasDeliveryType) {
                    String shippingProvider = String.valueOf(shippingProviderObj);
                    dmpDataMap.put("logisticsChannelName", isSelfDeliveryOrder(fulfillmentType, shippingType) ? "" : shippingProvider);
                }
                //包裹号
                Object packages = dmpDataMap.get("packages");
                if (packages != null){
                    JSONArray jsonArray = JSONUtil.parseArray(packages);
                    if (CollUtil.isNotEmpty(jsonArray)){
                        Object object = jsonArray.get(0);
                        JSONObject jsonObject = JSONUtil.parseObj(object);
                        dmpDataMap.put("planPackageNo", Objects.nonNull(jsonObject) ? jsonObject.getStr("fid") : "");
                    }
                }
                //物流商id
                dmpDataMap.put("planSupplierId", Objects.nonNull(shippingProviderObj) ? String.valueOf(shippingProviderObj) : "");

                Object orderTypeObj = dmpDataMap.get("orderType");
                if (orderTypeObj != null) {
                    String orderType = String.valueOf(orderTypeObj);
                    if("BACK_ORDER".equals(orderType)){
                        lableMap.put("orderType", "preOrder");
                    }
                }
                // 是否明细退款
                boolean hasRefundLineItems = false;
                Object lineItemsObj = dmpDataMap.get("lineItems");
                if (null != lineItemsObj){
                    // 存在退款的明细ID
                    Set<String> refundedLineItemIds = new HashSet<>();
                    JSONArray jsonArray = null;
                    if (lineItemsObj instanceof List){
                        List lineItemsList = (List) lineItemsObj;
                        jsonArray = JSONUtil.parseArray(lineItemsList);
                    } else if (lineItemsObj instanceof JSONArray){
                        jsonArray = (JSONArray) lineItemsObj;
                    } else {
                        jsonArray = JSONUtil.parseArray(lineItemsObj.toString());
                    }
                    for (Object itemObj : jsonArray) {
                        JSONObject itemJsonObj = null;
                        if (itemObj instanceof JSONObject){
                            itemJsonObj = JSONUtil.parseObj(itemObj);
                        } else if (JSONUtil.isTypeJSON(itemJsonObj.toString())){
                            itemJsonObj = JSONUtil.parseObj(itemObj.toString());
                        }
                        String cancelUser = itemJsonObj.getStr("cancelUser");
                        if (StringUtils.isNotBlank(cancelUser)) {
                            String sourceFundedLineItemId = itemJsonObj.getStr("fid");
                            refundedLineItemIds.add(sourceFundedLineItemId);
                            hasRefundLineItems = true;
                        }
                    }
                    if (CollectionUtils.isNotEmpty(refundedLineItemIds)) {
                        lableMap.put("refundedLineItemIds", refundedLineItemIds);
                    }
                }
                // 明细存在退货 取消状态
                if (hasRefundLineItems) {
                    dmpDataMap.put("isCancel", Boolean.TRUE);
                }

                dmpDataMap.put("extendData", JSONUtil.toJsonStr(lableMap));

                //支付时间
                Object paidTimeObj = dmpDataMap.get("paidTime");
                if (paidTimeObj != null && Long.valueOf(paidTimeObj + "") > 0) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(paidTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("payTime", payTime);
                } else {
                    dmpDataMap.put("payTime", null);
                }

                //创建时间
                Object createTimeObj = dmpDataMap.get("createTime");
                if (createTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(createTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformCreateTime", payTime);
                }

                //修改时间
                Object updateTimeObj = dmpDataMap.get("updateTime");
                if (updateTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformUpdateTime", payTime);
                }

                // 发货时间:
                // TikTok平台仓/FBT订单优先取 rts_time，保持后续销售出库单出库日期与平台履约时间口径一致。
                Long deliveryEpochSeconds = isPlatformWarehouseOrder
                        ? firstPositiveLongValue(dmpDataMap, "rtsTime", "rts_time", "deliveryTime", "delivery_time")
                        : firstPositiveLongValue(dmpDataMap, "deliveryTime", "delivery_time", "rtsTime", "rts_time");
                if (deliveryEpochSeconds != null) {
                    LocalDateTime deliveryTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(deliveryEpochSeconds),
                            ZoneId.systemDefault());
                    dmpDataMap.put("deliveryTime", deliveryTime);
                } else {
                    dmpDataMap.put("deliveryTime", null);
                }

                //支付信息
                Object paymentObj = dmpDataMap.get("payment");
                if (paymentObj != null) {
                    Map<String, Object> paymentMap = (Map<String, Object>) paymentObj;
                    dmpDataMap.put("payStatus", Boolean.TRUE);
                    dmpDataMap.put("totalTaxFee", paymentMap.get("tax"));
                    dmpDataMap.put("payAmount", paymentMap.get("totalAmount"));
                    dmpDataMap.put("allAmount", paymentMap.get("originalTotalProductPrice"));
                    dmpDataMap.put("currencyCode", paymentMap.get("currency"));
                    dmpDataMap.put("shippingAmount", paymentMap.get("shippingFee"));
                    dmpDataMap.put("totalDiscount", MathUtil.valueOf(paymentMap.get("sellerDiscount")).add(MathUtil.valueOf(paymentMap.get("platformDiscount"))));
                }
            }
        }
    }

    private String getStringValue(Map<String, Object> dataMap, String... keys) {
        if (dataMap == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            Object value = dataMap.get(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private Long firstPositiveLongValue(Map<String, Object> dataMap, String... keys) {
        if (dataMap == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = dataMap.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            try {
                long parsed = Long.parseLong(text);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    private boolean isTikTokPlatformWarehouseOrder(String fulfillmentType) {
        return TIKTOK_FULFILLMENT_BY_TIKTOK.equalsIgnoreCase(fulfillmentType);
    }

    private boolean isSelfDeliveryOrder(String fulfillmentType, String shippingType) {
        return TIKTOK_FULFILLMENT_BY_SELLER.equalsIgnoreCase(fulfillmentType)
                || TIKTOK_SHIPPING_TYPE_SELLER.equalsIgnoreCase(shippingType);
    }

    private String normalizeTikTokLogisticType(String fulfillmentType, String shippingType, String rawLogisticType) {
        if (isTikTokPlatformWarehouseOrder(fulfillmentType)) {
            return OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
        }
        if (isSelfDeliveryOrder(fulfillmentType, shippingType)) {
            return OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
        }
        if (OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode().equalsIgnoreCase(rawLogisticType)
                || OrderLogisticTypeEnum.SELF_SHIPMENT.getCode().equalsIgnoreCase(rawLogisticType)
                || OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode().equalsIgnoreCase(rawLogisticType)) {
            return rawLogisticType;
        }
        return "";
    }

    private void normalizeTikTokPlatformWarehouseStatus(TreeMap<String, Object> dmpDataMap, String status, boolean isPlatformWarehouseOrder) {
        if (!isPlatformWarehouseOrder || StringUtils.isBlank(status)) {
            return;
        }
        if ("ON_HOLD".equalsIgnoreCase(status)
                || "AWAITING_SHIPMENT".equalsIgnoreCase(status)
                || "AWAITING_COLLECTION".equalsIgnoreCase(status)
                || "PARTIALLY_SHIPPING".equalsIgnoreCase(status)) {
            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
            dmpDataMap.put("invalidStatus", Boolean.FALSE);
            return;
        }
        if ("IN_TRANSIT".equalsIgnoreCase(status)
                || "DELIVERED".equalsIgnoreCase(status)
                || "COMPLETED".equalsIgnoreCase(status)) {
            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            dmpDataMap.put("invalidStatus", Boolean.FALSE);
        }
    }
}
