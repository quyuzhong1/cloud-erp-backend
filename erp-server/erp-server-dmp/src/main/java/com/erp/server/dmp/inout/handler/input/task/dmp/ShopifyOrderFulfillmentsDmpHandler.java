package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if (CollectionUtils.isEmpty(detailList)){
            return detailList;
        }
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        List<Long> orderIds = detailList.stream().map(e -> Long.parseLong(e.getOrDefault("orderId", "0").toString())).distinct().collect(Collectors.toList());
        //订单配送信息
        List<ParamData> conditionDataList = new ArrayList<>();
        conditionDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIds));
        List<Map<String, Object>> dmpInputFulfillmentMongoChildList = mongoService.findMongoData(conditionDataList, "shopify_fulfillments_data");
        if (CollectionUtils.isNotEmpty(dmpInputFulfillmentMongoChildList)){
            Map<String, Map<String, Object>> fulfillmentsMap = dmpInputFulfillmentMongoChildList.stream().collect(Collectors.toMap(e -> e.getOrDefault("order_id", "").toString(), e -> e));
            for (Map<String, Object> detail : detailList) {
                Map<String, Object> fulfillmentItemMap = fulfillmentsMap.get(detail.get("orderId").toString());
                if (fulfillmentItemMap == null) {
                    continue;
                }
                fulfillmentItemMap.remove("_id");
                fulfillmentItemMap.remove("convertId");
                fulfillmentItemMap.remove("inputTaskId");
                fulfillmentItemMap.remove("mongoCreateTime");
                fulfillmentItemMap.remove("mongoUpdateTime");
                fulfillmentItemMap.remove("uniqueEncrypt");
                fulfillmentItemMap.remove("fileId");
                fulfillmentItemMap.remove("dataEncrypt");
                detail.putAll(fulfillmentItemMap);
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
