package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundRoot;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
//        String id = dmpInputTaskEntity.getId();
//        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
//        if (CollectionUtil.isEmpty(list)) {
//            return Collections.emptyList();
//        }
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
        Object shopId = dmpInputMongoEntity.getOrDefault("shopId", "");
        Object shopName = dmpInputMongoEntity.getOrDefault("shopName", "");

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
            dmpMap.put("shop_name", shopName);

            // 主单信息
            dmpMap.put("third_code", shopifyRefund.getId());
            dmpMap.put("platform_code", shopifyRefund.getOrderId());
            dmpMap.put("platform_create_time", shopifyRefund.getCreatedAt());
            dmpMap.put("platform_update_time", shopifyRefund.getProcessedAt());

            dmpMap.put("return_time", shopifyRefund.getProcessedAt());
            dmpMap.put("buyer_user_id", shopifyRefund.getUserId());
            dmpMap.put("remark", shopifyRefund.getNote());

            // 记录所有金额
            List<ShopifyTransaction> transactions = shopifyRefund.getTransactions();
            if (CollectionUtils.isNotEmpty(transactions)){
                BigDecimal allAmount = transactions.stream()
                        .map(ShopifyTransaction::getAmount)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                Currency currency = transactions.get(0).getCurrency();
                // 存在退款金额
                dmpMap.put("currency_code", currency.getCurrencyCode());
                dmpMap.put("all_amount", allAmount);
            } else {
                dmpMap.put("currency_code", "");
                dmpMap.put("all_amount", "0");
            }
            resultList.add(dmpMap);
        }
        return resultList;
    }
}
