package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.oms.enums.ListingInfoPlatformStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import com.sdk.wms.wego.enums.WegoSkuStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * WEGO SKU → {@code dmp_product_info}/{@code dmp_sku_info}（旧链路）。
 * <p>
 * 将源端状态 {@code 1/4} 归一为 {@code Active/Inactive} 写入 {@code dmp_sku_info.status}，
 * 供 Output MQ 落到 {@code PlatformProductDTO.platformStatus}。
 */
@Service
@Scope("prototype")
public class WegoSkuInfoDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        List<Map<String, Object>> resultList = new LinkedList<>();
        String sku = toStr(dmpInputMongoEntity.get("sku"));
        if (StringUtils.isBlank(sku)) {
            return resultList;
        }
        Integer statusCode = parseStatusCode(dmpInputMongoEntity.get("status"));
        if (!WegoSkuStatusEnum.needSync(statusCode)) {
            return resultList;
        }

        Map<String, Object> map = dmpInputMongoEntity;
        String name = StringUtils.defaultString(toStr(dmpInputMongoEntity.get("name")));
        String barcode = joinBarcodes(dmpInputMongoEntity);
        String platformStatus = statusCode != null && statusCode == WegoSkuStatusEnum.ABANDONED.getCode()
                ? ListingInfoPlatformStatusEnum.INACTIVE.getCode()
                : ListingInfoPlatformStatusEnum.ACTIVE.getCode();

        map.put("spuId", sku);
        map.put("spuNo", sku);
        map.put("spuName", name);
        map.put("skuId", barcode);
        map.put("skuNo", sku);
        map.put("name", name);
        map.put("status", platformStatus);
        map.put("sourcePlatform", OmsPlatformEnum.WE_GO.getCode());
        resultList.add(map);
        return resultList;
    }

    /**
     * 解析 WEGO status：兼容 Number 与可解析字符串（如 {@code "1"}/{@code "4"}），避免 Mongo 反序列化成字符串时整批丢弃。
     */
    private Integer parseStatusCode(Object statusObj) {
        if (statusObj instanceof Number) {
            return ((Number) statusObj).intValue();
        }
        String text = toStr(statusObj);
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析 WEGO {@code barcode}：OpenAPI 为 string 数组，兼容单字符串；逗号拼接写入 skuId。
     */
    private String joinBarcodes(Map<String, Object> mongoData) {
        List<String> barcodeList = new ArrayList<>();
        Object barcodeObj = mongoData.get("barcode");
        if (barcodeObj instanceof List) {
            for (Object b : (List<?>) barcodeObj) {
                if (b != null && StringUtils.isNotBlank(b.toString())) {
                    barcodeList.add(b.toString());
                }
            }
            return barcodeList.stream().collect(Collectors.joining(","));
        }
        String barcode = toStr(barcodeObj);
        return StringUtils.defaultString(barcode);
    }

    /** 空安全转字符串。 */
    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
