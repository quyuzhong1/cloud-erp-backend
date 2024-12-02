package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoReceiverService;
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
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
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
                Object shippingTypeObj = dmpDataMap.get("logisticType");
                if (shippingTypeObj != null) {
                    String shippingType = String.valueOf(shippingTypeObj);
                    Map<String, String> lableMap = new HashMap<>();
                    lableMap.put("tikTokStatus", statusObj.toString());
                    //自发货(shipping_type=SELLER)	中转仓（shipping_type=TIKTOK）
                    lableMap.put("shippingType", shippingType);
                    dmpDataMap.put("extendData", JSONUtil.toJsonStr(lableMap));

                    //渠道Id
                    Object shippingProviderIdObj = dmpDataMap.get("shippingProviderId");
                    if (shippingProviderIdObj != null) {
                        String shippingProviderId = String.valueOf(shippingProviderIdObj);
                        dmpDataMap.put("logisticsChannelId", "SELLER".equalsIgnoreCase(shippingType) ? "" : shippingProviderId);
                    }

                    //渠道名称
                    Object shippingProviderObj = dmpDataMap.get("shippingProvider");
                    if (shippingProviderObj != null) {
                        String shippingProvider = String.valueOf(shippingProviderObj);
                        dmpDataMap.put("logisticsChannelName", "SELLER".equalsIgnoreCase(shippingType) ? "" : shippingProvider);
                    }

                }

                //支付时间
                Object paidTimeObj = dmpDataMap.get("paidTime");
                if (paidTimeObj != null) {
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

                //发货时间
                Object deliveryTimeObj = dmpDataMap.get("deliveryTime");
                if (deliveryTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(deliveryTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("deliveryTime", payTime);
                }

                //支付信息
                Object paymentObj = dmpDataMap.get("payment");
                if (paymentObj != null) {
                    Map<String, Object> paymentMap = (Map<String, Object>) paymentObj;
                    dmpDataMap.put("payAmount", paymentMap.get("totalAmount"));
                    dmpDataMap.put("allAmount", paymentMap.get("subTotal"));
                    dmpDataMap.put("currencyCode", paymentMap.get("currency"));
                    dmpDataMap.put("shippingAmount", paymentMap.get("shippingFee"));
                    dmpDataMap.put("totalDiscount", MathUtil.valueOf(paymentMap.get("sellerDiscount")).add(MathUtil.valueOf(paymentMap.get("platformDiscount"))));
                }
            }
        }
    }
}
