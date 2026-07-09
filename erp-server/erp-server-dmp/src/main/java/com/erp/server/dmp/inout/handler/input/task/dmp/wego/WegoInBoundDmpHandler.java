package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.WarehousePlatformTypeEnum;
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
 * 同时在源头把 {@code warehousePlatformType} 兜底为
 * {@link WarehousePlatformTypeEnum#OVERSEAS_WAREHOUSE OVERSEAS_WAREHOUSE}（写入到
 * {@code dmp_third_inbound.warehouse_platform_type} 字段），保证下游
 * {@code PlatformInboundConsumerService.handle} 路由分支能正确命中海外仓处理服务；
 * 这与 {@code TikTokWarehouseDmpHandler} 的兜底策略保持一致，避免 WEGO 接口返回的
 * {@code warehouseBusiness} 等源平台 raw 值污染消费端路由字段。
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

    /**
     * {@link com.erp.model.dmp.entity.DmpThirdInboundEntity} 的字段名常量，
     * 与表字段 {@code warehouse_platform_type} 一一对应。
     */
    private static final String DMP_KEY_WAREHOUSE_PLATFORM_TYPE = "warehousePlatformType";

    /**
     * {@link com.erp.model.dmp.entity.DmpThirdInboundEntity} 的明细 JSON 字段名。
     */
    private static final String DMP_KEY_DETAIL_LIST_JSON = "detailListJson";

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (dmpDataMaps == null || dmpDataMaps.isEmpty()) {
                continue;
            }
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                fillWarehousePlatformType(dmpDataMap);
            }

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
                dmpDataMap.put(DMP_KEY_DETAIL_LIST_JSON, instocksJson);
            }
        }
    }

    /**
     * 把 dmp_third_inbound 的 warehouse_platform_type 字段统一覆盖为 overseasWarehouse。
     * <p>
     * 该字段是下游 {@code PlatformInboundConsumerService.handle} 的业务路由字段，WEGO 海外仓
     * 在该字段上只可能是 overseasWarehouse，不能被接口返回的源平台 raw 值（例如
     * WegoInboundResp 里 {@code warehouseBusiness="WEGO"}）或漏配/错配的 mapping 污染。
     * 因此无论上游 mapping 是否已写入、写入的是什么值，源头统一以枚举常量覆盖。
     */
    private void fillWarehousePlatformType(TreeMap<String, Object> dmpDataMap) {
        if (dmpDataMap == null) {
            return;
        }
        String overseasWarehouseCode = WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode();
        Object current = dmpDataMap.get(DMP_KEY_WAREHOUSE_PLATFORM_TYPE);
        if (current == null || !overseasWarehouseCode.equals(current.toString())) {
            dmpDataMap.put(DMP_KEY_WAREHOUSE_PLATFORM_TYPE, overseasWarehouseCode);
        }
    }
}
