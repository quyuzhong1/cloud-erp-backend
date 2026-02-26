package com.erp.server.wms.rocketmq.consumer.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.erp.server.wms.service.FbtInboundService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * FBT库存消息处理器
 */
@Component
public class FbtInventoryMessageHandler implements PlatformInventoryMessageHandler {

    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_SOURCE_PLATFORM = "sourcePlatform";
    private static final String KEY_SOURCE_PLATFORM_UNDERLINE = "source_platform";
    private static final String KEY_SHOP_ID = "shopId";
    private static final String KEY_SHOP_ID_UNDERLINE = "shop_id";
    private static final String KEY_AUTH_ID = "authId";
    private static final String KEY_AUTH_ID_UNDERLINE = "auth_id";
    private static final String KEY_RECORD_ID = "recordId";
    private static final String KEY_RECORD_ID_UNDERLINE = "record_id";
    private static final String KEY_INBOUND_ORDER_ID = "inboundOrderId";
    private static final String KEY_INBOUND_ORDER_ID_UNDERLINE = "inbound_order_id";
    private static final String KEY_WAREHOUSE_CODE = "warehouseCode";
    private static final String KEY_WAREHOUSE_CODE_UNDERLINE = "warehouse_code";
    private static final String KEY_PLATFORM_WAREHOUSE_CODE = "platformWarehouseCode";
    private static final String KEY_PLATFORM_WAREHOUSE_NAME = "platformWarehouseName";
    private static final String KEY_WAREHOUSE_NAME = "warehouseName";
    private static final String KEY_WAREHOUSE_NAME_UNDERLINE = "warehouse_name";
    private static final String KEY_FBT_WAREHOUSE_ID = "fbt_warehouse_id";
    private static final String KEY_PRODUCT_SKU = "productSku";
    private static final String KEY_SKU_CODE = "skuCode";
    private static final String KEY_SKU_CODE_UNDERLINE = "sku_code";
    private static final String KEY_SELLER_SKU = "seller_sku";
    private static final String KEY_GOODS_ID = "goodsId";
    private static final String KEY_GOODS_ID_UNDERLINE = "goods_id";
    private static final String KEY_GOODS_NAME = "goodsName";
    private static final String KEY_GOODS_NAME_UNDERLINE = "goods_name";
    private static final String KEY_DELTA_QTY = "deltaQty";
    private static final String KEY_CHANGE_QUANTITY = "change_quantity";
    private static final String KEY_EVENT_TIME = "eventTime";
    private static final String KEY_EVENT_TIME_UNDERLINE = "event_time";
    private static final String KEY_SELLABLE = "sellable";
    private static final String KEY_AVAILABLE_QTY = "availableQty";
    private static final String KEY_AVAILABLE_QUANTITY = "available_quantity";
    private static final String KEY_RESERVED = "reserved";
    private static final String KEY_RESERVED_QTY = "reservedQty";
    private static final String KEY_RESERVED_QUANTITY = "reserved_quantity";
    private static final String KEY_UNSELLABLE = "unsellable";
    private static final String KEY_UNFULFILLABLE_QTY = "unfulfillableQty";
    private static final String KEY_UNFULFILLABLE_QUANTITY = "unfulfillable_quantity";
    private static final String KEY_TRANSFER_ONWAY = "transferOnway";
    private static final String KEY_ONWAY = "onway";
    private static final String KEY_IN_TRANSIT_QTY = "inTransitQty";
    private static final String KEY_IN_TRANSIT_QUANTITY = "in_transit_quantity";
    private static final String KEY_DOWNLOAD_TIME = "downloadTime";
    private static final String KEY_UPDATED_TIME = "updatedTime";
    private static final String KEY_UPDATE_TIME = "update_time";

    @Resource
    private FbtInboundService fbtInboundService;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean supports(JSONObject message) {
        if (message == null) {
            return false;
        }
        return isFbtRecordMessage(message) || isFbtSnapshotMessage(message);
    }

    @Override
    public ApiResult<Object> handle(JSONObject message) {
        if (isFbtRecordMessage(message)) {
            fbtInboundService.handleInventoryRecordFromDmp(convertToRecord(message));
            return ApiResult.success();
        }
        if (isFbtSnapshotMessage(message)) {
            String authId = firstNotBlank(message, KEY_AUTH_ID, KEY_AUTH_ID_UNDERLINE);
            fbtInboundService.upsertInventorySnapshotFromDmp(convertToSnapshot(message), authId);
            return ApiResult.success();
        }
        return ApiResult.success();
    }

    private boolean isFbtRecordMessage(JSONObject message) {
        if (!isFbtMessage(message)) {
            return false;
        }
        return CharSequenceUtil.isNotBlank(firstNotBlank(message, KEY_INBOUND_ORDER_ID, KEY_INBOUND_ORDER_ID_UNDERLINE))
                || CharSequenceUtil.isNotBlank(firstNotBlank(message, KEY_RECORD_ID, KEY_RECORD_ID_UNDERLINE));
    }

    private boolean isFbtSnapshotMessage(JSONObject message) {
        if (!isFbtMessage(message)) {
            return false;
        }
        if (isFbtRecordMessage(message)) {
            return false;
        }
        return CharSequenceUtil.isNotBlank(firstNotBlank(message, KEY_PLATFORM_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE_UNDERLINE))
                && CharSequenceUtil.isNotBlank(firstNotBlank(message, KEY_PRODUCT_SKU, KEY_SKU_CODE, KEY_SKU_CODE_UNDERLINE));
    }

    private boolean isFbtMessage(JSONObject message) {
        String platform = firstNotBlank(message, KEY_PLATFORM, KEY_SOURCE_PLATFORM, KEY_SOURCE_PLATFORM_UNDERLINE);
        return CharSequenceUtil.equalsIgnoreCase(platform, OmsPlatformEnum.FBT.getCode());
    }

    private TiktokFbtDTO.InventoryRecordDTO convertToRecord(JSONObject message) {
        TiktokFbtDTO.InventoryRecordDTO dto = new TiktokFbtDTO.InventoryRecordDTO();
        dto.setShopId(firstNotBlank(message, KEY_SHOP_ID, KEY_SHOP_ID_UNDERLINE));
        dto.setRecordId(firstNotBlank(message, KEY_RECORD_ID, KEY_RECORD_ID_UNDERLINE));
        dto.setInboundOrderId(firstNotBlank(message, KEY_INBOUND_ORDER_ID, KEY_INBOUND_ORDER_ID_UNDERLINE));
        dto.setWarehouseCode(firstNotBlank(message, KEY_PLATFORM_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE_UNDERLINE, KEY_FBT_WAREHOUSE_ID));
        dto.setSkuCode(firstNotBlank(message, KEY_PRODUCT_SKU, KEY_SKU_CODE, KEY_SKU_CODE_UNDERLINE, KEY_SELLER_SKU));
        dto.setGoodsId(firstNotBlank(message, KEY_GOODS_ID, KEY_GOODS_ID_UNDERLINE));
        dto.setDeltaQty(parseInt(firstNotBlank(message, KEY_DELTA_QTY, KEY_CHANGE_QUANTITY)));
        dto.setEventTime(parseDateTime(firstNotBlank(message, KEY_EVENT_TIME, KEY_EVENT_TIME_UNDERLINE)));
        return dto;
    }

    private TiktokFbtDTO.InventorySnapshotDTO convertToSnapshot(JSONObject message) {
        TiktokFbtDTO.InventorySnapshotDTO dto = new TiktokFbtDTO.InventorySnapshotDTO();
        dto.setShopId(firstNotBlank(message, KEY_SHOP_ID, KEY_SHOP_ID_UNDERLINE));
        dto.setWarehouseCode(firstNotBlank(message, KEY_PLATFORM_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE, KEY_WAREHOUSE_CODE_UNDERLINE, KEY_FBT_WAREHOUSE_ID));
        dto.setWarehouseName(firstNotBlank(message, KEY_PLATFORM_WAREHOUSE_NAME, KEY_WAREHOUSE_NAME, KEY_WAREHOUSE_NAME_UNDERLINE));
        dto.setSkuCode(firstNotBlank(message, KEY_PRODUCT_SKU, KEY_SKU_CODE, KEY_SKU_CODE_UNDERLINE, KEY_SELLER_SKU));
        dto.setGoodsId(firstNotBlank(message, KEY_GOODS_ID, KEY_GOODS_ID_UNDERLINE));
        dto.setGoodsName(firstNotBlank(message, KEY_GOODS_NAME, KEY_GOODS_NAME_UNDERLINE));
        dto.setAvailableQty(parseInt(firstNotBlank(message, KEY_SELLABLE, KEY_AVAILABLE_QTY, KEY_AVAILABLE_QUANTITY)));
        dto.setReservedQty(parseInt(firstNotBlank(message, KEY_RESERVED, KEY_RESERVED_QTY, KEY_RESERVED_QUANTITY)));
        dto.setUnfulfillableQty(parseInt(firstNotBlank(message, KEY_UNSELLABLE, KEY_UNFULFILLABLE_QTY, KEY_UNFULFILLABLE_QUANTITY)));
        dto.setInTransitQty(parseInt(firstNotBlank(message, KEY_TRANSFER_ONWAY, KEY_ONWAY, KEY_IN_TRANSIT_QTY, KEY_IN_TRANSIT_QUANTITY)));
        dto.setUpdatedTime(parseDateTime(firstNotBlank(message, KEY_DOWNLOAD_TIME, KEY_UPDATED_TIME, KEY_UPDATE_TIME)));
        return dto;
    }

    private Integer parseInt(String raw) {
        if (CharSequenceUtil.isBlank(raw)) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (CharSequenceUtil.isBlank(raw)) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignore) {
        }
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignore) {
        }
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception ignore) {
        }
        try {
            long epochSecond = Long.parseLong(raw);
            if (epochSecond > 9999999999L) {
                epochSecond = epochSecond / 1000;
            }
            return LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.ofHours(8));
        } catch (Exception ignore) {
        }
        return null;
    }

    private String firstNotBlank(JSONObject message, String... keys) {
        if (message == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (CharSequenceUtil.isBlank(key)) {
                continue;
            }
            Object value = message.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (CharSequenceUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }
}
