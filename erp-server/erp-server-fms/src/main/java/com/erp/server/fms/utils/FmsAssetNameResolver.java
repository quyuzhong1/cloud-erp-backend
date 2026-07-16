package com.erp.server.fms.utils;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.erp.model.fms.entity.AssetCardEntity;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.fms.service.AssetCardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产名称关联查询（不依赖明细冗余字段）
 */
@Slf4j
@Component
public class FmsAssetNameResolver {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private AssetCardService assetCardService;

    public String resolveAcceptDetailProductName(AssetAcceptDetailEntity detail) {
        if (detail == null) {
            return null;
        }
        if (StringUtils.isNotBlank(detail.getSkuNo())) {
            String moldName = resolveMoldNameByCode(detail.getSkuNo());
            if (StringUtils.isNotBlank(moldName)) {
                return moldName;
            }
        }
        if (StringUtils.isNotBlank(detail.getSkuId())) {
            Map<String, String> productNameMap = batchResolveProductNameBySkuIds(Collections.singletonList(detail.getSkuId()));
            String productName = productNameMap.get(detail.getSkuId());
            if (StringUtils.isNotBlank(productName)) {
                return productName;
            }
        }
        return detail.getSkuNo();
    }

    public String resolveCardNameById(String cardId, String assetCode) {
        if (StringUtils.isBlank(cardId)) {
            return resolveMoldNameByCode(assetCode);
        }
        AssetCardEntity card = assetCardService.getById(cardId);
        return resolveCardName(card, assetCode);
    }

    public String resolveCardName(AssetCardEntity card, String assetCode) {
        String code = StringUtils.isNotBlank(assetCode) ? assetCode : (card != null ? card.getAssetCode() : null);
        String moldName = resolveMoldNameByCode(code);
        if (StringUtils.isNotBlank(moldName)) {
            return moldName;
        }
        return card != null ? card.getName() : null;
    }

    public Map<String, String> batchResolveCardNameByIds(Collection<String> cardIds) {
        if (CollUtil.isEmpty(cardIds)) {
            return Collections.emptyMap();
        }
        List<AssetCardEntity> cardList = assetCardService.listByIds(cardIds);
        if (CollUtil.isEmpty(cardList)) {
            return Collections.emptyMap();
        }
        List<String> assetCodes = cardList.stream()
                .map(AssetCardEntity::getAssetCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, MoldInfoEntity> moldInfoMap = batchResolveMoldInfo(assetCodes);
        Map<String, String> result = new HashMap<>();
        for (AssetCardEntity card : cardList) {
            MoldInfoEntity moldInfo = moldInfoMap.get(card.getAssetCode());
            if (moldInfo != null && StringUtils.isNotBlank(moldInfo.getName())) {
                result.put(card.getId(), moldInfo.getName());
            } else {
                result.put(card.getId(), card.getName());
            }
        }
        return result;
    }

    public Map<String, String> batchResolveProductNameBySkuIds(Collection<String> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ProductDetailEntity> productList = plmTaskFeign.listByIds(skuIds.stream().distinct().collect(Collectors.toList()));
            if (CollUtil.isEmpty(productList)) {
                return Collections.emptyMap();
            }
            return productList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getId()) && StringUtils.isNotBlank(p.getName()))
                    .collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName, (v1, v2) -> v1));
        } catch (Exception e) {
            log.error("批量查询SKU产品名称失败，skuIds: {}", skuIds, e);
            return Collections.emptyMap();
        }
    }

    public String resolveMoldNameByCode(String assetCode) {
        if (StringUtils.isBlank(assetCode)) {
            return null;
        }
        Map<String, MoldInfoEntity> moldInfoMap = batchResolveMoldInfo(Collections.singletonList(assetCode));
        MoldInfoEntity moldInfo = moldInfoMap.get(assetCode);
        return moldInfo != null ? moldInfo.getName() : null;
    }

    private Map<String, MoldInfoEntity> batchResolveMoldInfo(Collection<String> moldCodes) {
        if (CollUtil.isEmpty(moldCodes)) {
            return Collections.emptyMap();
        }
        try {
            List<MoldInfoEntity> moldInfoList = plmTaskFeign.listMoldInfoByCodes(moldCodes.stream().distinct().collect(Collectors.toList()));
            if (CollUtil.isEmpty(moldInfoList)) {
                return Collections.emptyMap();
            }
            return moldInfoList.stream()
                    .filter(info -> StringUtils.isNotBlank(info.getCode()))
                    .collect(Collectors.toMap(MoldInfoEntity::getCode, info -> info, (v1, v2) -> v1));
        } catch (Exception e) {
            log.error("批量查询模具档案失败，moldCodes: {}", moldCodes, e);
            return Collections.emptyMap();
        }
    }
}
