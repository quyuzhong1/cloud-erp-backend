package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.OmsPlatformEnum;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBT库存初始化处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtInventoryInitHandler extends DmpInputInitHandler {

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

        Map<String, Object> response = tikTokSdkClientService.searchFbtInventory(shopInfoDTO, Collections.emptyList(), Collections.emptyList());
        Object inventoryObj = response == null ? null : response.get("inventory_list");
        if (!(inventoryObj instanceof List)) {
            log.info("FBT库存响应为空, authId:{}, shopId:{}", provider.getId(), shopId);
            return Collections.emptyList();
        }

        List<Map<String, Object>> inventoryList = (List<Map<String, Object>>) inventoryObj;
        if (CollUtil.isEmpty(inventoryList)) {
            log.info("FBT库存列表为空, authId:{}, shopId:{}", provider.getId(), shopId);
            return Collections.emptyList();
        }

        JSONArray rows = new JSONArray();
        for (Map<String, Object> inventory : inventoryList) {
            rows.add(normalizeInventoryRow(inventory, provider.getId(), shopId));
        }

        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(rows.toJSONString());
        return Collections.singletonList(dto);
    }

    private Map<String, Object> normalizeInventoryRow(Map<String, Object> source, String authId, String shopId) {
        Map<String, Object> row = new LinkedHashMap<>(source);
        Map<String, Object> warehouse = asMap(source.get("warehouse"));
        if (warehouse == null) {
            warehouse = asMap(source.get("fbt_warehouse"));
        }
        Map<String, Object> sku = asMap(source.get("sku"));
        Map<String, Object> goods = asMap(source.get("goods"));
        Map<String, Object> onHandDetail = asMap(source.get("on_hand_detail"));
        Map<String, Object> skuOnHandDetail = firstSkuOnHandDetail(goods);

        row.put("authId", authId);
        row.put("shopId", shopId);
        row.put("sourcePlatform", OmsPlatformEnum.FBT.getCode());
        row.put("sourceSystem", OmsPlatformEnum.FBT.getCode());

        row.put("fbt_warehouse_id", firstNotBlank(
                source.get("fbt_warehouse_id"),
                source.get("warehouse_id"),
                getMapValue(warehouse, "fbt_warehouse_id"),
                getMapValue(warehouse, "warehouse_id"),
                getMapValue(warehouse, "id")));
        row.put("warehouse_name", firstNotBlank(
                source.get("fbt_warehouse_name"),
                source.get("warehouse_name"),
                getMapValue(warehouse, "name")));
        row.put("goods_id", firstNotBlank(
                source.get("goods_id"),
                source.get("id"),
                getMapValue(goods, "id")));
        row.put("goods_name", firstNotBlank(
                source.get("goods_name"),
                source.get("name"),
                getMapValue(goods, "name")));
        row.put("seller_sku", firstNotBlank(
                source.get("goods_id"),
                source.get("id"),
                getMapValue(goods, "id"),
                source.get("seller_sku"),
                source.get("sku"),
                source.get("reference_code"),
                getMapValue(sku, "seller_sku"),
                getMapValue(sku, "sku"),
                getMapValue(sku, "code"),
                getMapValue(goods, "reference_code")));
        row.put("available_quantity", firstNotBlank(
                source.get("available_quantity"),
                source.get("sellable_quantity"),
                getMapValue(onHandDetail, "available_quantity"),
                getMapValue(skuOnHandDetail, "available_quantity")));
        row.put("reserved_quantity", firstNotBlank(
                source.get("reserved_quantity"),
                getMapValue(onHandDetail, "reserved_quantity"),
                getMapValue(skuOnHandDetail, "reserved_quantity")));
        row.put("unfulfillable_quantity", firstNotBlank(
                source.get("unfulfillable_quantity"),
                source.get("unsellable_quantity"),
                getMapValue(onHandDetail, "unfulfillable_quantity"),
                getMapValue(skuOnHandDetail, "unfulfillable_quantity")));
        row.put("in_transit_quantity", firstNotBlank(source.get("in_transit_quantity"), source.get("deliver_onway_quantity")));
        row.put("update_time", firstNotBlank(source.get("update_time"), source.get("updated_time"), source.get("event_time")));
        return row;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstSkuOnHandDetail(Map<String, Object> goods) {
        if (goods == null) {
            return null;
        }
        Object skusObj = goods.get("skus");
        if (!(skusObj instanceof List) || CollUtil.isEmpty((List<?>) skusObj)) {
            return null;
        }
        Object firstSku = ((List<?>) skusObj).get(0);
        if (!(firstSku instanceof Map)) {
            return null;
        }
        return asMap(((Map<String, Object>) firstSku).get("on_hand_detail"));
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private Object getMapValue(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
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
