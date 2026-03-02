package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.MD5Util;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBT库存记录推送FBA签收处理器
 */
@Service
@Scope("prototype")
public class FbtFbaShipmentReceiveRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final ZoneOffset ZONE_OFFSET_8 = ZoneOffset.ofHours(8);
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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

        Map<String, FbaReceiveGroupEntity> groupedMap = new LinkedHashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> entry : changeMongoMap.entrySet()) {
            List<Map<String, Object>> rows = entry.getValue();
            if (CollUtil.isEmpty(rows)) {
                continue;
            }
            for (Map<String, Object> row : rows) {
                if (row == null || this.validateDataBlack(row, cfgOutputId)) {
                    continue;
                }
                String inboundOrderId = firstNotBlank(row, KEY_INBOUND_ORDER_ID, KEY_INBOUND_ORDER_ID_UNDERLINE);
                String shopId = firstNotBlank(row, KEY_SHOP_ID, KEY_SHOP_ID_UNDERLINE);
                String fnSku = firstNotBlank(row, KEY_GOODS_ID, KEY_GOODS_ID_UNDERLINE);
                String msku = firstNotBlank(row, KEY_PRODUCT_SKU, KEY_SELLER_SKU, KEY_SKU_CODE);
                Integer qty = parseInt(firstNotBlank(row, KEY_DELTA_QTY, KEY_CHANGE_QUANTITY));
                if (StrUtil.isBlank(inboundOrderId) || StrUtil.isBlank(shopId)
                        || StrUtil.isBlank(fnSku) || StrUtil.isBlank(msku)
                        || qty == null || qty == 0) {
                    continue;
                }
                OffsetDateTime eventTime = parseEventTime(row.get(KEY_EVENT_TIME));
                if (eventTime == null) {
                    eventTime = parseEventTime(row.get(KEY_EVENT_TIME_UNDERLINE));
                }
                if (eventTime == null) {
                    eventTime = OffsetDateTime.now(ZONE_OFFSET_8);
                }

                String authId = firstNotBlank(row, KEY_AUTH_ID, KEY_AUTH_ID_UNDERLINE);
                String groupSeed = StrUtil.join("|", inboundOrderId, shopId, authId, eventTime.toLocalDate());
                String groupUniqueId = normalizeUniqueId("fbtrcv_" + MD5Util.toMD5(groupSeed));
                String dataId = normalizeUniqueId("fbtrcvd_" + MD5Util.toMD5(groupSeed));

                OffsetDateTime finalEventTime = eventTime;
                FbaReceiveGroupEntity groupEntity = groupedMap.computeIfAbsent(dataId, key -> buildGroupEntity(
                        inboundOrderId, shopId, authId, finalEventTime.toLocalDate(), groupUniqueId, row));
                groupEntity.getDetailList().add(buildDetailEntity(row, inboundOrderId, shopId, authId, eventTime, qty, fnSku, msku));
            }
        }

        for (Map.Entry<String, FbaReceiveGroupEntity> entry : groupedMap.entrySet()) {
            result.put(entry.getKey(), JSON.toJSONString(entry.getValue()));
        }
        return result;
    }

    private FbaReceiveGroupEntity buildGroupEntity(String inboundOrderId,
                                                   String shopId,
                                                   String authId,
                                                   LocalDate requestReceiveDate,
                                                   String uniqueId,
                                                   Map<String, Object> row) {
        FbaReceiveGroupEntity groupEntity = new FbaReceiveGroupEntity();
        groupEntity.setSid(authId);
        groupEntity.setAuthId(authId);
        groupEntity.setShopId(shopId);
        groupEntity.setFbaShipmentId(inboundOrderId);
        groupEntity.setRequestReceiveDate(requestReceiveDate);
        groupEntity.setUniqueId(uniqueId);
        groupEntity.setPlatform(firstNotBlank(row, KEY_PLATFORM, KEY_SOURCE_PLATFORM, KEY_SOURCE_PLATFORM_UNDERLINE));
        if (StrUtil.isBlank(groupEntity.getPlatform())) {
            groupEntity.setPlatform(OmsPlatformEnum.FBT.getCode());
        }
        groupEntity.setDetailList(new ArrayList<>());
        return groupEntity;
    }

    private FbaReceiveDetailEntity buildDetailEntity(Map<String, Object> row,
                                                     String inboundOrderId,
                                                     String shopId,
                                                     String authId,
                                                     OffsetDateTime eventTime,
                                                     Integer qty,
                                                     String fnSku,
                                                     String msku) {
        String uniqueId = normalizeUniqueId(firstNotBlank(row, KEY_UNIQUE_ID, KEY_RECORD_ID, KEY_RECORD_ID_UNDERLINE));
        if (StrUtil.isBlank(uniqueId)) {
            String recordSeed = StrUtil.join("|",
                    inboundOrderId,
                    shopId,
                    authId,
                    firstNotBlank(row, KEY_PRODUCT_SKU, KEY_SELLER_SKU, KEY_SKU_CODE),
                    firstNotBlank(row, KEY_GOODS_ID, KEY_GOODS_ID_UNDERLINE),
                    eventTime.toString(),
                    qty);
            uniqueId = normalizeUniqueId("fbtir_" + MD5Util.toMD5(recordSeed));
        }

        String receiveTime = TIME_FORMATTER.format(eventTime);
        FbaReceiveDetailEntity detail = new FbaReceiveDetailEntity();
        detail.setShopId(shopId);
        detail.setSid(parseInteger(authId));
        detail.setReceivedDateStr(receiveTime);
        detail.setReceivedDateLocaleStr(receiveTime);
        detail.setFnsku(fnSku);
        detail.setSku(msku);
        detail.setQuantity(qty);
        detail.setFbaShipmentId(inboundOrderId);
        detail.setFulfillmentCenterId(firstNotBlank(row, KEY_PLATFORM_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE_UNDERLINE, KEY_FBT_WAREHOUSE_ID));
        detail.setUniqueMd5(uniqueId);
        detail.setUniqueIndex(calculateUniqueIndex(uniqueId));
        detail.setReceivedDateReport(eventTime.toLocalDate());
        return detail;
    }

    private Integer calculateUniqueIndex(String uniqueId) {
        if (StrUtil.isBlank(uniqueId)) {
            return 0;
        }
        int hash = uniqueId.hashCode();
        return hash == Integer.MIN_VALUE ? 0 : Math.abs(hash);
    }

    private OffsetDateTime parseEventTime(Object eventTimeObj) {
        if (eventTimeObj == null) {
            return null;
        }
        if (eventTimeObj instanceof OffsetDateTime) {
            return (OffsetDateTime) eventTimeObj;
        }
        if (eventTimeObj instanceof LocalDateTime) {
            return ((LocalDateTime) eventTimeObj).atOffset(ZONE_OFFSET_8);
        }
        if (eventTimeObj instanceof Date) {
            Date date = (Date) eventTimeObj;
            return date.toInstant().atOffset(ZONE_OFFSET_8);
        }
        if (eventTimeObj instanceof Number) {
            long value = ((Number) eventTimeObj).longValue();
            if (value > 99999999999L) {
                return Instant.ofEpochMilli(value).atOffset(ZONE_OFFSET_8);
            }
            return Instant.ofEpochSecond(value).atOffset(ZONE_OFFSET_8);
        }

        String text = String.valueOf(eventTimeObj).trim();
        if (StrUtil.isBlank(text) || "null".equalsIgnoreCase(text)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text);
        } catch (DateTimeParseException ignore) {
        }
        try {
            return Instant.parse(text).atOffset(ZONE_OFFSET_8);
        } catch (DateTimeParseException ignore) {
        }
        try {
            return LocalDateTime.parse(text).atOffset(ZONE_OFFSET_8);
        } catch (DateTimeParseException ignore) {
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atOffset(ZONE_OFFSET_8);
        } catch (DateTimeParseException ignore) {
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

    private Integer parseInteger(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ignore) {
            return null;
        }
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

    private String normalizeUniqueId(String uniqueId) {
        if (StrUtil.isBlank(uniqueId)) {
            return null;
        }
        if (uniqueId.length() <= 50) {
            return uniqueId;
        }
        return "fbt_" + MD5Util.toMD5(uniqueId);
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("fbaShipmentId");
    }
}
