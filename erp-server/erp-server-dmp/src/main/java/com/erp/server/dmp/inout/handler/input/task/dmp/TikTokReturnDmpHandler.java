package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.service.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
public class TikTokReturnDmpHandler extends DmpInputDbConvertDmpHandler {
    @Resource
    private DmpSoReturnInfoService dmpSoReturnInfoService;
    @Resource
    private DmpSoReturnDetailService dmpSoReturnDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isNotEmpty(keySet)) {
            List<String> orderIdList = new ArrayList<>();
            for (List<Map<String, Object>> key : keySet) {
                orderIdList.addAll(key.stream().map(f -> f.get("returnId").toString()).collect(Collectors.toList()));
            }

            List<DmpSoReturnInfoEntity> list = dmpSoReturnInfoService.lambdaQuery()
                    .in(DmpSoReturnInfoEntity::getThirdCode, orderIdList)
                    .in(DmpSoReturnInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode(), DmpBasicSystemCodeEnum.MABANG.getCode()))
                    .select(DmpSoReturnInfoEntity::getId)
                    .list();
            if (CollUtil.isNotEmpty(list)) {
                List<String> ids = list.stream().map(DmpSoReturnInfoEntity::getId).collect(Collectors.toList());
                dmpSoReturnInfoService.removeByIds(ids);
                dmpSoReturnDetailService.lambdaUpdate()
                        .in(DmpSoReturnDetailEntity::getMainId, ids)
                        .remove();
            }
        }


        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", dmpDataMap.get("nextLevelId"));
                dmpDataMap.put("shopId", dmpDataMap.get("nextLevelId"));
                Object statusObj = dmpDataMap.get("platformStatus");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("RETURN_OR_REFUND_REQUEST_PENDING".equalsIgnoreCase(status)) {
                        //买方已发起退货或退款请求。该请求正在等待卖家或系统的审核。
                        dmpDataMap.put("status", "1");

                    } else if ("REFUND_OR_RETURN_REQUEST_REJECT".equalsIgnoreCase(status)) {
                        //退货或退款请求被拒绝
                        dmpDataMap.put("status", "5");

                    } else if ("AWAITING_BUYER_SHIP".equalsIgnoreCase(status)) {
                        //退货请求已获批准。卖方正在等待买方将批准的物品运送给卖方。如果买家没有在截止日期前将物品运送给卖家，平台将关闭请求。
                        dmpDataMap.put("status", "1");

                    } else if ("BUYER_SHIPPED_ITEM".equalsIgnoreCase(status)) {
                        //买方已将批准的物品运送给卖方
                        dmpDataMap.put("status", "1");

                    } else if ("REJECT_RECEIVE_PACKAGE".equalsIgnoreCase(status)) {
                        //卖方检查了退回的物品并拒绝了退货请求
                        dmpDataMap.put("status", "5");

                    } else if ("RETURN_OR_REFUND_REQUEST_SUCCESS".equalsIgnoreCase(status)) {
                        //退货/退款请求成功。买家将获得退款
                        dmpDataMap.put("status", "2");

                    } else if ("RETURN_OR_REFUND_REQUEST_CANCEL".equalsIgnoreCase(status)) {
                        //请求已被买方或系统取消
                        dmpDataMap.put("status", "5");

                    } else if ("RETURN_OR_REFUND_REQUEST_COMPLETE".equalsIgnoreCase(status)) {
                        //退货/退款已成功处理。买家已经退款。
                        dmpDataMap.put("status", "4");

                    } else if ("AWAITING_BUYER_RESPONSE".equalsIgnoreCase(status)) {
                        //卖家向买家提供另一种退货类型，并等待买家的回复。卖家建议的退货类型可以检查卖家_建议_退货类型。
                        dmpDataMap.put("status", "1");
                    }
                }

                //创建时间
                Object createTimeObj = dmpDataMap.get("createTime");
                if (createTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(createTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformCreateTime", createTime);
                    dmpDataMap.put("returnTime", createTime);
                }

                //修改时间
                Object updateTimeObj = dmpDataMap.get("updateTime");
                if (updateTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformUpdateTime", payTime);
                }

                //支付信息
                Object paymentObj = dmpDataMap.get("refundAmount");
                if (paymentObj != null) {
                    Map<String, Object> paymentMap = (Map<String, Object>) paymentObj;
                    dmpDataMap.put("allAmount", paymentMap.get("refundTotal"));
                    dmpDataMap.put("currencyCode", paymentMap.get("currency"));
                    dmpDataMap.put("refundTax", paymentMap.get("refundTax"));
                }

                //折扣信息
                Object discountAmountObj = dmpDataMap.get("discountAmount");
                if (discountAmountObj != null) {
                    List<Map<String, Object>> discountAmountList = (List<Map<String, Object>>) discountAmountObj;
                    dmpDataMap.put("shippingFeeSellerDiscount", discountAmountList.stream().map(req -> MathUtil.valueOf(req.get("shippingFeeSellerDiscount"))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                    dmpDataMap.put("shippingFeePlatformDiscount", discountAmountList.stream().map(req -> MathUtil.valueOf(req.get("shippingFeePlatformDiscount"))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                }
            }
        }
    }
}
