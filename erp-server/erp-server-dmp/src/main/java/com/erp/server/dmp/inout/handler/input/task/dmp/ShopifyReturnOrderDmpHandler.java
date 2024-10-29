package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * Shopify退货单主信息
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyReturnOrderDmpHandler extends DmpInputDoNextDmpHandler{


    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        Object refundsObj = dmpInputMongoEntity.get("refunds");
        if (null == refundsObj){
            return Collections.emptyList();
        }
        // 退货/退款信息
        List<ShopifyRefund> shopifyRefunds = JSON.parseArray(JSON.toJSONString(refundsObj), ShopifyRefund.class);
        if (CollectionUtils.isEmpty(shopifyRefunds)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        // 公共参数
        Object shopId = dmpInputMongoEntity.getOrDefault("nextLevelId", "");
        // 订单币种
        String currency = "";
        Object currencyObj = dmpInputMongoEntity.get("currency");
        if (null != currencyObj) {
            currency = (String) currencyObj;
        }

        for (ShopifyRefund shopifyRefund : shopifyRefunds) {
            List<ShopifyRefundLineItem> refundLineItems = shopifyRefund.getRefundLineItems();
            if (CollectionUtils.isEmpty(refundLineItems)){
                // 当前无退货信息
                continue;
            }
            // 合并退货信息
            Map<String, Object> dmpMap = new HashMap<>();

            // 记录公共参数
            dmpMap.put("shopId", shopId);

            // 主单信息
            dmpMap.put("thirdCode", shopifyRefund.getId());
            dmpMap.put("platformCode", shopifyRefund.getOrderId());

            LocalDateTime createdAt = shopifyRefund.getCreatedAt();
            LocalDateTime createTime = LocalDateTime.parse(createdAt.toString());
            dmpMap.put("platformCreateTime", createTime);

            LocalDateTime processedAt = shopifyRefund.getProcessedAt();
            LocalDateTime parseReturnTime = LocalDateTime.parse(processedAt.toString());
            dmpMap.put("platformUpdateTime", parseReturnTime);

            dmpMap.put("returnTime", parseReturnTime);
            dmpMap.put("buyerUserId", shopifyRefund.getUserId());
            dmpMap.put("remark", shopifyRefund.getNote());
            dmpMap.put("currencyCode", currency);

            // 记录所有金额
            List<ShopifyTransaction> transactions = shopifyRefund.getTransactions();
            if (CollectionUtils.isNotEmpty(transactions)){
                BigDecimal allAmount = transactions.stream()
                        .map(ShopifyTransaction::getAmount)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                // 存在退款金额
                dmpMap.put("allAmount", allAmount);
            } else {
                dmpMap.put("allAmount", "0");
            }
            resultList.add(dmpMap);
        }
        return resultList;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String mainId = detailMap.get("mainId").toString();
                detailMap.put("sourceId", mainId);
            }
        }
    }

}
