package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.enums.ListingInfoPlatformStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Magalu Listing MQ 推送 OMS 平台 SKU 对照表
 */
@Service
@Scope("prototype")
public class MagaluProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpProductInfoEntity> dmpProductInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSkuInfoEntity>> dmpSkuInfoEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_product_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpProductInfoEntity productInfo = (DmpProductInfoEntity) entity;
                    dmpProductInfoEntityMap.put(productInfo.getId(), productInfo);
                }
            } else if ("dmp_sku_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSkuInfoEntity skuInfo = (DmpSkuInfoEntity) entity;
                    List<DmpSkuInfoEntity> list = dmpSkuInfoEntityMap.computeIfAbsent(skuInfo.getMainId(), key -> new ArrayList<>());
                    list.add(skuInfo);
                }
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_product_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changeIds.add(entity.getId());
                }
            } else if ("dmp_sku_info".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpSkuInfoEntity skuInfo = (DmpSkuInfoEntity) entity;
                    changeIds.add(skuInfo.getMainId());
                }
            }
        }

        Map<String, String> resultMap = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpProductInfoEntity productInfo = dmpProductInfoEntityMap.get(changeId);
            List<DmpSkuInfoEntity> skuInfoList = dmpSkuInfoEntityMap.get(changeId);
            if (productInfo == null || CollUtil.isEmpty(skuInfoList)) {
                continue;
            }
            for (DmpSkuInfoEntity skuInfo : skuInfoList) {
                PlatformProductDTO product = convert(productInfo, skuInfo, cfgOutputId);
                if (product != null) {
                    resultMap.put(skuInfo.getId(), JSON.toJSONString(product));
                }
            }
        }
        return resultMap;
    }

    private PlatformProductDTO convert(DmpProductInfoEntity productInfo, DmpSkuInfoEntity skuInfo, String cfgOutputId) {
        if (productInfo == null || skuInfo == null) {
            return null;
        }
        if (validateDataBlack(skuInfo, cfgOutputId)) {
            return null;
        }
        PlatformProductDTO product = new PlatformProductDTO();
        product.setPlatform(MAGALU_PLATFORM);
        product.setPlatformProductNo(productInfo.getSpuId());
        product.setPlatformProductName(productInfo.getSpuName());
        product.setPlatformSkuNo(StringUtils.defaultString(skuInfo.getSkuNo()));
        product.setPlatformSkuName(StringUtils.defaultIfBlank(skuInfo.getName(), productInfo.getSpuName()));
        product.setPlatformType("platform");
        product.setShopId(productInfo.getNextLevelId());
        product.setPlatformSkuId(skuInfo.getSkuId());
        product.setPlatformUpdateTime(skuInfo.getPlatformUpdateTime());
        product.setProductSpec(StringUtils.defaultString(skuInfo.getCategoryName()));
        product.setPlatformStatus(mapPlatformStatus(skuInfo.getStatus()));
        product.setUniqueId(StrUtil.format("{}_{}", skuInfo.getSkuId(), productInfo.getNextLevelId()));

        String imageUrls = skuInfo.getImageUrls();
        if (StringUtils.isBlank(imageUrls)) {
            product.setProductImageUrl("");
        } else {
            product.setProductImageUrl(imageUrls.split(";")[0]);
        }
        product.setProductPacking(StrUtil.format("长度:{}{};宽度:{}{};高度:{}{};重量:{}{};",
                skuInfo.getPackageLength(), StringUtils.defaultString(skuInfo.getPackageUnit()),
                skuInfo.getPackageWidth(), StringUtils.defaultString(skuInfo.getPackageUnit()),
                skuInfo.getPackageHeight(), StringUtils.defaultString(skuInfo.getPackageUnit()),
                skuInfo.getGrossWeight(), StringUtils.defaultString(skuInfo.getWeightUnit())));
        return product;
    }

    private String mapPlatformStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return ListingInfoPlatformStatusEnum.INCOMPLETE.getCode();
        }
        switch (status) {
            case "1":
                return ListingInfoPlatformStatusEnum.ACTIVE.getCode();
            case "3":
                return ListingInfoPlatformStatusEnum.INACTIVE.getCode();
            case "7":
                return ListingInfoPlatformStatusEnum.DELETE.getCode();
            default:
                return ListingInfoPlatformStatusEnum.INCOMPLETE.getCode();
        }
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformSkuNo");
    }
}
