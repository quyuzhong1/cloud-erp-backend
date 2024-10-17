package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.shopify.api.rest.model.ShopifyLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * Shopify退款单明细信息
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyRefundOrderDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
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

        for (ShopifyRefund shopifyRefund : shopifyRefunds) {
            List<ShopifyRefundLineItem> refundLineItems = shopifyRefund.getRefundLineItems();
            if (CollectionUtils.isEmpty(refundLineItems)){
                // 当前无退货信息
                continue;
            }
            for (ShopifyRefundLineItem refundLineItem : refundLineItems) {
                ShopifyLineItem lineItem = refundLineItem.getLineItem();
                JSONObject jsonObject = (JSONObject) JSON.toJSON(lineItem);
                jsonObject.put("thirdOrderCode", shopifyRefund.getId());
                jsonObject.put("platformOrderCode", shopifyRefund.getId());
                jsonObject.put("soEntryId", shopifyRefund.getOrderId());
                jsonObject.put("platformCode", shopifyRefund.getOrderId());
                jsonObject.put("platformCreateTime", shopifyRefund.getCreatedAt());
                jsonObject.put("platformUpdateTime", shopifyRefund.getProcessedAt());
                jsonObject.put("qty", refundLineItem.getQuantity());

                jsonObject.put("buyerUserId", shopifyRefund.getUserId());
                jsonObject.put("reason", shopifyRefund.getNote());

                resultList.add(jsonObject);
            }
        }
        return resultList;
    }
}
