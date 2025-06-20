package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * Shopify退款单主信息
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyOrderFulfillmentsDmpHandler extends DmpInputDoNextDmpHandler{

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        long orderId = Long.parseLong(dmpInputMongoEntity.getOrDefault("orderId", "0").toString());
        if (orderId <= 0){
            return Collections.emptyList();
        }
        //订单配送信息
        List<ParamData> conditionDataList = new ArrayList<>();
        conditionDataList.add(new ParamData("order_id", "order_id", PannoEnum.EQ, orderId));
        List<Map<String, Object>> dmpInputFulfillmentMongoChildList = mongoService.findMongoData(conditionDataList, "shopify_fulfillments_data");
        if (CollectionUtils.isNotEmpty(dmpInputFulfillmentMongoChildList)){
            for (Map<String, Object> detail : dmpInputFulfillmentMongoChildList) {
                // 补充主表信息字段
                dmpInputMongoEntity.forEach((key, value) -> detail.putIfAbsent(key, value));
                resultMapList.add(detail);
            }
        }
        return resultMapList;
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
