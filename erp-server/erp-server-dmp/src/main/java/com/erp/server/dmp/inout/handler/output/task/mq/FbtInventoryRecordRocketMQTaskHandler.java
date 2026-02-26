package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBT库存记录MQ推送处理器
 */
@Service
@Scope("prototype")
public class FbtInventoryRecordRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String KEY_UNIQUE_ID = "uniqueId";
    private static final String KEY_RECORD_ID = "recordId";
    private static final String KEY_RECORD_ID_UNDERLINE = "record_id";
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_SOURCE_PLATFORM = "sourcePlatform";
    private static final String KEY_SOURCE_PLATFORM_UNDERLINE = "source_platform";
    private static final String KEY_AUTH_ID = "authId";
    private static final String KEY_AUTH_ID_UNDERLINE = "auth_id";
    private static final String KEY_SHOP_ID = "shopId";
    private static final String KEY_SHOP_ID_UNDERLINE = "shop_id";
    private static final String KEY_INBOUND_ORDER_ID = "inboundOrderId";
    private static final String KEY_INBOUND_ORDER_ID_UNDERLINE = "inbound_order_id";
    private static final String KEY_PLATFORM_WAREHOUSE_CODE = "platformWarehouseCode";
    private static final String KEY_WAREHOUSE_CODE_UNDERLINE = "warehouse_code";
    private static final String KEY_FBT_WAREHOUSE_ID = "fbt_warehouse_id";
    private static final String KEY_PRODUCT_SKU = "productSku";
    private static final String KEY_SELLER_SKU = "seller_sku";
    private static final String KEY_SKU_CODE = "sku_code";
    private static final String KEY_GOODS_ID = "goodsId";
    private static final String KEY_GOODS_ID_UNDERLINE = "goods_id";
    private static final String KEY_DELTA_QTY = "deltaQty";
    private static final String KEY_CHANGE_QUANTITY = "change_quantity";
    private static final String KEY_EVENT_TIME = "eventTime";
    private static final String KEY_EVENT_TIME_UNDERLINE = "event_time";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, String> result = new LinkedHashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();

        Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeMongoMap = dmpRequest.getChangeConvertInputMongoEntityListMaps();
        if (CollUtil.isEmpty(changeMongoMap)) {
            changeMongoMap = dmpRequest.getConvertInputMongoEntityListMaps();
        }
        if (CollUtil.isEmpty(changeMongoMap)) {
            return result;
        }

        for (Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> entry : changeMongoMap.entrySet()) {
            List<Map<String, Object>> rows = entry.getValue();
            if (CollUtil.isEmpty(rows)) {
                continue;
            }
            for (Map<String, Object> row : rows) {
                Map<String, Object> payload = buildPayload(row);
                if (this.validateDataBlack(payload, cfgOutputId)) {
                    continue;
                }
                String uniqueId = stringVal(payload.get(KEY_UNIQUE_ID));
                if (StrUtil.isBlank(uniqueId)) {
                    continue;
                }
                result.put(uniqueId, JSON.toJSONString(payload));
            }
        }
        return result;
    }

    private Map<String, Object> buildPayload(Map<String, Object> row) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(KEY_UNIQUE_ID, firstNotBlank(row, KEY_UNIQUE_ID, KEY_RECORD_ID, KEY_RECORD_ID_UNDERLINE));
        payload.put(KEY_PLATFORM, firstNotBlank(row, KEY_PLATFORM, KEY_SOURCE_PLATFORM, KEY_SOURCE_PLATFORM_UNDERLINE));
        payload.put(KEY_SOURCE_PLATFORM, firstNotBlank(row, KEY_SOURCE_PLATFORM, KEY_SOURCE_PLATFORM_UNDERLINE, KEY_PLATFORM));
        payload.put(KEY_AUTH_ID, firstNotBlank(row, KEY_AUTH_ID, KEY_AUTH_ID_UNDERLINE));
        payload.put(KEY_SHOP_ID, firstNotBlank(row, KEY_SHOP_ID, KEY_SHOP_ID_UNDERLINE));
        payload.put(KEY_RECORD_ID, firstNotBlank(row, KEY_RECORD_ID, KEY_RECORD_ID_UNDERLINE));
        payload.put(KEY_INBOUND_ORDER_ID, firstNotBlank(row, KEY_INBOUND_ORDER_ID, KEY_INBOUND_ORDER_ID_UNDERLINE));
        payload.put(KEY_PLATFORM_WAREHOUSE_CODE, firstNotBlank(row, KEY_PLATFORM_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE_UNDERLINE, KEY_FBT_WAREHOUSE_ID));
        payload.put(KEY_PRODUCT_SKU, firstNotBlank(row, KEY_PRODUCT_SKU, KEY_SELLER_SKU, KEY_SKU_CODE));
        payload.put(KEY_GOODS_ID, firstNotBlank(row, KEY_GOODS_ID, KEY_GOODS_ID_UNDERLINE));
        payload.put(KEY_DELTA_QTY, parseInt(firstNotBlank(row, KEY_DELTA_QTY, KEY_CHANGE_QUANTITY)));
        payload.put(KEY_EVENT_TIME, firstNotBlank(row, KEY_EVENT_TIME, KEY_EVENT_TIME_UNDERLINE));
        return payload;
    }

    private String firstNotBlank(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (StrUtil.isBlank(key)) {
                continue;
            }
            Object value = row.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    private Integer parseInt(String value) {
        if (StrUtil.isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private String stringVal(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList(KEY_RECORD_ID, KEY_INBOUND_ORDER_ID);
    }
}
