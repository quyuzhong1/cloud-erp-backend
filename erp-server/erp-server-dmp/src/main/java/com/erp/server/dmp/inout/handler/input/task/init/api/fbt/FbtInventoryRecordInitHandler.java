package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
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
    private static final String KEY_UNIQUE_ID = "uniqueId";
    private static final String KEY_INBOUND_ORDER_ID = "inboundOrderId";
    private static final String KEY_INBOUND_ORDER_ID_UNDERLINE = "inbound_order_id";
    private static final String KEY_PLATFORM_WAREHOUSE_CODE = "platformWarehouseCode";
    private static final String KEY_FBT_WAREHOUSE_ID = "fbt_warehouse_id";
    private static final String KEY_WAREHOUSE_CODE = "warehouse_code";
    private static final String KEY_WAREHOUSE_CODE_CAMEL = "warehouseCode";
    private static final String KEY_PRODUCT_SKU = "productSku";
    private static final String KEY_SELLER_SKU = "seller_sku";
    private static final String KEY_SKU_CODE = "sku_code";
    private static final String KEY_SKU_CODE_CAMEL = "skuCode";
    private static final String KEY_GOODS_ID = "goodsId";
    private static final String KEY_GOODS_ID_UNDERLINE = "goods_id";
    private static final String KEY_DELTA_QTY = "deltaQty";
    private static final String KEY_CHANGE_QUANTITY = "change_quantity";
    private static final String KEY_EVENT_TIME = "eventTime";
    private static final String KEY_EVENT_TIME_UNDERLINE = "event_time";
    private static final String AUTH_JSON_SHOP_ID = "shopId";

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            log.warn("未找到已授权FBT仓配置");
            return Collections.emptyList();
        }

        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> StrUtil.equalsIgnoreCase(e.getId(), dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new ServiceException("FBT授权信息不存在,nextLevelId:" + dmpInputTaskEntity.getNextLevelId());
        }

        String shopId = getAuthShopId(provider);
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

    private String getAuthShopId(OverseasProviderEntity provider) {
        Map<String, Object> authJson = provider.getAuthJson();
        String shopId = authJson == null ? null : String.valueOf(authJson.get(AUTH_JSON_SHOP_ID));
        if (StrUtil.isBlank(shopId) || "null".equalsIgnoreCase(shopId)) {
            throw new ServiceException("FBT授权信息缺少shopId, authId:" + provider.getId());
        }
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getId, shopId)
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollUtil.isEmpty(shopInfoList)) {
            throw new ServiceException("TikTok店铺不存在或未授权, shopId:" + shopId);
        }
        return shopId;
    }

    private Map<String, Object> normalizeRecord(Map<String, Object> source, String authId, String shopId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String recordId = firstNotBlank(source.get(KEY_RECORD_ID_UNDERLINE), source.get(KEY_RECORD_ID));
        row.put(KEY_RECORD_ID, recordId);
        row.put(KEY_UNIQUE_ID, firstNotBlank(recordId, buildFallbackId(source, authId, shopId)));
        row.put(KEY_PLATFORM, OmsPlatformEnum.FBT.getCode());
        row.put(KEY_SOURCE_PLATFORM, OmsPlatformEnum.FBT.getCode());
        row.put(KEY_AUTH_ID, authId);
        row.put(KEY_SHOP_ID, shopId);
        row.put(KEY_INBOUND_ORDER_ID, firstNotBlank(source.get(KEY_INBOUND_ORDER_ID_UNDERLINE), source.get(KEY_INBOUND_ORDER_ID)));
        row.put(KEY_PLATFORM_WAREHOUSE_CODE, firstNotBlank(source.get(KEY_FBT_WAREHOUSE_ID), source.get(KEY_WAREHOUSE_CODE), source.get(KEY_WAREHOUSE_CODE_CAMEL)));
        row.put(KEY_PRODUCT_SKU, firstNotBlank(source.get(KEY_SELLER_SKU), source.get(KEY_SKU_CODE), source.get(KEY_SKU_CODE_CAMEL)));
        row.put(KEY_GOODS_ID, firstNotBlank(source.get(KEY_GOODS_ID_UNDERLINE), source.get(KEY_GOODS_ID)));
        row.put(KEY_DELTA_QTY, parseInt(source.get(KEY_CHANGE_QUANTITY), source.get(KEY_DELTA_QTY)));
        row.put(KEY_EVENT_TIME, source.get(KEY_EVENT_TIME_UNDERLINE));
        return row;
    }

    private String buildFallbackId(Map<String, Object> source, String authId, String shopId) {
        return StrUtil.format("{}:{}:{}:{}:{}",
                authId,
                shopId,
                firstNotBlank(source.get(KEY_INBOUND_ORDER_ID_UNDERLINE), source.get(KEY_INBOUND_ORDER_ID)),
                firstNotBlank(source.get(KEY_SELLER_SKU), source.get(KEY_SKU_CODE), source.get(KEY_SKU_CODE_CAMEL)),
                source.get(KEY_EVENT_TIME_UNDERLINE));
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
}
