package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformFbtShipmentDTO;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbtShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpFbtShipmentEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FBT货件MQ输出处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtShipmentRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, DmpFbtShipmentEntity> mainEntityMap = new HashMap<>();
        Map<String, List<DmpFbtShipmentDetailEntity>> detailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getConvertInputDmpBaseEntityListMaps().entrySet()) {
            String storageName = entry.getKey().getStorageName();
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            if ("dmp_fbt_shipment".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpFbtShipmentEntity mainEntity = (DmpFbtShipmentEntity) entity;
                    mainEntityMap.put(mainEntity.getId(), mainEntity);
                }
            } else if ("dmp_fbt_shipment_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    DmpFbtShipmentDetailEntity detailEntity = (DmpFbtShipmentDetailEntity) entity;
                    detailEntityMap.computeIfAbsent(detailEntity.getMainId(), key -> new ArrayList<>()).add(detailEntity);
                }
            }
        }

        Set<String> changedMainIds = new LinkedHashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            String storageName = entry.getKey().getStorageName();
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            if ("dmp_fbt_shipment".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changedMainIds.add(entity.getId());
                }
            } else if ("dmp_fbt_shipment_detail".equals(storageName)) {
                for (BaseEntity entity : value) {
                    changedMainIds.add(((DmpFbtShipmentDetailEntity) entity).getMainId());
                }
            }
        }

        Map<String, String> dataMap = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(changedMainIds)) {
            return dataMap;
        }
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String mainId : changedMainIds) {
            DmpFbtShipmentEntity mainEntity = mainEntityMap.get(mainId);
            if (mainEntity == null) {
                continue;
            }
            PlatformFbtShipmentDTO dto = convert(mainEntity, detailEntityMap.get(mainId), cfgOutputId);
            if (dto != null) {
                dataMap.put(dto.getUniqueId(), JSON.toJSONString(dto));
            }
        }
        return dataMap;
    }

    private PlatformFbtShipmentDTO convert(DmpFbtShipmentEntity mainEntity,
                                           List<DmpFbtShipmentDetailEntity> detailEntities,
                                           String cfgOutputId) {
        if (this.validateDataBlack(mainEntity, cfgOutputId)) {
            throw new ServiceException("校验参数出错");
        }
        PlatformFbtShipmentDTO dto = new PlatformFbtShipmentDTO();
        dto.setPlatform(mainEntity.getSourcePlatform());
        dto.setDmpSyncTaskId(mainEntity.getInputTaskId());
        dto.setUniqueId(mainEntity.getInboundOrderId());
        dto.setAuthId(mainEntity.getAuthId());
        dto.setShopId(mainEntity.getShopId());
        dto.setInboundOrderId(mainEntity.getInboundOrderId());
        dto.setShipmentName(mainEntity.getShipmentName());
        dto.setWarehouseCode(mainEntity.getPlatformWarehouseCode());
        dto.setWarehouseName(mainEntity.getPlatformWarehouseName());
        dto.setPlatformShipmentStatus(mainEntity.getPlatformShipmentStatus());
        dto.setUpdatedTime(mainEntity.getPlatformUpdateTime());
        dto.setCarrierList(parseCarrierList(mainEntity.getCarrierListJson()));
        dto.setReceivedBatches(parseReceivedBatches(mainEntity.getReceivedBatchesJson()));
        dto.setPlannedGoods(parsePlannedGoods(detailEntities));
        if (CollectionUtils.isEmpty(detailEntities)) {
            log.warn("FBT货件无明细数据: entity={}", JSONUtil.toJsonStr(mainEntity));
        }
        return dto;
    }

    private List<PlatformFbtShipmentDTO.CarrierDTO> parseCarrierList(String carrierListJson) {
        if (StringUtils.isBlank(carrierListJson)) {
            return Collections.emptyList();
        }
        List<PlatformFbtShipmentDTO.CarrierDTO> carrierList = JSON.parseArray(StringUtils.defaultString(carrierListJson), PlatformFbtShipmentDTO.CarrierDTO.class);
        return carrierList == null ? Collections.emptyList() : carrierList;
    }

    private List<PlatformFbtShipmentDTO.ReceivedBatchDTO> parseReceivedBatches(String receivedBatchesJson) {
        if (StringUtils.isBlank(receivedBatchesJson)) {
            return Collections.emptyList();
        }
        List<PlatformFbtShipmentDTO.ReceivedBatchDTO> receivedBatches =
                JSON.parseArray(StringUtils.defaultString(receivedBatchesJson), PlatformFbtShipmentDTO.ReceivedBatchDTO.class);
        return receivedBatches == null ? Collections.emptyList() : receivedBatches;
    }

    private List<PlatformFbtShipmentDTO.PlannedGoodDTO> parsePlannedGoods(List<DmpFbtShipmentDetailEntity> detailEntities) {
        if (CollectionUtils.isEmpty(detailEntities)) {
            return Collections.emptyList();
        }
        List<PlatformFbtShipmentDTO.PlannedGoodDTO> list = new ArrayList<>();
        for (DmpFbtShipmentDetailEntity detailEntity : detailEntities) {
            PlatformFbtShipmentDTO.PlannedGoodDTO dto = new PlatformFbtShipmentDTO.PlannedGoodDTO();
            dto.setGoodsId(detailEntity.getGoodsId());
            dto.setReferenceCode(detailEntity.getReferenceCode());
            dto.setName(detailEntity.getProductName());
            dto.setQuantity(detailEntity.getDeclareQty());
            dto.setSkuIds(parseSkuIds(detailEntity.getSkuIdsJson()));
            list.add(dto);
        }
        return list;
    }

    private List<String> parseSkuIds(String skuIdsJson) {
        if (StringUtils.isBlank(skuIdsJson)) {
            return Collections.emptyList();
        }
        List<String> skuIds = JSON.parseArray(skuIdsJson, String.class);
        return skuIds == null ? Collections.emptyList() : skuIds;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("inboundOrderId");
    }
}
