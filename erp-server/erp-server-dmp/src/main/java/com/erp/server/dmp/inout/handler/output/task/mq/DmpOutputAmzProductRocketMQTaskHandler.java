package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@Service
@Scope("prototype")
public class DmpOutputAmzProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {


    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        //  PlatformProductDTO
        Map<String , DmpProductInfoEntity> dmpProductInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSkuInfoEntity>> dmpSkuInfoEntityMap = new HashMap<>();
        for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if(CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if("dmp_product_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        DmpProductInfoEntity dmpProductInfoEntity = (DmpProductInfoEntity) v;
                        dmpProductInfoEntityMap.put(dmpProductInfoEntity.getId(), dmpProductInfoEntity);
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
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for(String changId : changeIds) {
            DmpProductInfoEntity dmpProductInfoEntity = dmpProductInfoEntityMap.get(changId);
            List<DmpSkuInfoEntity> dmpSkuInfoEntityList = dmpSkuInfoEntityMap.get(changId);
            if (CollectionUtils.isEmpty(dmpSkuInfoEntityList)){
                log.warn("亚马逊中台Listing数据解析异常: dmpProductInfoEntity={}, skuEntityList={}", JSONUtil.toJsonStr(dmpProductInfoEntity), JSONUtil.toJsonStr(dmpSkuInfoEntityList));
                continue;
            }
            for(DmpSkuInfoEntity dmpSkuInfoEntity : dmpSkuInfoEntityList) {
                PlatformProductDTO product = this.convert(dmpProductInfoEntity, dmpSkuInfoEntity, cfgOutputId);
                if(product != null) {
                    map.put(dmpSkuInfoEntity.getId(), JSON.toJSONString(product));
                }
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformProductDTO convert(DmpProductInfoEntity dmpProductInfoEntity , DmpSkuInfoEntity dmpSkuInfoEntity , String cfgOutputId) {
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
        product.setPlatformSkuNo(org.apache.commons.lang.StringUtils.isBlank(skuNo)? "" : skuNo);

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

        return product;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("msku", "platformShopCode");
    }
}
