package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputAmzFbaShipmentRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpFbaShipmentEntity> dmpMainEntityMap = new HashMap<>();
        Map<String, List<DmpFbaShipmentDetailEntity>> dmpDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_fba_shipment".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpFbaShipmentEntity dmpMainEntity = (DmpFbaShipmentEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
                    }
                } else if ("dmp_fba_shipment_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpFbaShipmentDetailEntity dmpDetailEntity = (DmpFbaShipmentDetailEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpFbaShipmentDetailEntity> list = dmpDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpDetailEntity);
                        dmpDetailEntityMap.put(mainId, list);
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
                if ("dmp_fba_shipment".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_fba_shipment_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpFbaShipmentDetailEntity dmpDetailEntity = (DmpFbaShipmentDetailEntity) v;
                        changeIds.add(dmpDetailEntity.getMainId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        if (CollectionUtils.isEmpty(changeIds)){
            // 无变化
            return map;
        }

        for (String changId : changeIds) {
            DmpFbaShipmentEntity dmpMainEntity = dmpMainEntityMap.get(changId);
            List<DmpFbaShipmentDetailEntity> dmpDetailEntityList = dmpDetailEntityMap.get(changId);
            PlatformFbaShipmentDTO dto = this.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
            map.put(dto.getUniqueId(), JSON.toJSONString(dto));
        }
        return map;
    }

    /**
     * FBA数据
     **/
    public PlatformFbaShipmentDTO convert(DmpFbaShipmentEntity dmpMainEntity, List<DmpFbaShipmentDetailEntity> dmpDetailEntityList, String cfgOutputId) {
        if (this.validateDataBlack(dmpMainEntity, cfgOutputId)) {
            ServiceException.runError("校验白名出错");
        }
        if (CollectionUtils.isEmpty(dmpDetailEntityList)){
            ServiceException.runError("明细不能为空");
        }

        // 主表
        PlatformFbaShipmentDTO platformFbaShipmentDTO = new PlatformFbaShipmentDTO();
        platformFbaShipmentDTO.setName(dmpMainEntity.getName());
        platformFbaShipmentDTO.setCountryId(dmpMainEntity.getCountryId());
        platformFbaShipmentDTO.setFulfillmentCenter(dmpMainEntity.getFulfillmentCenter());
        platformFbaShipmentDTO.setPlatformShipmentStatus(dmpMainEntity.getPlatformShipmentStatus());
        platformFbaShipmentDTO.setLabelType(dmpMainEntity.getLabelType());
        platformFbaShipmentDTO.setFbaShipmentId(dmpMainEntity.getFbaShipmentId());
        platformFbaShipmentDTO.setDmpSyncTaskId(dmpMainEntity.getInputTaskId());
        platformFbaShipmentDTO.setUniqueId(dmpMainEntity.getFbaShipmentId());
        platformFbaShipmentDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        platformFbaShipmentDTO.setShopId(dmpMainEntity.getNextLevelId());
//        platformFbaShipmentDTO.setShopName();?
        platformFbaShipmentDTO.setPlatformUpdateTime(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        platformFbaShipmentDTO.setShipmentCreateTime(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        platformFbaShipmentDTO.setPackType(dmpMainEntity.getPackType());
        platformFbaShipmentDTO.setDeliveryFromAddress(dmpMainEntity.getDeliveryFromAddress());
        platformFbaShipmentDTO.setDeliveryStatus("unShipped");

        // 明细
        List<PlatformFbaShipmentReceiveDTO> dtoDetailList = new LinkedList<>();
        for (DmpFbaShipmentDetailEntity item : dmpDetailEntityList) {
            PlatformFbaShipmentReceiveDTO platformFbaShipmentReceiveDTO = new PlatformFbaShipmentReceiveDTO();
            platformFbaShipmentReceiveDTO.setSellerSku(item.getMsku());
            platformFbaShipmentReceiveDTO.setFnSku(item.getFnSku());
            platformFbaShipmentReceiveDTO.setDeclareQty(item.getDeclareQty());
            platformFbaShipmentReceiveDTO.setReceiveQty(item.getReceiveQty());
            platformFbaShipmentReceiveDTO.setFbaShipmentId(item.getFbaShipmentId());
            platformFbaShipmentReceiveDTO.setDeliveryQty(0);
            platformFbaShipmentReceiveDTO.setReceiveDate(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
            dtoDetailList.add(platformFbaShipmentReceiveDTO);
        }
        platformFbaShipmentDTO.setDetailList(dtoDetailList);

        return platformFbaShipmentDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("fbaShipmentId");
    }
}
