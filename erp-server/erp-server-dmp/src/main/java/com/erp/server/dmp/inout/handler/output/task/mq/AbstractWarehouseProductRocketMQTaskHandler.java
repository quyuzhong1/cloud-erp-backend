package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.utils.MD5Util;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 三方仓商品 Output 公共逻辑：dmp_product_info + dmp_sku_info → RocketMQ → PlatformListingConsumer。
 * <p>
 * 子类仅提供默认 {@code sourcePlatform}；{@code dmp_sku_info.status} 透传为
 * {@link PlatformProductDTO#setPlatformStatus(String)}，供消费端启停回收。
 */
public abstract class AbstractWarehouseProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    /**
     * 产品表未带 sourcePlatform 时的平台兜底编码（如 aiya / wego）。
     */
    protected abstract String defaultSourcePlatform();

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
                for (BaseEntity v : value) {
                    DmpProductInfoEntity product = (DmpProductInfoEntity) v;
                    dmpProductInfoEntityMap.put(product.getId(), product);
                }
            } else if ("dmp_sku_info".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpSkuInfoEntity sku = (DmpSkuInfoEntity) v;
                    dmpSkuInfoEntityMap.computeIfAbsent(sku.getMainId(), k -> new ArrayList<>()).add(sku);
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
                for (BaseEntity v : value) {
                    changeIds.add(v.getId());
                }
            } else if ("dmp_sku_info".equals(storageName)) {
                for (BaseEntity v : value) {
                    changeIds.add(((DmpSkuInfoEntity) v).getMainId());
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpProductInfoEntity productInfo = dmpProductInfoEntityMap.get(changeId);
            List<DmpSkuInfoEntity> skuList = dmpSkuInfoEntityMap.get(changeId);
            if (productInfo == null || CollUtil.isEmpty(skuList)) {
                continue;
            }
            for (DmpSkuInfoEntity sku : skuList) {
                PlatformProductDTO product = convert(productInfo, sku, cfgOutputId);
                if (product != null) {
                    map.put(sku.getId(), JSON.toJSONString(product));
                }
            }
        }
        return map;
    }

    /**
     * 组装推送 OMS 的仓库商品 DTO。
     * <ul>
     *   <li>{@code sku_id} → {@code platformProductBarcode}（条码串）</li>
     *   <li>{@code status} → {@code platformStatus}（爱亚原文 Active/Inactive；WEGO 已在 Input 归一）</li>
     *   <li>{@code uniqueId} = MD5(platform + skuNo)</li>
     * </ul>
     *
     * @param productInfo 产品主表
     * @param skuInfo     SKU 明细
     * @param cfgOutputId 输出配置 id（黑名单校验）
     * @return 推送报文；命中黑名单时返回 null
     */
    public PlatformProductDTO convert(DmpProductInfoEntity productInfo, DmpSkuInfoEntity skuInfo, String cfgOutputId) {
        if (this.validateDataBlack(skuInfo, cfgOutputId)) {
            return null;
        }
        PlatformProductDTO product = new PlatformProductDTO();
        product.setPlatformType("warehouse");
        String skuNo = skuInfo.getSkuNo();
        product.setPlatformSkuNo(skuNo);
        product.setPlatformSkuName(skuInfo.getName());
        product.setProductImageUrl(skuInfo.getImageUrls());
        product.setProductSpec(skuInfo.getCategoryName());
        product.setType(RuleTypeEnum.WAREHOUSE.getCode());
        product.setPlatformUpdateTime(skuInfo.getPlatformUpdateTime());
        product.setDownloadTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String sourcePlatform = StringUtils.defaultIfBlank(productInfo.getSourcePlatform(), defaultSourcePlatform());
        product.setUniqueId(MD5Util.toMD5(sourcePlatform + skuNo));
        product.setMatchResult(false);
        product.setPlatform(sourcePlatform);
        product.setAuthId(productInfo.getAuthId());
        product.setPlatformProductBarcode(skuInfo.getSkuId());
        product.setPlatformStatus(skuInfo.getStatus());
        return product;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformSkuNo");
    }
}
