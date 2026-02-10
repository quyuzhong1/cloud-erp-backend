package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FBT产品MQ任务Handler
 * 将FBT商品DMP数据转换为PlatformProductDTO并发送到RocketMQ
 * 
 * 字段映射：
 * - platformSkuNo: 库存SKU (goods/id)
 * - platformSkuName: 库存产品名称 (goods/name)
 * - thirdBarcode: 三方仓商品条码 (goods/code)
 * - type: WAREHOUSE (仓库类型)
 *
 * @author System
 * @since 2026-02-10
 */
@Service
@Scope("prototype")
public class FbtProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpProductInfoEntity> dmpProductInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSkuInfoEntity>> dmpSkuInfoEntityMap = new HashMap<>();
        
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_product_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpProductInfoEntity dmpProductInfoEntity = (DmpProductInfoEntity) v;
                        dmpProductInfoEntityMap.put(dmpProductInfoEntity.getId(), dmpProductInfoEntity);
                    }
                } else if ("dmp_sku_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSkuInfoEntity dmpSkuInfoEntity = (DmpSkuInfoEntity) v;
                        String mainId = dmpSkuInfoEntity.getMainId();
                        List<DmpSkuInfoEntity> list = dmpSkuInfoEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSkuInfoEntity);
                        dmpSkuInfoEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_product_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_sku_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSkuInfoEntity dmpSkuInfoEntity = (DmpSkuInfoEntity) v;
                        changeIds.add(dmpSkuInfoEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpProductInfoEntity dmpProductInfoEntity = dmpProductInfoEntityMap.get(changeId);
            List<DmpSkuInfoEntity> dmpSkuInfoEntityList = dmpSkuInfoEntityMap.get(changeId);
            if (CollUtil.isEmpty(dmpSkuInfoEntityList)) {
                continue;
            }
            for (DmpSkuInfoEntity dmpSkuInfoEntity : dmpSkuInfoEntityList) {
                PlatformProductDTO product = this.convert(dmpProductInfoEntity, dmpSkuInfoEntity, cfgOutputId);
                if (product != null) {
                    map.put(dmpSkuInfoEntity.getId(), JSON.toJSONString(product));
                }
            }
        }
        return map;
    }

    /**
     * 将FBT商品数据转换为PlatformProductDTO
     * 
     * 字段对应关系（根据图片）：
     * - 库存SKU -> goods/id -> platformSkuNo
     * - 库存产品名称 -> goods/name -> platformSkuName
     * - 三方仓商品条码 -> goods/code -> thirdBarcode
     * - 仓库 -> 中台配置绑定的FBT仓库名称
     * - 服务商 -> FBT仓
     */
    public PlatformProductDTO convert(DmpProductInfoEntity dmpProductInfoEntity, DmpSkuInfoEntity dmpSkuInfoEntity, String cfgOutputId) {
        if (dmpProductInfoEntity == null) {
            return null;
        }
        if (this.validateDataBlack(dmpSkuInfoEntity, cfgOutputId)) {
            return null;
        }

        PlatformProductDTO product = new PlatformProductDTO();

        // 平台代码 - FBT作为仓库，使用TikTok平台代码
        product.setPlatform(PlatformDictEnum.TIK_TOK.getCode());
        
        // 库存SKU (goods/id) -> 通过DMP配置映射到skuId字段
        String skuId = dmpSkuInfoEntity.getSkuId();
        product.setPlatformSkuNo(StringUtils.isBlank(skuId) ? "" : skuId);
        product.setPlatformSkuId(skuId);
        
        // 库存产品名称 (goods/name) -> 通过DMP配置映射到name字段
        String skuName = dmpSkuInfoEntity.getName();
        if (StringUtils.isNotBlank(skuName)) {
            product.setPlatformSkuName(skuName);
        } else {
            product.setPlatformSkuName(dmpProductInfoEntity.getSpuName());
        }
        
        // SPU信息
        product.setPlatformProductNo(dmpSkuInfoEntity.getSpuId());
        product.setPlatformProductName(dmpProductInfoEntity.getSpuName());

        // 三方仓商品条码 (goods/code) -> 通过DMP配置映射到skuNo字段 -> platformProductBarcode映射到ListingInfoEntity.thirdBarcode
        String referenceCode = dmpSkuInfoEntity.getSkuNo();
        if (StringUtils.isNotBlank(referenceCode)) {
            product.setPlatformProductBarcode(referenceCode);
        }

        // 类型：WAREHOUSE (仓库类型，区别于平台类型)
        product.setType(RuleTypeEnum.WAREHOUSE.getCode());
        product.setPlatformType("warehouse");

        // 图片URL
        String imageUrls = dmpSkuInfoEntity.getImageUrls();
        if (StringUtils.isBlank(imageUrls)) {
            product.setProductImageUrl("");
        } else {
            String imageUrl = imageUrls.split(";")[0];
            product.setProductImageUrl(imageUrl);
        }

        // 授权ID（对应overseas_provider表的ID）
        String authId = dmpProductInfoEntity.getAuthId();
        if (StringUtils.isNotBlank(authId)) {
            product.setAuthId(authId);
        }
        
        // 店铺ID（关联的TikTok店铺）
        product.setShopId(dmpProductInfoEntity.getNextLevelId());

        // 包装信息
        String packing = StrUtil.format("长度:{}{};宽度:{}{};高度:{}{};重量:{}{};",
                dmpSkuInfoEntity.getPackageLength(), dmpSkuInfoEntity.getPackageUnit(),
                dmpSkuInfoEntity.getPackageWidth(), dmpSkuInfoEntity.getPackageUnit(),
                dmpSkuInfoEntity.getPackageHeight(), dmpSkuInfoEntity.getPackageUnit(),
                dmpSkuInfoEntity.getGrossWeight(), dmpSkuInfoEntity.getWeightUnit());
        product.setProductPacking(packing);

        // 更新时间
        product.setPlatformUpdateTime(dmpSkuInfoEntity.getPlatformUpdateTime());

        // 平台唯一标识 = 库存SKU(goods/id) + 授权ID (FBT仓)
        String uniqueId = StrUtil.format("{}_{}", 
                StringUtils.isNotBlank(skuId) ? skuId : referenceCode, 
                authId);
        product.setUniqueId(uniqueId);

        return product;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformSkuNo", "thirdBarcode");
    }
}
