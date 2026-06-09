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
 * 把 mongo 缓存中的「实际入库明细」数组（{@code instocks}，对应
 * {@link com.sdk.wms.wego.dto.response.WegoInboundResp.InstockDTO}）整体拍平到
 * {@link com.erp.model.dmp.entity.DmpThirdInboundEntity} 的 {@code detail_list_json} 字段，
 * 供下游 {@code WegoInboundRocketMQTaskHandler} 反序列化后按
 * {@code instocks[].products[]} 笛卡尔展开生成签收流水。
 * <p>
 * 注意：WEGO 与极风（jifeng）的入库回写结构不同 —— 极风是 SKU 级的 {@code skuList}（含
 * {@code putawayCount} / {@code putawayLastTime} 等聚合字段），WEGO 是入库批次级
 * {@code instocks}（含 {@code defectiveProductFlag} / {@code batch} / {@code createTime}
 * / {@code upUserName} 等批次维度字段，箱内产品再展开为 {@code products}）。
 * <p>
 * 多例：因父类持有成员变量，最终实现类需由 Spring 管理为 {@link Scope}({@code prototype})。
 */
@Service
@Scope("prototype")
public class WegoInBoundDmpHandler extends DmpInputDbConvertDmpHandler {

    /**
     * WEGO 入库单 mongo 缓存中「实际入库明细」字段名，对应
     * {@link com.sdk.wms.wego.dto.response.WegoInboundResp.InorderDTO#getInstocks()}。
     */
    private static final String MONGO_KEY_INSTOCKS = "instocks";

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
            Object instocks = mongoData.get(MONGO_KEY_INSTOCKS);
            if (instocks == null) {
                continue;
            }
            String instocksJson = JSON.toJSONString(instocks);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("detailListJson", instocksJson);
            }
        }
    }
}
