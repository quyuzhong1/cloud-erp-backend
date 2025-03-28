package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.utils.MathUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokFullyOrderDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        return (List<Map<String, Object>>) dmpInputMongoEntity.get("skus");
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("third_detail_id", dmpDataMap.get("externalSkuCode"));
                dmpDataMap.put("platform_detail_id", dmpDataMap.get("externalSkuCode"));
                dmpDataMap.put("platform_sku", dmpDataMap.get("externalSkuCode"));
                dmpDataMap.put("platform_spu_no", dmpDataMap.get("platformSkuCode"));
                buildExtendData(dmpDataMap);
            }
        }
    }

    private void buildExtendData(TreeMap<String, Object> dmpDataMap) {
        Map<String, Object> data = new HashMap<>();
        data.put("deliveryQty", dmpDataMap.get("deliveredQuantity"));
        data.put("receiveQty", dmpDataMap.get("receivedQuantity"));
        data.put("instockQty", dmpDataMap.get("inboundQuantity"));
        data.put("returnQty", dmpDataMap.get("returnedQuantity"));
        dmpDataMap.put("extendData", JSON.toJSONString(data));
    }
}
