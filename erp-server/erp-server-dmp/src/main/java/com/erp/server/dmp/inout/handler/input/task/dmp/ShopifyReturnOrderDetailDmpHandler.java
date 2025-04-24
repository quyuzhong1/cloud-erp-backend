package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.sdk.oms.shopify.api.rest.model.ShopifyLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * Shopify退货单明细
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyReturnOrderDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object refundsObj = dmpInputMongoEntity.get("refunds");
        if (null == refundsObj) {
            return Collections.emptyList();
        }
        // 退货/退款信息
        List<ShopifyRefund> shopifyRefunds = JSONUtil.toList(JSONUtil.toJsonStr(refundsObj), ShopifyRefund.class);
        if (CollectionUtils.isEmpty(shopifyRefunds)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> resultList = new LinkedList<>();


        for (ShopifyRefund shopifyRefund : shopifyRefunds) {
            List<ShopifyRefundLineItem> refundLineItems = shopifyRefund.getRefundLineItems();
            if (CollectionUtils.isEmpty(refundLineItems)) {
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

    @Override
    protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("ShopifyReturnOrderDetailDmpHandler afterConvertData：");
        String parentTableName = SqlHelper.table(DmpSoReturnInfoEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

        Map<String, String> dmpReturnIdMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(listMaps)) {
            for(Map<String, Object> listMap : listMaps) {
                dmpReturnIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String returnOrderId = detailMap.get("thirdOrderCode").toString();
                String dmpId = dmpReturnIdMap.get(returnOrderId);
                detailMap.put("mainId", dmpId);
            }
        }
        log.debug("ShopifyReturnOrderDetailDmpHandler afterConvertData：");
    }
}
