package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.InventoryDetails;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.ResearchingQuantity;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.ReservedQuantity;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.UnfulfillableQuantity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzFbaInventoryDmpHandler extends DmpInputDbConvertDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            Object detailsObj = mongoData.get("inventoryDetails");
            if (null == detailsObj) {
                continue;
            }
            if (!(detailsObj instanceof JSONObject)) {
                continue;
            }
            JSONObject detailsJsonObj = (JSONObject) detailsObj;
            InventoryDetails inventoryDetails = JSON.parseObject(detailsJsonObj.toJSONString(), InventoryDetails.class);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Integer fulFillableQuantity = inventoryDetails.getFulfillableQuantity();
                dmpDataMap.put("fulfillable_qty", fulFillableQuantity);


                Integer inboundWorkingQuantity = inventoryDetails.getInboundWorkingQuantity();
                dmpDataMap.put("inbound_working_qty", inboundWorkingQuantity);

                Integer inboundShippedQuantity = inventoryDetails.getInboundShippedQuantity();
                dmpDataMap.put("inbound_shipped_qty", inboundShippedQuantity);

                Integer inboundReceivingQuantity = inventoryDetails.getInboundReceivingQuantity();
                dmpDataMap.put("inbound_receiving_qty", inboundReceivingQuantity);

                ReservedQuantity reservedQuantity = inventoryDetails.getReservedQuantity();
                if (null != reservedQuantity) {
                    Integer totalReservedQuantity = reservedQuantity.getTotalReservedQuantity();
                    dmpDataMap.put("reserved_qty", totalReservedQuantity);
                }

                ResearchingQuantity researchingQuantity = inventoryDetails.getResearchingQuantity();
                if (null != researchingQuantity) {
                    Integer totalResearchingQuantity = researchingQuantity.getTotalResearchingQuantity();
                    dmpDataMap.put("researching_qty", totalResearchingQuantity);
                }

                UnfulfillableQuantity unfulfillableQuantity = inventoryDetails.getUnfulfillableQuantity();
                if (null != unfulfillableQuantity) {
                    Integer totalUnfulfillableQuantity = unfulfillableQuantity.getTotalUnfulfillableQuantity();
                    dmpDataMap.put("unsellable_qty", totalUnfulfillableQuantity);
                }
            }
        }

    }

}
