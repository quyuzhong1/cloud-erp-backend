package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.CurrencyUtil;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private ThirdMappingService thirdMappingService;


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputLxOrderDmpHandler 处理完成: taskId={}", dmpInputTaskEntity.getId());
        // 领星平台来源
        List<DictBasicDTO.ViewDTO> dictbaseList = dictBasicService.getByKey("lingxingPlatformCode");
        // 店铺和映射
        List<ThirdShopEntity> shopList = thirdShopService.lambdaQuery()
                .eq(ThirdShopEntity::getSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
                .list();
        List<ThirdMappingEntity> mappingList = thirdMappingService.lambdaQuery()
                .eq(ThirdMappingEntity::getType, "shop")
                .eq(ThirdMappingEntity::getThirdSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
                .list();

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // 发货单类型 delivery_type
                //平台发货的订单，在ERP订单需要显示“平台仓”标识
                String deliveryTypeStr = dmpDataMap.getOrDefault("delivery_type", "").toString();
                Boolean isPlatformWarehouseOrder = convertIsPlatformWarehouseOrder(deliveryTypeStr);

                Map<String, Object> extendDataMap = new HashMap<>();
                extendDataMap.put("isPlatformWarehouseOrder", isPlatformWarehouseOrder);
                dmpDataMap.put("extendData", JSON.toJSONString(extendDataMap));


                // 物流类型
                String logisticType = "";
                if (null !=isPlatformWarehouseOrder) {
                    if (isPlatformWarehouseOrder) {
                        //平台仓
                        logisticType = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
//                    } else {
//                        //自发货
//                        logisticType = OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
                    }
                }
                dmpDataMap.put("logisticType", logisticType);

                // 原始销售平台状态
                String sourcePlatformOrderStatus = "";
                // 原始销售平台付款状态
                String sourcePlatformPayStatus = "";
                // 领星来源平台代号：LingxingPlatformCodeEnum
                String platformCodeStr = "";

                // 平台原始信息
                Object platformInfoListObj = dmpDataMap.get("platform_info");
                if (null != platformInfoListObj) {
                    JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(platformInfoListObj));
                    if (CollectionUtils.isNotEmpty(jsonArray)) {
                        Object platformInfoObjIndex1 = jsonArray.get(0);
                        Map<String, Object> platformInfoMap = (JSONObject) platformInfoObjIndex1;
                        String platformOriginalStatus = platformInfoMap.getOrDefault("status", "").toString();
                        dmpDataMap.put("platformOriginalStatus", platformOriginalStatus);

                        // 平台订单
                        String platformOrderNo = platformInfoMap.getOrDefault("platform_order_no", "").toString();
                        dmpDataMap.put("platformCode", platformOrderNo);

                        // 解析来源平台
                        platformCodeStr = platformInfoMap.getOrDefault("platform_code", "").toString();
                        if (StringUtils.isNotBlank(platformCodeStr)) {
                            String finalPlatformCodeStr = platformCodeStr;
                            DictBasicDTO.ViewDTO viewDTO = dictbaseList.stream().filter(e -> e.getValue().equalsIgnoreCase(finalPlatformCodeStr)).findFirst().orElse(null);
                            if (null == viewDTO) {
                                ServiceException.runError("dmp字典未找到领星平台：" + platformCodeStr);
                            }
                            String name = viewDTO.getName();
                            dmpDataMap.put("sourcePlatform", name);
                        }

                        // 平台仓发货时间
                        // "delivery_time": NumberInt("0"),
                        String deliveryTimeStr = platformInfoMap.getOrDefault("delivery_time", "0").toString();
                        long deliveryTimeLong = Long.parseLong(deliveryTimeStr);
                        if (0 < deliveryTimeLong && OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode().equals(logisticType)){
                            LocalDateTime deliveryTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(deliveryTimeLong), ZoneId.systemDefault());
                            dmpDataMap.put("deliveryTime", deliveryTime);
                        }

                        // 销售平台原始状态
                        sourcePlatformOrderStatus = platformInfoMap.getOrDefault("status", "").toString();
                        sourcePlatformPayStatus = platformInfoMap.getOrDefault("payment_status", "").toString();
                    }
                }

                // 订单状态
                String sourceOrderStatus = dmpDataMap.getOrDefault("status", "").toString();
                dmpDataMap.put("deliveryStatus", convertDmpOrderStatus(sourceOrderStatus, isPlatformWarehouseOrder, sourcePlatformOrderStatus, platformCodeStr));

                // 更新时间
                String updateTimeStr = dmpDataMap.getOrDefault("update_time", "").toString();
                LocalDateTime platformUpdateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(updateTimeStr)), ZoneId.systemDefault());
                dmpDataMap.put("platformUpdateTime", platformUpdateTime);

                // 创建时间
                String platformCreateTimeStr = dmpDataMap.getOrDefault("global_purchase_time", "").toString();
                LocalDateTime platformCreateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(platformCreateTimeStr)), ZoneId.systemDefault());
                dmpDataMap.put("platformCreateTime", platformCreateTime);

                // 发货时间
                // "global_delivery_time": NumberInt("0"),
//                String globalDeliveryTimeStr = dmpDataMap.getOrDefault("global_delivery_time", "0").toString();
//                long deliveryTimeLong = Long.parseLong(globalDeliveryTimeStr);
//                if (0 < deliveryTimeLong){
//                    LocalDateTime globalDeliveryTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(deliveryTimeLong), ZoneId.systemDefault());
//                    dmpDataMap.put("globalDeliveryTime", globalDeliveryTime);
//                }

                // 付款时间
                // global_payment_time
                String globalPaymentTimeStr = dmpDataMap.getOrDefault("global_payment_time", "").toString();
                LocalDateTime globalPaymentTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(globalPaymentTimeStr)), ZoneId.systemDefault());
                dmpDataMap.put("payTime", globalPaymentTime);

                // 付款状态
                Boolean payStatus = convertPayStatus(sourceOrderStatus,sourcePlatformOrderStatus, platformCodeStr, sourcePlatformPayStatus);
                dmpDataMap.put("payStatus", payStatus);

                // 取消状态
                Boolean isCancel = convertIsCancel(sourceOrderStatus,sourcePlatformOrderStatus, platformCodeStr);
                dmpDataMap.put("isCancel", isCancel);
                dmpDataMap.put("invalidStatus", isCancel);

                // 审核状态
                String approveStatus = convertApproveStatus(sourceOrderStatus, isPlatformWarehouseOrder, sourcePlatformOrderStatus, platformCodeStr);
                dmpDataMap.put("orderStatus", approveStatus);

                // 店铺ID
                String storeId = dmpDataMap.getOrDefault("store_id", "").toString();
                // 校验和获取ERP店铺
                ThirdMappingEntity mappingEntity = checkAndGetErpShopId(shopList, mappingList, storeId);
                dmpDataMap.put("shopId", mappingEntity.getSysId());
                dmpDataMap.put("shopName", mappingEntity.getSysName());

                // 金额相关信息
                Object transactionInfoObj = dmpDataMap.get("transaction_info");
                if (null != transactionInfoObj) {
                    JSONArray transactionInfoJsonArray = JSON.parseArray(JSON.toJSONString(transactionInfoObj));
                    if (CollectionUtils.isNotEmpty(transactionInfoJsonArray)) {
                        Object transactionInfoObjIndex1 = transactionInfoJsonArray.get(0);
                        Map<String, Object> transactionInfoMap = (JSONObject) transactionInfoObjIndex1;
                        for (Map.Entry<String, Object> entry : transactionInfoMap.entrySet()) {
                            BigDecimal amount = CurrencyUtil.parseAmount(entry.getValue().toString());
                            dmpDataMap.put(entry.getKey(), amount);
                            if ("order_total_amount".equalsIgnoreCase(entry.getKey())){
                                dmpDataMap.put("payAmount", amount);
                            }
                            if ("order_item_amount".equalsIgnoreCase(entry.getKey())){
                                dmpDataMap.put("allAmount", amount);
                            }
                            if ("customer_shipping_amount".equalsIgnoreCase(entry.getKey())){
                                dmpDataMap.put("shippingAmount", amount);
                            }
                            if ("transaction_fee_amount".equalsIgnoreCase(entry.getKey())){
                                dmpDataMap.put("platformCost", amount);
                            }
                            if ("platform_tax_amount".equalsIgnoreCase(entry.getKey())){
                                dmpDataMap.put("vatCost", amount);
                            }
                        }
                    }
                }



                // 买家信息
                Object buyersInfoObj = dmpDataMap.get("buyers_info");
                if (null != buyersInfoObj) {
                    Map<String, Object> buyersInfoMap = JSON.parseObject(JSON.toJSONString(buyersInfoObj));
                    String buyerNote = buyersInfoMap.getOrDefault("buyer_note", "").toString();
                    dmpDataMap.put("buyerRemark", buyerNote);
                }
            }
        }
    }

    /**
     * 转换审核状态
     */
    private String convertApproveStatus(String sourceOrderStatus, Boolean isPlatformWarehouseOrder, String sourcePlatformOrderStatus, String platformCodeStr) {
        if (null == isPlatformWarehouseOrder){
            return "";
        }
        // 领星系统订单状态：1 同步中2 已同步3 未付款4 待审核5 待发货6 已发货7 已取消/不发货8 不显示9 平台发货
        // 平台仓
        if (isPlatformWarehouseOrder){
            if ("4".equalsIgnoreCase(sourceOrderStatus)
                    || "5".equalsIgnoreCase(sourceOrderStatus)
                    || "6".equalsIgnoreCase(sourceOrderStatus)
                    || "9".equalsIgnoreCase(sourceOrderStatus)
            ) {
                return ApproveStatusEnum.APPROVE.getCode();
            } else {
                return ApproveStatusEnum.WAIT_SUBMIT.getCode();
            }
        }
        if ("6".equalsIgnoreCase(sourceOrderStatus)
                || "9".equalsIgnoreCase(sourceOrderStatus)
        ) {
            return ApproveStatusEnum.APPROVE.getCode();
        }
        if ("7".equalsIgnoreCase(sourceOrderStatus)){
            Boolean isCancel = convertIsCancel(sourceOrderStatus, sourcePlatformOrderStatus, platformCodeStr);
            if (!isCancel){
                return ApproveStatusEnum.APPROVE.getCode();
            }
        }
        return ApproveStatusEnum.WAIT_SUBMIT.getCode();
    }

    /**
     * 转换取消状态
     *
     * @param sourceOrderStatus         原始订单状态
     * @param sourcePlatformOrderStatus
     * @param platformCodeStr
     * @return true=取消
     */
    private Boolean convertIsCancel(String sourceOrderStatus, String sourcePlatformOrderStatus, String platformCodeStr) {
        // 7 已取消/不发货(优化确定平台取消的场景)
        if ("7".equalsIgnoreCase(sourceOrderStatus)) {
            // TEMU
            // PENDING
            // UN_SHIPPING
            // PARTIALLY_RECEIVED
            // CANCELED
            // RECEIVED
            // PARTIALLY_SHIPPED
            // SHIPPED

            // ebay
            // Cancelled
            // CancelPending
            // Completed
            // Inactive
            // Active

            // 100：未付款
            //200：等待乐天处理
            //600：付款处理中
            //700：完成支付
            //800：等待取消确认
            //300：待发货
            //400：等待修改确认
            //500：已发货
            //900：已付款
            return "CANCELED".equalsIgnoreCase(sourcePlatformOrderStatus)
                    || "Cancelled".equalsIgnoreCase(sourcePlatformOrderStatus);
        }
        return false;

    }

    /**
     * 转换付款状态
     */
    private Boolean convertPayStatus(String sourceOrderStatus, String sourcePlatformOrderStatus, String platformCodeStr, String sourcePlatformPayStatus) {
        // 领星系统订单状态：1 同步中2 已同步3 未付款4 待审核5 待发货6 已发货7 已取消/不发货8 不显示9 平台发货
        if ("3".equalsIgnoreCase(sourceOrderStatus)) {
            return false;
        }
        if ("4".equalsIgnoreCase(sourceOrderStatus)
                || "5".equalsIgnoreCase(sourceOrderStatus)
                || "6".equalsIgnoreCase(sourceOrderStatus)
                || "9".equalsIgnoreCase(sourceOrderStatus)
        ) {
            return true;
        }
        if ("7".equalsIgnoreCase(sourceOrderStatus)){
            Boolean isCancel = convertIsCancel(sourceOrderStatus, sourcePlatformOrderStatus, platformCodeStr);
            if (!isCancel){
                return "paid".equalsIgnoreCase(sourcePlatformPayStatus);
            }
            return false;
        }
        return null;
    }

    /**
     * 校验和获取ERP店铺
     */
    private ThirdMappingEntity checkAndGetErpShopId(List<ThirdShopEntity> shopList, List<ThirdMappingEntity> mappingList, String storeId) {
        ThirdShopEntity thirdShopEntity = shopList.stream().filter(e -> e.getCode().equalsIgnoreCase(storeId)).findFirst().orElse(null);
        if (null == thirdShopEntity) {
            ServiceException.runError("未找到领星店铺对应映射记录:领星店铺ID=" + storeId);
        }
        ThirdMappingEntity mappingEntity = mappingList.stream().filter(e -> e.getThirdInfoId().equalsIgnoreCase(thirdShopEntity.getId())).findFirst().orElse(null);
        if (null == mappingEntity) {
            ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
        }
        if (StringUtils.isBlank(mappingEntity.getSysId())){
            ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
        }
        return mappingEntity;
    }

    /**
     * 转换是否是平台仓
     */
    private Boolean convertIsPlatformWarehouseOrder(String deliveryTypeStr) {
        // 对应ERP的订单发货类型：
        // 1 混合方式【中转值，最终会转为2或3】
        // 2 自发货：对应ERP自发货订单
        // 3 平台发货【指由平台仓库自动完成履约的订单，如Walmart的WFS订单】：对应ERP平台仓发货订单
        if ("2".equalsIgnoreCase(deliveryTypeStr)) {
            return false;
        }
        if ("3".equalsIgnoreCase(deliveryTypeStr)) {
            return true;
        }
        // 无法判断为null, 黑名单拦截
        return null;
    }

    /**
     * 领星订单状态转换订单状态
     */
    public String convertDmpOrderStatus(String sourceOrderStatus, Boolean isPlatformWarehouseOrder, String sourcePlatformOrderStatus, String platformCodeStr) {
        // 1 同步中2 已同步 8 不显示
        if (null == isPlatformWarehouseOrder
                || "1".equalsIgnoreCase(sourceOrderStatus)
                || "2".equalsIgnoreCase(sourceOrderStatus)
                || "8".equalsIgnoreCase(sourceOrderStatus)
        ) {
            // 无法处理单据=设置空
            return "";
        }

        // 统一取消判断
        if ("7".equalsIgnoreCase(sourceOrderStatus)){
            if (convertIsCancel(sourceOrderStatus, sourcePlatformOrderStatus, platformCodeStr)){
                // 已取消
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            } else {
                // 非已取消=在其他地方已做了发货
                return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
            }
        }

        // 平台仓订单
        if (isPlatformWarehouseOrder) {
            // 领星系统订单状态：1 同步中2 已同步3 未付款4 待审核5 待发货6 已发货7 已取消/不发货8 不显示9 平台发货
            if ("3".equalsIgnoreCase(sourceOrderStatus)) {
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }
            if ("4".equalsIgnoreCase(sourceOrderStatus)
                    || "5".equalsIgnoreCase(sourceOrderStatus)) {
                return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
            }
            if ("6".equalsIgnoreCase(sourceOrderStatus)
                    || "9".equalsIgnoreCase(sourceOrderStatus)) {
                return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
            }
            return "";
        }

        // 自发货订单
        // 领星系统订单状态：1 同步中2 已同步3 未付款4 待审核5 待发货6 已发货7 已取消/不发货8 不显示9 平台发货
        if ("3".equalsIgnoreCase(sourceOrderStatus)
                || "4".equalsIgnoreCase(sourceOrderStatus)
                || "5".equalsIgnoreCase(sourceOrderStatus)
        ) {
            return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
        }
        if ("6".equalsIgnoreCase(sourceOrderStatus) || "9".equalsIgnoreCase(sourceOrderStatus)) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        }
        return "";
    }


}
