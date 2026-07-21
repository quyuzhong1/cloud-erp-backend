package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 爱亚（AIYA/百世 GLINK）入库 DMP 转换扩展 handler，对齐 {@code WegoInBoundDmpHandler} / {@code JiFengInBoundDmpHandler}。
 * <p>
 * 把 mongo 缓存中的入库单明细数组（{@code asnItems}，对应
 * {@link com.sdk.wms.aiya.dto.response.AiyaInboundResp.AsnLineItemDTO}）整体拍平到
 * {@link com.erp.model.dmp.entity.DmpThirdInboundEntity} 的 {@code detail_list_json} 字段，
 * 供下游 {@code AiyaInboundRocketMQTaskHandler} 反序列化后按验货流水（含良品/不良品）生成签收/调拨/库存数据。
 * <p>
 * 同时把 {@code warehouse_platform_type} 兜底为
 * {@link WarehousePlatformTypeEnum#OVERSEAS_WAREHOUSE OVERSEAS_WAREHOUSE}，保证下游
 * {@code PlatformInboundConsumerService.handle} 路由到海外仓处理服务；与 wego/tiktok 兜底策略一致。
 * <p>
 * 多例：父类持有成员变量，最终实现类需 {@link Scope}({@code prototype})。
 */
@Service
@Scope("prototype")
public class AiyaInBoundDmpHandler extends DmpInputDbConvertDmpHandler {

    /**
     * 爱亚入库 mongo 缓存中「入库单明细」字段名，对应
     * {@link com.sdk.wms.aiya.dto.response.AiyaInboundResp.AsnInfoDTO#getAsnLineItems()}
     * 归一化后统一以 {@code asnItems} 键承载。
     */
    private static final String MONGO_KEY_ASN_ITEMS = "asnItems";

    /**
     * {@link com.erp.model.dmp.entity.DmpThirdInboundEntity} 的仓库平台类型字段名，
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
            Object asnItems = mongoData.get(MONGO_KEY_ASN_ITEMS);
            if (asnItems == null) {
                continue;
            }
            String detailJson = JSON.toJSONString(asnItems);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put(DMP_KEY_DETAIL_LIST_JSON, detailJson);
            }
        }
    }

    /**
     * 把 dmp_third_inbound 的 warehouse_platform_type 统一覆盖为 overseasWarehouse。
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
