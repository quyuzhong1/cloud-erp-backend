package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.MercadoOrderLogisticTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoReceiverService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoOrderDmpHandler extends MercadoDmpHandler {
    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private DmpSoReceiverService dmpSoReceiverService;
    @Resource
    private DmpSoDetailService dmpSoDetailService;

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

        //查询SLA信息
        DmpInputTaskEntity slaInputTaskEntity = list.stream().filter(req -> "1952980922018058718".equals(req.getCfgInputId())).findFirst().orElse(null);
        List<ParamData> paramSlaList = new ArrayList<>();
        paramSlaList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, slaInputTaskEntity.getId()));
        List<Map<String, Object>> dmpInputMongoChildSlaList = mongoService.findMongoData(paramSlaList, "mercadolibre_shipmentSla_data");

        //使用 DateTimeFormatter 解析字符串日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String parentTaskId = dmpInputTaskEntity.getParentTaskId();
        List<DmpInputTaskEntity> parentTaskEntityList = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, parentTaskId).list();
        if (CollectionUtil.isEmpty(parentTaskEntityList)) {
            return;
        }

        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isNotEmpty(keySet)) {
            List<String> orderIdList = new ArrayList<>();
            for (List<Map<String, Object>> key : keySet) {
                orderIdList.addAll(key.stream().map(f -> f.get("fid").toString()).collect(Collectors.toList()));
            }

            List<DmpSoInfoEntity> soInfoEntityList = dmpSoInfoService.lambdaQuery()
                    .in(DmpSoInfoEntity::getThirdCode, orderIdList)
                    .in(DmpSoInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode(), DmpBasicSystemCodeEnum.MABANG.getCode()))
                    .select(DmpSoInfoEntity::getId)
                    .list();
            if (CollUtil.isNotEmpty(soInfoEntityList)) {
                List<String> ids = soInfoEntityList.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
                dmpSoInfoService.removeByIds(ids);
                dmpSoReceiverService.lambdaUpdate()
                        .in(DmpSoReceiverEntity::getMainId, ids)
                        .remove();
                dmpSoDetailService.lambdaUpdate()
                        .in(DmpSoDetailEntity::getMainId, ids)
                        .remove();
            }
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
                    //买家自选物流 （跟踪方式）
                    dmpDataMap.put("buyerSelectedLogistics", shipmentMap.get("trackingMethod"));
                    lableMap.put("shipmentId", shipmentId);

                    Object dateCreated = shipmentMap.get("dateCreated");
                    if (ObjectUtil.isNotEmpty(dateCreated)) {
                        //发货时间
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(dateCreated), formatter);
                        dmpDataMap.put("deliveryTime", offsetDateTime.toLocalDateTime());
                    }

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
                    String status = String.valueOf(statusObj);

                    dmpDataMap.put("invalidStatus", this.convertCancel(status));
                    dmpDataMap.put("isCancel", this.convertCancel(status));
                    dmpDataMap.put("orderStatus", this.convertOrderStatus(status, logisticType));
                    dmpDataMap.put("deliveryStatus", this.convertBillStatus(status, logisticType));

                    //订单状态
                    String orderStatus = String.valueOf(statusObj);
                    dmpDataMap.put("platformOriginalStatus", orderStatus);
                    if ("invalid".equalsIgnoreCase(orderStatus)) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        dmpDataMap.put("isCancel", Boolean.TRUE);
                    } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        dmpDataMap.put("isCancel", Boolean.TRUE);
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
                            dmpDataMap.put("payStatus", Boolean.TRUE);

                            dmpDataMap.put("currencyCode", feedbackList.get(0).get("currencyId"));
                            BigDecimal totalPaidAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("totalPaidAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("payAmount", totalPaidAmount);
                            BigDecimal transactionAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("transactionAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("allAmount", transactionAmount);
                            BigDecimal shippingAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("shippingCost"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("shippingAmount", shippingAmount);
                            BigDecimal totalDiscount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("couponAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            dmpDataMap.put("totalDiscount", totalDiscount);

                            BigDecimal taxesAmount = feedbackList.stream().map(req -> MathUtil.valueOf(req.get("taxesAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
                            lableMap.put("taxesAmount", taxesAmount);

                            dmpDataMap.put("totalTaxFee", taxesAmount);
                        }
                    }

                    //基本汇率
                    Object orderItemsObj = dmpDataMap.get("orderItems");
                    if (orderItemsObj != null) {
                        List<Map<String, Object>> orderItemsList = (List<Map<String, Object>>) orderItemsObj;
                        if (CollectionUtil.isNotEmpty(orderItemsList)) {
                            dmpDataMap.put("exchangeRate", orderItemsList.get(0).get("baseExchangeRate"));
                            BigDecimal allAmount = orderItemsList.stream().map(req -> MathUtil.valueOf(req.get("fullUnitPrice")).multiply(MathUtil.valueOf(req.get("quantity")))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                            dmpDataMap.put("allAmount", allAmount);
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

                    //sla信息不为空
                    Map<String, Object> shipmentSlaMap = dmpInputMongoChildSlaList.stream().filter(req -> req.get("fid").equals(shipmentId)).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(shipmentSlaMap)) {
                        //sla 状态 。 表示货件的当前状态。其值可以是“on_time”、“delayed”、“early”
                        dmpDataMap.put("slaStatus", shipmentSlaMap.get("status"));
                        //sla 最晚发货时间 。 发货的截止日期和时间，需要在此时间之前发货
                        Object expectedDateObj =shipmentSlaMap.get("expectedDate") ;
                        if (null != expectedDateObj) {
                            lableMap.put("slaExpectedDate", expectedDateObj);
                        }
                    }

                    //扩展字段
                    dmpDataMap.put("extendData", JSONUtil.toJsonStr(lableMap));
                }
            }
        }
    }


    public String convertBillStatus(String status, String logisticType) {
        if ("cancelled".equalsIgnoreCase(status)) {
            return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
        } else if ("shipped".equalsIgnoreCase(status)) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        } else if ("delivered".equalsIgnoreCase(status)) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        } else if ("not_delivered".equalsIgnoreCase(status)) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        } else if ("handling".equalsIgnoreCase(status)) {
            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
            } else {
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }
        } else if ("ready_to_ship".equalsIgnoreCase(status)) {
            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
            } else {
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }
        } else {
            return SoB2cBillStatusEnum.ENUM_EXCEPTION.getCode();
        }

    }

    public boolean convertCancel(String status) {
        if ("cancelled".equalsIgnoreCase(status)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public String convertOrderStatus(String status, String logisticType) {
        if ("cancelled".equalsIgnoreCase(status)) {
            return ApproveStatusEnum.WAIT_SUBMIT.getCode();
        } else if ("shipped".equalsIgnoreCase(status)) {
            return ApproveStatusEnum.APPROVE.getCode();
        } else if ("delivered".equalsIgnoreCase(status)) {
            return ApproveStatusEnum.APPROVE.getCode();
        } else if ("not_delivered".equalsIgnoreCase(status)) {
            return ApproveStatusEnum.APPROVE.getCode();
        } else if ("handling".equalsIgnoreCase(status)) {
            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                return ApproveStatusEnum.APPROVE.getCode();
            } else {
                return ApproveStatusEnum.WAIT_SUBMIT.getCode();
            }
        } else if ("ready_to_ship".equalsIgnoreCase(status)) {
            if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                return ApproveStatusEnum.APPROVE.getCode();
            } else {
                return ApproveStatusEnum.WAIT_SUBMIT.getCode();
            }
        } else {
            return ApproveStatusEnum.WAIT_SUBMIT.getCode();
        }
    }
}
