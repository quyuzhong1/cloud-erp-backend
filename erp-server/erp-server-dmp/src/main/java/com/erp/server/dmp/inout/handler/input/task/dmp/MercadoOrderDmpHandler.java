package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.oms.enums.MercadoOrderLogisticTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoOrderDmpHandler extends MercadoDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        DmpInputTaskEntity dmpInputTaskEntity = list.stream().filter(req -> "1816384798088779541".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "mercadolibre_shipment_data");
        //使用 DateTimeFormatter 解析字符串日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String parentTaskId = dmpInputTaskEntity.getParentTaskId();
        List<DmpInputTaskEntity> parentTaskEntityList = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, parentTaskId).list();
        if (CollectionUtil.isEmpty(parentTaskEntityList)) {
            return;
        }

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Map<String, Object> lableMap = new HashMap<>();

                dmpDataMap.put("nextLevelId", parentTaskEntityList.get(0).getNextLevelId());
                dmpDataMap.put("shopId", parentTaskEntityList.get(0).getNextLevelId());

                //创建时间
                Object createTimeObj = dmpDataMap.get("dateCreated");
                if (createTimeObj != null) {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(createTimeObj), formatter);
                    // 转换为 LocalDateTime
                    dmpDataMap.put("platformCreateTime", offsetDateTime.toLocalDateTime());
                }

                //修改时间
                Object updateTimeObj = dmpDataMap.get("lastUpdated");
                if (updateTimeObj != null) {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(updateTimeObj), formatter);
                    // 转换为 LocalDateTime
                    dmpDataMap.put("platformUpdateTime", offsetDateTime.toLocalDateTime());
                }

                Map<String, Object> shipmentIdMap = (Map<String, Object>) dmpDataMap.get("shipping");
                Object shipmentId = shipmentIdMap.get("fid");
                if (shipmentId != null) {
                    Map<String, Object> shipmentMap = dmpInputMongoChildList.stream().filter(req -> req.get("fid").equals(shipmentId)).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(shipmentMap)) {
                        continue;
                    }
                    dmpDataMap.put("logisticsCode", shipmentMap.get("trackingNumber"));
                    lableMap.put("shipmentId", shipmentId);

                    //物流状态
                    Map<String, Object> logisticMap = (Map<String, Object>) shipmentMap.get("logistic");
                    String logisticType = "";
                    String mode = String.valueOf(logisticMap.get("mode"));
                    String type = String.valueOf(logisticMap.get("type"));
                    lableMap.put("mode", mode);
                    if ("me2".equalsIgnoreCase(mode) && MercadoOrderLogisticTypeEnum.FULFILLMENT.getCode().equalsIgnoreCase(type)) {
                        //如果是平台仓，状态审核通过
                        logisticType = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
                        lableMap.put("isPlatformWarehouseOrder", Boolean.TRUE);
                    } else if ("me2".equalsIgnoreCase(mode)
                            && (MercadoOrderLogisticTypeEnum.DROP_OFF.getCode().equals(type) || MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode().equalsIgnoreCase(type))
                    ) {
                        logisticType = OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode();
                    } else if ("me1".equalsIgnoreCase(mode)) {
                        //自发货
                        logisticType = OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
                    }
                    lableMap.put("logisticType", type);
                    dmpDataMap.put("logisticType", logisticType);
                    //作废状态
                    Object statusObj = shipmentMap.get("status");
                    if (statusObj != null) {
                        String status = String.valueOf(statusObj);
                        if ("cancelled".equalsIgnoreCase(status)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                        } else if ("shipped".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("delivered".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("not_delivered".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                            dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                        } else if ("handling".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                                dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                            } else {
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                            }
                        } else if ("ready_to_ship".equalsIgnoreCase(status)) {
                            dmpDataMap.put("orderStatus", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                                dmpDataMap.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                            } else {
                                dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                            }
                        }
                    }

                    //订单状态
                    Object orderStatusObj = dmpDataMap.get("status");
                    if (orderStatusObj != null) {
                        String orderStatus = String.valueOf(statusObj);
                        dmpDataMap.put("platformOriginalStatus", orderStatus);
                        if ("invalid".equalsIgnoreCase(orderStatus)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                            dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        }
                    }

                    //买家备注
                    Object feedbackObj = dmpDataMap.get("feedback");
                    if (feedbackObj != null) {
                        Map<String, Object> feedbackMap = (Map<String, Object>) feedbackObj;
                        dmpDataMap.put("buyerRemark", feedbackMap.get("purchase"));

                    }
                    Object packId = dmpDataMap.get("platformCode");
                    if (packId == null) {
                        dmpDataMap.put("platformCode", dmpDataMap.get("thirdCode"));
                    }


                    //支付信息
                    Object paymentsObj = dmpDataMap.get("payments");
                    if (paymentsObj != null) {

                        List<Map<String, Object>> feedbackList = (List<Map<String, Object>>) paymentsObj;
                        if (CollectionUtil.isNotEmpty(feedbackList)) {
                            OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(feedbackList.get(0).get("dateCreated")), formatter);
                            // 转换为 LocalDateTime
                            dmpDataMap.put("payTime", offsetDateTime.toLocalDateTime());

                            dmpDataMap.put("currencyCode", feedbackList.get(0).get("currencyId"));
                            BigDecimal totalPaidAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("totalPaidAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("payAmount", totalPaidAmount);
                            BigDecimal transactionAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("transactionAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("allAmount", transactionAmount);
                            BigDecimal shippingAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("shippingAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("shippingCost", shippingAmount);

                            BigDecimal taxesAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("taxesAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);

                            lableMap.put("taxesAmount", taxesAmount);

                        }
                    }

                    //基本汇率
                    Object orderItemsObj = dmpDataMap.get("orderItems");
                    if (orderItemsObj != null) {
                        List<Map<String, Object>> orderItemsList = (List<Map<String, Object>>) orderItemsObj;
                        if (CollectionUtil.isNotEmpty(orderItemsList)) {
                            dmpDataMap.put("exchangeRate", orderItemsList.get(0).get("baseExchangeRate"));
                        }
                    }

                    //基本汇率
                    Object leadTimeObj = shipmentMap.get("leadTime");
                    if (leadTimeObj != null) {
                        Map<String, Object> leadTimeMap = (Map<String, Object>) leadTimeObj;
                        if (ObjectUtils.isNotEmpty(leadTimeMap)) {
                            Object shippingMethodObj = leadTimeMap.get("shippingMethod");
                            if (shippingMethodObj != null) {
                                Map<String, Object> shippingMethodMap = (Map<String, Object>) shippingMethodObj;
                                dmpDataMap.put("logisticsName", shippingMethodMap.get("name"));
                            }
                            Object costObj = leadTimeMap.get("cost");
                            lableMap.put("cost", costObj);
                        }


                    }

                    //扩展字段
                    dmpDataMap.put("extendData", JSONUtil.toJsonStr(lableMap));
                }
            }
        }
    }
}
