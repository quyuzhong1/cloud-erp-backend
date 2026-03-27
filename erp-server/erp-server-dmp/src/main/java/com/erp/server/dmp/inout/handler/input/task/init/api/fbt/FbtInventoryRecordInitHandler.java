package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBT库存记录初始化处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtInventoryRecordInitHandler extends DmpInputInitHandler {

    private static final String KEY_SHOP_ID = "shopId";
    private static final String KEY_AUTH_ID = "authId";
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_SOURCE_PLATFORM = "sourcePlatform";
    private static final String KEY_RECORD_ID = "recordId";
    private static final String KEY_RECORD_ID_UNDERLINE = "record_id";
    private static final String KEY_ID = "id";
    private static final String KEY_UNIQUE_ID = "uniqueId";
    private static final String KEY_INBOUND_ORDER_ID = "inboundOrderId";
    private static final String KEY_INBOUND_ORDER_ID_UNDERLINE = "inbound_order_id";
    private static final String KEY_ORDER = "order";
    private static final String KEY_ORDER_TYPE = "orderType";
    private static final String KEY_ORDER_TYPE_UNDERLINE = "order_type";
    private static final String KEY_PLATFORM_WAREHOUSE_CODE = "platformWarehouseCode";
    private static final String KEY_FBT_WAREHOUSE_ID = "fbt_warehouse_id";
    private static final String KEY_WAREHOUSE_CODE = "warehouse_code";
    private static final String KEY_WAREHOUSE_CODE_CAMEL = "warehouseCode";
    private static final String KEY_PRODUCT_SKU = "productSku";
    private static final String KEY_SELLER_SKU = "seller_sku";
    private static final String KEY_SKU_CODE = "sku_code";
    private static final String KEY_SKU_CODE_CAMEL = "skuCode";
    private static final String KEY_REFERENCE_CODE = "reference_code";
    private static final String KEY_GOODS_ID = "goodsId";
    private static final String KEY_GOODS_ID_UNDERLINE = "goods_id";
    private static final String KEY_GOODS = "goods";
    private static final String KEY_DELTA_QTY = "deltaQty";
    private static final String KEY_CHANGE_QUANTITY = "change_quantity";
    private static final String KEY_CHANGED_QUANTITY = "changed_quantity";
    private static final String KEY_EVENT_TIME = "eventTime";
    private static final String KEY_EVENT_TIME_UNDERLINE = "event_time";
    private static final String KEY_CREATE_TIME = "create_time";

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;
    @Resource
    private FbtAuthorizedShopResolver fbtAuthorizedShopResolver;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        FbtAuthorizedShopResolver.ResolvedAuthContext resolvedAuthContext =
                fbtAuthorizedShopResolver.resolveByAuthId(dmpInputTaskEntity.getNextLevelId());
        OverseasProviderEntity provider = resolvedAuthContext.getProvider();
        String shopId = resolvedAuthContext.getShopId();
        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("TikTok店铺授权信息为空, shopId:" + shopId);
        }

        Long createTimeGe = toEpochSecond(dmpInputTaskEntity.getStartTime());
        Long createTimeLe = toEpochSecond(dmpInputTaskEntity.getEndTime());
        Map<String, Object> response = tikTokSdkClientService.searchFbtInventoryRecord(
                shopInfoDTO,
                Collections.emptyList(),
                Collections.emptyList(),
                createTimeGe,
                createTimeLe);

        Object recordObj = response == null ? null : response.get("inventory_records");
        if (!(recordObj instanceof List)) {
            log.info("FBT库存记录响应为空, authId:{}, shopId:{}", provider.getId(), shopId);
            return Collections.emptyList();
        }
        List<Map<String, Object>> recordList = (List<Map<String, Object>>) recordObj;
        if (CollUtil.isEmpty(recordList)) {
            log.info("FBT库存记录为空, authId:{}, shopId:{}", provider.getId(), shopId);
            return Collections.emptyList();
        }

        JSONArray rows = new JSONArray();
        for (Map<String, Object> record : recordList) {
            rows.add(normalizeRecord(record, provider.getId(), shopId));
        }

        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(rows.toJSONString());
        return Collections.singletonList(dto);
    }

    private Map<String, Object> normalizeRecord(Map<String, Object> source, String authId, String shopId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String recordId = firstNotBlank(source.get(KEY_RECORD_ID_UNDERLINE), source.get(KEY_RECORD_ID), source.get(KEY_ID));
        String inboundOrderId = firstNotBlank(
                source.get(KEY_INBOUND_ORDER_ID_UNDERLINE),
                source.get(KEY_INBOUND_ORDER_ID),
                getMapValue(source.get(KEY_ORDER), KEY_ID)
        );
        String goodsId = firstNotBlank(
                source.get(KEY_GOODS_ID_UNDERLINE),
                source.get(KEY_GOODS_ID),
                getMapValue(source.get(KEY_GOODS), KEY_ID)
        );
        String productSku = firstNotBlank(
                source.get(KEY_SELLER_SKU),
                source.get(KEY_SKU_CODE),
                source.get(KEY_SKU_CODE_CAMEL),
                source.get(KEY_REFERENCE_CODE),
                getMapValue(source.get(KEY_GOODS), KEY_REFERENCE_CODE)
        );
        Object eventTime = firstNotBlankObject(
                source.get(KEY_EVENT_TIME_UNDERLINE),
                source.get(KEY_EVENT_TIME),
                source.get(KEY_CREATE_TIME)
        );
        String platformWarehouseCode = firstNotBlank(
                source.get(KEY_FBT_WAREHOUSE_ID),
                source.get(KEY_WAREHOUSE_CODE),
                source.get(KEY_WAREHOUSE_CODE_CAMEL)
        );
        String orderType = firstNotBlank(
                source.get(KEY_ORDER_TYPE_UNDERLINE),
                source.get(KEY_ORDER_TYPE),
                getMapValue(source.get(KEY_ORDER), "type")
        );
        row.put(KEY_RECORD_ID, recordId);
        String uniqueId = firstNotBlank(recordId, buildFallbackId(source, authId, shopId));
        row.put(KEY_UNIQUE_ID, normalizeUniqueId(uniqueId));
        row.put(KEY_PLATFORM, OmsPlatformEnum.FBT.getCode());
        row.put(KEY_SOURCE_PLATFORM, OmsPlatformEnum.FBT.getCode());
        row.put(KEY_AUTH_ID, authId);
        row.put(KEY_SHOP_ID, shopId);
        row.put(KEY_INBOUND_ORDER_ID, inboundOrderId);
        row.put(KEY_ORDER_TYPE, orderType);
        row.put(KEY_PLATFORM_WAREHOUSE_CODE, platformWarehouseCode);
        row.put(KEY_PRODUCT_SKU, productSku);
        row.put(KEY_GOODS_ID, goodsId);
        row.put(KEY_DELTA_QTY, parseInt(source.get(KEY_CHANGED_QUANTITY), source.get(KEY_CHANGE_QUANTITY), source.get(KEY_DELTA_QTY)));
        row.put(KEY_EVENT_TIME, eventTime);
        return row;
    }

    private String buildFallbackId(Map<String, Object> source, String authId, String shopId) {
        String raw = StrUtil.join("|",
                safeVal(authId),
                safeVal(shopId),
                safeVal(firstNotBlank(
                        source.get(KEY_INBOUND_ORDER_ID_UNDERLINE),
                        source.get(KEY_INBOUND_ORDER_ID),
                        getMapValue(source.get(KEY_ORDER), KEY_ID)
                )),
                safeVal(firstNotBlank(
                        source.get(KEY_SELLER_SKU),
                        source.get(KEY_SKU_CODE),
                        source.get(KEY_SKU_CODE_CAMEL),
                        source.get(KEY_REFERENCE_CODE),
                        getMapValue(source.get(KEY_GOODS), KEY_REFERENCE_CODE)
                )),
                safeVal(firstNotBlank(
                        source.get(KEY_FBT_WAREHOUSE_ID),
                        source.get(KEY_WAREHOUSE_CODE),
                        source.get(KEY_WAREHOUSE_CODE_CAMEL)
                )),
                safeVal(firstNotBlank(
                        source.get(KEY_GOODS_ID_UNDERLINE),
                        source.get(KEY_GOODS_ID),
                        getMapValue(source.get(KEY_GOODS), KEY_ID)
                )),
                safeVal(firstNotBlank(
                        source.get(KEY_EVENT_TIME_UNDERLINE),
                        source.get(KEY_EVENT_TIME),
                        source.get(KEY_CREATE_TIME)
                )),
                String.valueOf(parseInt(source.get(KEY_CHANGED_QUANTITY), source.get(KEY_CHANGE_QUANTITY), source.get(KEY_DELTA_QTY)))
        );
        return "fbtir_" + MD5Util.toMD5(raw);
    }

    private String normalizeUniqueId(String uniqueId) {
        if (StrUtil.isBlank(uniqueId)) {
            return null;
        }
        if (uniqueId.length() <= 50) {
            return uniqueId;
        }
        return "fbtir_" + MD5Util.toMD5(uniqueId);
    }

    private String safeVal(String value) {
        return StrUtil.blankToDefault(value, "");
    }

    private Long toEpochSecond(LocalDateTime time) {
        return time == null ? null : time.toEpochSecond(ZoneOffset.ofHours(8));
    }

    private Integer parseInt(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (Exception ignore) {
            }
        }
        return 0;
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
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

    private Object firstNotBlankObject(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }

    private String getMapValue(Object source, String key) {
        if (!(source instanceof Map) || StrUtil.isBlank(key)) {
            return null;
        }
        Object value = ((Map<?, ?>) source).get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (StrUtil.isBlank(text) || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }
}
