package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.alibaba.fastjson.JSON;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * WEGO 入库 DMP 转换扩展 handler，与 {@code JiFengInBoundDmpHandler} 等价。
 * <p>
 * 将 mongo 缓存中的入库明细（key={@code skuList}）拍平到 {@link com.erp.model.dmp.entity.DmpThirdInboundEntity}
 * 的 {@code detail_list_json} 字段，供下游 {@code WegoInboundRocketMQTaskHandler} 反序列化为 SKU 明细列表。
 * <p>
 * 多例：因父类持有成员变量，最终实现类需由 Spring 管理为 {@link Scope}({@code prototype})。
 */
@Service
@Scope("prototype")
public class WegoInBoundDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            List<Map<String, Object>> mongoDataMaps = entry.getKey();
            if (mongoDataMaps == null || mongoDataMaps.isEmpty()) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            Object skuList = mongoData.get("skuList");
            if (skuList != null) {
                for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                    dmpDataMap.put("detailListJson", JSON.toJSONString(skuList));
                }
            }
        }
    }
}
