package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import com.sdk.wms.aiya.enums.AiyaSkuStatusEnum;
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
 * 爱亚 SKU → {@code dmp_product_info}/{@code dmp_sku_info}（旧链路）。
 * <p>
 * Mongo 原始字段整理为 convert_mapping 可用结构；后续由 Product Output MQ 推送到
 * {@code PlatformListingConsumer}。{@code storage_name} / 字段映射在环境库配置。
 */
@Service
@Scope("prototype")
public class AiyaSkuInfoDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        List<Map<String, Object>> resultList = new LinkedList<>();
        String sku = toStr(dmpInputMongoEntity.get("sku"));
        if (StringUtils.isBlank(sku)) {
            return resultList;
        }
        String status = toStr(dmpInputMongoEntity.get("status"));
        if (!AiyaSkuStatusEnum.needSync(status)) {
            return resultList;
        }

        Map<String, Object> map = dmpInputMongoEntity;
        String name = toStr(dmpInputMongoEntity.get("name"));
        if (StringUtils.isBlank(name)) {
            name = toStr(dmpInputMongoEntity.get("description"));
        }
        String barcode = joinBarcodes(dmpInputMongoEntity);

        // 扁平 SKU：一条 Mongo 同时作为 product + sku 明细（对齐极风/大卖写法）
        map.put("spuId", sku);
        map.put("spuNo", sku);
        map.put("spuName", StringUtils.defaultString(name));
        map.put("skuId", barcode);
        map.put("skuNo", sku);
        map.put("name", StringUtils.defaultString(name));
        map.put("status", status);
        map.put("sourcePlatform", OmsPlatformEnum.AI_YA.getCode());
        resultList.add(map);
        return resultList;
    }

    /**
     * 解析爱亚 {@code barcodeList}：元素为 {@code {unit, barcode}} 或纯字符串，逗号拼接后写入 skuId。
     * 方案文档 6.2.2 写 barcode，开放平台真实字段为 barcodeList（与 packagingList 同级）。
     */
    private String joinBarcodes(Map<String, Object> mongoData) {
        List<String> barcodeList = new ArrayList<>();
        Object barcodeListObj = mongoData.get("barcodeList");
        if (barcodeListObj instanceof List) {
            for (Object entry : (List<?>) barcodeListObj) {
                String barcode;
                if (entry instanceof Map) {
                    barcode = toStr(((Map<?, ?>) entry).get("barcode"));
                } else {
                    barcode = toStr(entry);
                }
                if (StringUtils.isNotBlank(barcode)) {
                    barcodeList.add(barcode);
                }
            }
        }
        return barcodeList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
    }

    /** 空安全转字符串。 */
    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
