package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.sbs.request.SbsInventoryRequest;
import com.sdk.oms.shopee.service.ShopeeSbsInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shopee FBS 库存 init 处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeFbsInventoryInitHandler extends DmpInputInitHandler {

    private static final int PAGE_SIZE = 100;
    private static final Set<String> WHS_REGIONS = new HashSet<>(Arrays.asList(
            "BR", "CN", "ID", "MY", "MX", "TH", "TW", "PH", "VN", "SG"));

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private ShopeeSbsInventoryService shopeeSbsInventoryService;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        List<CfgAppClientEntity> cfgAppClientEntityList = cfgAppClientService.lambdaQuery()
                .eq(CfgAppClientEntity::getBusinessType, appClientEnum.getBusinessType())
                .eq(CfgAppClientEntity::getDictPlatform, appClientEnum.getPlatform())
                .eq(CfgAppClientEntity::getPlatformType, appClientEnum.getPlatformType())
                .list();
        if (CollUtil.isEmpty(cfgAppClientEntityList)) {
            throw new ServiceException("shopee应用未配置");
        }
        CfgAppClientEntity cfgAppClientEntity = cfgAppClientEntityList.get(0);

        List<ShopAuthEntity> shopAuthEntityList = FeignQuery.create(ShopAuthEntity.class)
                .eq(ShopAuthEntity::getShopId, nextLevelId).list();
        if (CollUtil.isEmpty(shopAuthEntityList)) {
            throw new ServiceException("shopee授权未配置");
        }
        ShopAuthEntity shopAuthEntity = shopAuthEntityList.get(0);

        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(nextLevelId);
        if (shopInfo == null || StringUtils.isBlank(shopInfo.getDictCountryCode())) {
            log.info("【Shopee FBS库存】店铺国家为空，跳过同步，shopId={}", nextLevelId);
            return Collections.emptyList();
        }
        String whsRegion = shopInfo.getDictCountryCode().trim().toUpperCase();
        if (!WHS_REGIONS.contains(whsRegion)) {
            log.info("【Shopee FBS库存】店铺国家不在SBS区域白名单，shopId={}, whsRegion={}", nextLevelId, whsRegion);
            return Collections.emptyList();
        }

        SbsInventoryRequest request = SbsInventoryRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .whsRegion(whsRegion)
                .pageSize(PAGE_SIZE)
                .build();

        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            request.setPageNo(pageNo);
            ShopeeResponse response = execute(request);
            if (response == null || response.getResponse() == null) {
                break;
            }
            JSONObject result = response.getResponse();
            JSONArray itemList = result.getJSONArray("item_list");
            if (CollUtil.isEmpty(itemList)) {
                break;
            }
            flattenInventory(resultList, itemList, whsRegion);
            if (itemList.size() < PAGE_SIZE) {
                break;
            }
            pageNo++;
        }
        return resultList;
    }

    private void flattenInventory(List<DmpInputTaskInitDTO> resultList, JSONArray itemList, String whsRegion) {
        for (int i = 0; i < itemList.size(); i++) {
            JSONObject item = itemList.getJSONObject(i);
            String itemId = stringValue(item.get("item_id"));
            String itemName = stringValue(item.get("item_name"));
            JSONArray skuList = item.getJSONArray("sku_list");
            if (CollUtil.isEmpty(skuList)) {
                continue;
            }
            for (int j = 0; j < skuList.size(); j++) {
                JSONObject sku = skuList.getJSONObject(j);
                String modelId = stringValue(sku.get("model_id"));
                String modelName = stringValue(sku.get("model_name"));
                String modelSku = stringValue(sku.get("model_sku"));
                String mtsku = stringValue(sku.get("mtsku"));
                String shopSkuId = stringValue(sku.get("shop_sku_id"));
                String warehouseItemId = stringValue(sku.get("warehouse_item_id"));
                JSONArray whsList = sku.getJSONArray("whs_list");
                if (CollUtil.isEmpty(whsList)) {
                    continue;
                }
                for (int k = 0; k < whsList.size(); k++) {
                    JSONObject whs = whsList.getJSONObject(k);
                    String whsId = stringValue(whs.get("whs_id"));
                    String fbsSku = StringUtils.defaultIfBlank(mtsku, modelSku);
                    if (StringUtils.isBlank(fbsSku) || StringUtils.isBlank(whsId)) {
                        continue;
                    }
                    JSONObject row = new JSONObject();
                    row.set("rowKey", nextLevelId + "_" + whsId + "_" + fbsSku);
                    row.set("whsRegion", whsRegion);
                    row.set("warehouseItemId", warehouseItemId);
                    row.set("shopSkuId", shopSkuId);
                    row.set("itemId", itemId);
                    row.set("modelId", modelId);
                    row.set("fbsSku", fbsSku);
                    row.set("platformSku", modelSku);
                    row.set("platformProductName", itemName);
                    row.set("specName", modelName);
                    row.set("warehouseId", whsId);
                    row.set("warehouseName", whsId);
                    row.set("purchaseMode", stringValue(whs.get("purchase_mode")));
                    row.set("recommendedReplenishmentQty", intValue(whs.get("to_replenish_qty")));
                    int sellableQty = intValue(whs.get("sellable_qty"));
                    int reservedQty = intValue(whs.get("reserved_qty"));
                    int unsellableQty = intValue(whs.get("unsellable_qty"));
                    int inTransitQty = intValue(whs.get("in_transit_pending_putaway_qty"));
                    row.set("totalStockQty", sellableQty + reservedQty + unsellableQty + inTransitQty);
                    row.set("stockedInboundQty", intValue(whs.get("ir_approval_qty")));
                    row.set("transferAsnInboundQty", 0);
                    row.set("reservedQty", reservedQty);
                    row.set("unsellableQty", unsellableQty);
                    row.set("inTransitQty", inTransitQty);
                    row.set("turnoverDays", intValue(whs.get("coverage_days")));
                    row.set("warehouseInventoryCoverageDays", intValue(whs.get("in_whs_coverage_days")));
                    row.set("dailyAvgSalesQty", decimalValue(whs.get("selling_speed")));
                    row.set("last7DaysSalesQty", intValue(whs.get("last_7_sold")));
                    row.set("last15DaysSalesQty", intValue(whs.get("last_15_sold")));
                    row.set("last30DaysSalesQty", intValue(whs.get("last_30_sold")));
                    row.set("last60DaysSalesQty", intValue(whs.get("last_60_sold")));
                    row.set("last90DaysSalesQty", intValue(whs.get("last_90_sold")));
                    row.set("stockAge030Qty", intValue(whs.get("qty_of_stock_age_one")));
                    row.set("stockAge3160Qty", intValue(whs.get("qty_of_stock_age_two")));
                    row.set("stockAge6190Qty", intValue(whs.get("qty_of_stock_age_three")));
                    row.set("stockAge91120Qty", intValue(whs.get("qty_of_stock_age_four")));
                    row.set("stockAge121180Qty", intValue(whs.get("qty_of_stock_age_five")));
                    row.set("stockAgeOver180Qty", intValue(whs.get("qty_of_stock_age_six")));

                    DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
                    dto.setMsg(JSON.toJSONString(row));
                    resultList.add(dto);
                }
            }
        }
    }

    private ShopeeResponse execute(SbsInventoryRequest request) {
        ShopeeResponse response = null;
        long sleepTime = 1000;
        int count = 0;
        while (response == null) {
            try {
                response = shopeeSbsInventoryService.getCurrentInventory(request);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
                    if (count == 10) {
                        throw new ServiceException("调用Shopee FBS库存接口重试10次失败");
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    sleepTime += 1000;
                    count++;
                    continue;
                }
                throw new ServiceException("调用Shopee FBS库存接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
            }
            if (response != null && StringUtils.isNotBlank(response.getError())) {
                throw new ServiceException("调用Shopee FBS库存接口报错，错误原因：" + response.getMessage());
            }
            break;
        }
        return response;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
