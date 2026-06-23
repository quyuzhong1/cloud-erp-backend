package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaInventoryEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.DmpFbaInventoryService;
import com.erp.server.dmp.service.DmpProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Scope("prototype")
public class DmpOutputAmzProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private DmpFbaInventoryService dmpFbaInventoryService;
    @Resource
    private DmpProductInfoService dmpProductInfoService;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        //  PlatformProductDTO
        Map<String , DmpProductInfoEntity> dmpProductInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSkuInfoEntity>> dmpSkuInfoEntityMap = new HashMap<>();

        List<String> mskuList = new LinkedList<>();
        Set<String> shopIdList = new HashSet<>();

        for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if(CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if("dmp_product_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        DmpProductInfoEntity dmpProductInfoEntity = (DmpProductInfoEntity) v;
                        dmpProductInfoEntityMap.put(dmpProductInfoEntity.getId(), dmpProductInfoEntity);
                        shopIdList.add(dmpProductInfoEntity.getAuthId());
                    }
                }else if("dmp_sku_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        DmpSkuInfoEntity skuEntity = (DmpSkuInfoEntity) v;
                        String mainId = skuEntity.getMainId();
                        List<DmpSkuInfoEntity> list = dmpSkuInfoEntityMap.get(mainId);
                        if(CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(skuEntity);
                        dmpSkuInfoEntityMap.put(mainId, list);
                        mskuList.add(skuEntity.getSkuNo());
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if(CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if("dmp_product_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }else if("dmp_sku_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        DmpSkuInfoEntity detailEntity = (DmpSkuInfoEntity) v;
                        changeIds.add(detailEntity.getMainId());
                    }
                }
            }
        }

        // 补充存在 changeIds，但不在 dmpProductInfoEntityMap 中的产品主数据（如仅明细变更触发、主表数据未随本次转换带入）。
        // 注意：此处仅回查产品主表，不回查 SKU 明细。业务场景为：仅主表变更、明细未变更时不推送——
        // 因此当 dmpSkuInfoEntityMap 中没有该 changeId 的明细时，下方循环会跳过，不推送该 Listing。
        List<String> missProductMainIds = changeIds.stream().filter(e -> !dmpProductInfoEntityMap.containsKey(e)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(missProductMainIds)) {
            List<DmpProductInfoEntity> missProductInfoEntities = dmpProductInfoService.listByIds(missProductMainIds);
            if (CollectionUtils.isNotEmpty(missProductInfoEntities)) {
                for (DmpProductInfoEntity dmpProductInfoEntity : missProductInfoEntities) {
                    dmpProductInfoEntityMap.put(dmpProductInfoEntity.getId(), dmpProductInfoEntity);
                    shopIdList.add(dmpProductInfoEntity.getAuthId());
                }
            }
        }

        // 库存信息
        List<DmpFbaInventoryEntity> dmpFbaInventoryEntityList = new LinkedList<>();
        if (!CollectionUtils.isEmpty(mskuList) && !CollectionUtils.isEmpty(shopIdList)){
            List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);
            if (CollectionUtils.isEmpty(shopList)){
                ServiceException.runError("解析亚马逊Listing数据异常:找不到店铺ID:{}", JSONUtil.toJsonStr(shopIdList));
            }
            List<String> shopCodeList = shopList.stream().map(ShopInfoEntity::getPlatformShopCode).distinct().collect(Collectors.toList());
            dmpFbaInventoryEntityList = dmpFbaInventoryService.lambdaQuery()
                    .in(DmpFbaInventoryEntity::getMsku, mskuList)
                    .in(DmpFbaInventoryEntity::getPlatformShopCode, shopCodeList)
                    .list();
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for(String changId : changeIds) {
            DmpProductInfoEntity dmpProductInfoEntity = dmpProductInfoEntityMap.get(changId);
            List<DmpSkuInfoEntity> dmpSkuInfoEntityList = dmpSkuInfoEntityMap.get(changId);
            // 产品主数据缺失：异常情况，跳过避免下方转换空指针
            if (dmpProductInfoEntity == null){
                log.warn("亚马逊中台Listing产品主数据缺失,跳过推送: mainId={}", changId);
                continue;
            }
            // 明细(SKU)未变更：业务上仅主表变更不推送
            if (CollectionUtils.isEmpty(dmpSkuInfoEntityList)){
                log.info("亚马逊中台Listing仅主表变更,明细未变更,跳过推送: mainId={}", changId);
                continue;
            }
            for(DmpSkuInfoEntity dmpSkuInfoEntity : dmpSkuInfoEntityList) {
                PlatformProductDTO product = this.convert(dmpProductInfoEntity, dmpSkuInfoEntity, cfgOutputId, dmpFbaInventoryEntityList);
                if(product != null) {
                    map.put(dmpSkuInfoEntity.getId(), JSON.toJSONString(product));
                }
            }
        }
        return map;
    }

    /**
     * 解析 Listing 产品数据
     **/
    public PlatformProductDTO convert(DmpProductInfoEntity dmpProductInfoEntity , DmpSkuInfoEntity dmpSkuInfoEntity , String cfgOutputId, List<DmpFbaInventoryEntity> dmpFbaInventoryEntityList) {
        if(this.validateDataBlack(dmpSkuInfoEntity, cfgOutputId)) {
            return null;
        }
        PlatformProductDTO product = new PlatformProductDTO();

        product.setPlatform(dmpProductInfoEntity.getSourcePlatform());
        // 平台sku no
        product.setPlatformProductNo(dmpSkuInfoEntity.getSpuId());
        // 平台sku 名
        String spuName = dmpProductInfoEntity.getSpuName();
        product.setPlatformProductName(spuName);
        String skuNo = dmpSkuInfoEntity.getSkuNo();
        product.setPlatformSkuNo(StringUtils.isBlank(skuNo)? "" : skuNo);
        product.setProductSpec(dmpSkuInfoEntity.getProdcutProperty());

        product.setPlatformSkuName(spuName);
        // 类型 platform 平台  warehouse 仓库
        product.setPlatformType("platform");
        String imageUrls = dmpSkuInfoEntity.getImageUrls();
        if (StringUtils.isBlank(imageUrls)) {
            product.setProductImageUrl("");
        } else {
            String imageUrl = imageUrls.split(";")[0];
            product.setProductImageUrl(imageUrl);
        }
        product.setShopId(dmpProductInfoEntity.getNextLevelId());
        // 包装信息
        String packing = "";
        if (null != dmpSkuInfoEntity.getPackageLength()
                || null != dmpSkuInfoEntity.getPackageWidth()
                || null != dmpSkuInfoEntity.getPackageHeight()
                || null != dmpSkuInfoEntity.getGrossWeight()
        ) {
            packing = StrUtil.format("长度:{}cm;宽度:{}cm;高度:{}cm;重量:{}kg;", dmpSkuInfoEntity.getPackageLength(), dmpSkuInfoEntity.getPackageWidth(), dmpSkuInfoEntity.getPackageHeight(), dmpSkuInfoEntity.getGrossWeight());
        }
        product.setProductPacking(packing);
        product.setPlatformUpdateTime(dmpSkuInfoEntity.getPlatformUpdateTime());
        product.setPlatformSkuId(dmpSkuInfoEntity.getSkuId());

        // 平台唯一标识=平台skuId + 店铺ID
        String uniqueId = StrUtil.format("{}_{}", dmpSkuInfoEntity.getSkuId(), dmpProductInfoEntity.getNextLevelId());
        product.setUniqueId(uniqueId);

        // fnsku
        DmpFbaInventoryEntity dmpFbaInventoryEntity = dmpFbaInventoryEntityList
                .stream()
                .filter(e -> Objects.equals(e.getMsku(), dmpSkuInfoEntity.getSkuNo()))
                .findFirst()
                .orElse(null);
        if (null != dmpFbaInventoryEntity){
            product.setPlatformFnSku(dmpFbaInventoryEntity.getFnSku());
        }

        //父平台产品ID（父ASIN）
        product.setPlatformParentSpuNo(dmpSkuInfoEntity.getPlatformParentSpuNo());
        //平台的Listing状态
        product.setPlatformStatus(dmpSkuInfoEntity.getStatus());
        return product;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
