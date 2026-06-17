package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.jifeng.JiFengReturnInstockDetailDmpHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * WEGO 退货入库单明细 DMP handler。
 * <p>
 * 从 Mongo 主记录的 {@code instockProducts}（实际入库明细）中拆分出明细行，
 * 每行包含 {@code sku} 和 {@code qty}，对应落入 {@code dmp_third_return_inbound_detail}。
 * <p>
 * 与 {@link JiFengReturnInstockDetailDmpHandler} 类比，极风取 {@code skuList} 字段，
 * WEGO 取 {@code instockProducts} 字段；字段结构均为 {@code [{sku, qty}]} 数组。
 * <p>
 * 注意：当 {@code instockProducts} 为空时，框架会继续尝试 {@code queryProducts}（预计入库明细）。
 * 如需严格只用实际入库数据，在 InitHandler 层已过滤 status=6（已处理），
 * 「已处理」状态的退货单 instockProducts 通常非空。
 */
@Service
@Scope("prototype")
public class WegoReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    /** WEGO API 实际入库明细字段（优先使用） */
    private static final String MONGO_KEY_INSTOCK_PRODUCTS = "instockProducts";

    /** WEGO API 预计入库明细字段（实际明细为空时的回退） */
    private static final String MONGO_KEY_QUERY_PRODUCTS = "queryProducts";

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        // 优先取实际入库明细，回退到预计入库明细
        List<Map<String, Object>> result = parseProductList(dmpInputMongoEntity, MONGO_KEY_INSTOCK_PRODUCTS);
        if (CollectionUtils.isEmpty(result)) {
            result = parseProductList(dmpInputMongoEntity, MONGO_KEY_QUERY_PRODUCTS);
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
