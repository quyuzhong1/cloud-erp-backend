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
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) dmpInputMongoEntity.get("skus");
        detailList.forEach(d -> {
            d.put("platformSpuCode", dmpInputMongoEntity.get("platformSpuCode"));
        });
        return detailList;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("thirdDetailId", dmpDataMap.get("platformSkuCode"));
                dmpDataMap.put("platformDetailId", dmpDataMap.get("platformSkuCode"));
                dmpDataMap.put("platformSku", dmpDataMap.get("externalSkuCode"));
                dmpDataMap.put("platformSpuNo", dmpDataMap.get("platformSpuCode"));
                dmpDataMap.put("qty",dmpDataMap.get("stockupQuantity"));
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
