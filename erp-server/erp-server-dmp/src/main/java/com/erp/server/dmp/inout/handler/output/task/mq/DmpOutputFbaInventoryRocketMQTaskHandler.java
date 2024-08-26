package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaInventoryEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapping;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DmpOutputFbaInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpFbaInventoryEntity> dmpFbaInventoryEntityMap = new HashMap<>();

        // 店铺信息
        Set<String> shopIds = new HashSet<>();
        Map<String, ShopInfoEntity> shopMap = new HashMap<>();
        Set<String> sellerSkuList = new HashSet<>();
        // Sku映射信息Map<ShopId， Map<卖家sku, 映射信息>
        Map<String, Map<String, ListingInfoWithSkuMappingDTO>> listingInfoMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_fba_inventory".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpFbaInventoryEntity dmpEntity = (DmpFbaInventoryEntity) v;
                    dmpFbaInventoryEntityMap.put(dmpEntity.getId(), dmpEntity);
                    shopIds.add(dmpEntity.getNextLevelId());
                    sellerSkuList.add(dmpEntity.getMsku());
                }
            }
        }

        // 查询当前店铺信息
        if (!CollectionUtils.isEmpty(shopIds)) {
            shopMap = shopInfoFeign.listShopInfoByIds(new LinkedList<>(shopIds)).stream()
                    .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        }

        // 查询映射相关信息
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = ListingInfoParamDTO.initAmazon(shopIds, sellerSkuList);
            listingInfoMap = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getShopId,
                            Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo,
                                    Function.identity(),
                                    // 过期时间最新优先
                                    (existing, replacement) -> replacement.getExpireTime().isAfter(existing.getExpireTime()) ? replacement : existing
                            )));

        }


        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_fba_inventory".equals(storageName)) {
                for (BaseEntity v : value) {
                    changeIds.add(v.getId());
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpFbaInventoryEntity dmpEntity = dmpFbaInventoryEntityMap.get(changId);
            FbaInventoryEntity entity = this.convert(dmpEntity,
                    cfgOutputId,
                    shopMap.get(dmpEntity.getNextLevelId()),
                    listingInfoMap.getOrDefault(dmpEntity.getNextLevelId(), Collections.emptyMap()).get(dmpEntity.getMsku()));
            if (null != entity) {
                map.put(entity.getId(), JSON.toJSONString(entity));
            }
        }
        return map;
    }

    /**
     * DMP数据转换推送DTO
     **/
    public FbaInventoryEntity convert(DmpFbaInventoryEntity dmpEntity, String cfgOutputId, ShopInfoEntity shopInfoEntity, ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        FbaInventoryEntity dtoEntity = new FbaInventoryEntity();
        BeanUtils.copyProperties(dmpEntity, dtoEntity);
        dtoEntity.setSkuNo(null == listingInfoWithSkuMappingDTO ? "" : listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo());

        if (null != dmpEntity.getLastPlatformUpdateTime()){
            ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
            dtoEntity.setDataEndTime(dmpEntity.getLastPlatformUpdateTime().atOffset(zoneOffset));
        }
        dtoEntity.setWarehouseName(StringUtils.isBlank(shopInfoEntity.getWarehouseName()) ? "" : shopInfoEntity.getWarehouseName());
        dtoEntity.setWarehouseId(StringUtils.isBlank(shopInfoEntity.getWarehouseId()) ? "" : shopInfoEntity.getWarehouseId());
        return dtoEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("msku", "marketplaceId", "platformShopCode");
    }
}
