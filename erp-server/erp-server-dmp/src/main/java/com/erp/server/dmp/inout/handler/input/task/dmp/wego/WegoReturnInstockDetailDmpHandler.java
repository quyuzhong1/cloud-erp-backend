package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import com.sdk.wms.wego.enums.WegoEnums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * WEGO 退货入库单明细 DMP handler。
 * <p>
 * 从 Mongo 主记录中拆分 SKU 明细行，对应落入 {@code dmp_third_return_inbound_detail}。
 * <p>
 * 明细来源优先级：
 * <ol>
 *   <li>{@code queryProducts}（预计/计划入库明细）—— WEGO 分页接口（queryPage）中
 *       即使 status=6 也以此字段为准，{@code instockProducts} 在分页接口中可能为空；</li>
 *   <li>{@code instockProducts}（实际入库明细）—— {@code queryProducts} 为空时的兜底。</li>
 * </ol>
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    /** WEGO API 实际入库明细字段（{@code queryProducts} 为空时的兜底，见 {@link #getDetailList}） */
    private static final String MONGO_KEY_INSTOCK_PRODUCTS = "instockProducts";

    /** WEGO API 预计入库明细字段（实际明细为空时的回退） */
    private static final String MONGO_KEY_QUERY_PRODUCTS = "queryProducts";

    /** WEGO API 返回的库存类型字段名，取值见 {@link WegoEnums.InventoryTypeEnum} */
    private static final String MONGO_KEY_INVENTORY_TYPE = "inventoryType";
    /** DMP 明细字段：是否不良品 */
    private static final String DMP_KEY_DEFECTIVE_PRODUCT_FLAG = "defectiveProductFlag";

    /**
     * 将父级 MongoDB 文档中的 {@code inventoryType} 转换为 {@code defectiveProductFlag}，
     * 并写入每条明细实体 Map。
     * <p>
     * {@code inventoryType} 是订单级字段，无法通过 {@code instockProducts} 数组元素直接获取，
     * 因此需要在此处从 {@code entry.getKey().get(0)}（即完整的父级 MongoDB 文档）读取后，
     * 回填到所有明细实体 Map 中。
     */
    @Override
    protected void afterConvertData(
            Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry
                : dmpInputDataDmpRelationMaps.entrySet()) {
            List<Map<String, Object>> mongoDataList = entry.getKey();
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (dmpDataMaps == null || dmpDataMaps.isEmpty()) {
                continue;
            }
            if (mongoDataList == null || mongoDataList.isEmpty()) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataList.get(0);
            Object inventoryTypeObj = mongoData.get(MONGO_KEY_INVENTORY_TYPE);
            if (inventoryTypeObj == null) {
                continue;
            }
            boolean defectiveProductFlag = WegoEnums.InventoryTypeEnum.isDefective(inventoryTypeObj.toString());
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, defectiveProductFlag);
            }
        }
    }

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        // 优先取预计入库明细（queryProducts），WEGO 分页接口 instockProducts 可能为空
        List<Map<String, Object>> result = parseProductList(dmpInputMongoEntity, MONGO_KEY_QUERY_PRODUCTS);
        if (CollectionUtils.isEmpty(result)) {
            result = parseProductList(dmpInputMongoEntity, MONGO_KEY_INSTOCK_PRODUCTS);
        }
        return result;
    }

    private List<Map<String, Object>> parseProductList(Map<String, Object> mongoEntity, String key) {
        Object listObj = mongoEntity.get(key);
        if (listObj == null) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(listObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object item : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(item);
            resultList.add(jsonObject);
        }
        return resultList;
    }
}
