package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpAwdInventoryEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.entity.AwdInventoryEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2025/12/26 11:49
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Scope("prototype")
public class DmpOutputAwdInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpAwdInventoryEntity> dmpAwdInventoryEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_awd_inventory".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpAwdInventoryEntity dmpEntity = (DmpAwdInventoryEntity) v;
                    dmpAwdInventoryEntityMap.put(dmpEntity.getId(), dmpEntity);
                }
            }
        }


        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_awd_inventory".equals(storageName)) {
                for (BaseEntity v : value) {
                    changeIds.add(v.getId());
                }
            }
        }

        List<String> mskuIds = new ArrayList<>();
        String shopId = "";
        for (DmpAwdInventoryEntity value : dmpAwdInventoryEntityMap.values()) {
            if (StringUtils.isNotBlank(value.getMsku())) {
                mskuIds.add(value.getMsku());
                if (StringUtils.isBlank(shopId)) {
                    shopId = value.getNextLevelId();
                }
            }
        }

        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(mskuIds);
        listingInfoParamDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        listingInfoParamDTO.setShopIdList(Collections.singletonList(shopId));
        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpAwdInventoryEntity dmpEntity = dmpAwdInventoryEntityMap.get(changId);
            AwdInventoryEntity entity = this.convert(dmpEntity, cfgOutputId, mappingSkuViewDTOS);
            if (null != entity) {
                map.put(changId, JSON.toJSONString(entity));
            }
        }
        return map;
    }

    /**
     * DMP数据转换推送DTO
     **/
    public AwdInventoryEntity convert(DmpAwdInventoryEntity dmpEntity, String cfgOutputId,List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOS) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        AwdInventoryEntity dtoEntity = new AwdInventoryEntity();
        SkuMappingDTO.MappingSkuViewDTO mappingSkuViewDTO = mappingSkuViewDTOS.stream()
                .filter(item -> item.getPlatformSkuNo().equals(dmpEntity.getMsku()))
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(mappingSkuViewDTO)) {
            dtoEntity.setSkuId(StringUtils.isNotBlank(mappingSkuViewDTO.getProductSkuId()) ? mappingSkuViewDTO.getProductSkuId() : "");
            dtoEntity.setSkuNo(StringUtils.isNotBlank(mappingSkuViewDTO.getProductSkuNo()) ? mappingSkuViewDTO.getProductSkuNo() : "");
            dtoEntity.setProductName(StringUtils.isNotBlank(mappingSkuViewDTO.getProductName()) ? mappingSkuViewDTO.getProductName() : "");
        }
        dtoEntity.setMsku(dmpEntity.getMsku());

        dtoEntity.setReplenishmentQty(Objects.isNull(dmpEntity.getReplenishmentQty()) ? 0 : dmpEntity.getReplenishmentQty());
        dtoEntity.setReservedDistributableQty(Objects.isNull(dmpEntity.getReservedDistributableQty()) ? 0 : dmpEntity.getReservedDistributableQty());
        dtoEntity.setAvailableDistributableQty(Objects.isNull(dmpEntity.getAvailableDistributableQty()) ? 0 : dmpEntity.getAvailableDistributableQty());
        dtoEntity.setTotalInboundQty(Objects.isNull(dmpEntity.getTotalInboundQty()) ? 0 : dmpEntity.getTotalInboundQty());
        dtoEntity.setTotalOnhandQty(Objects.isNull(dmpEntity.getTotalOnhandQty()) ? 0 : dmpEntity.getTotalOnhandQty());
        //店铺管理-基础设置-AWD仓
        dtoEntity.setWarehouseName("");
        dtoEntity.setWarehouseId("");

        return dtoEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("msku");
    }
}