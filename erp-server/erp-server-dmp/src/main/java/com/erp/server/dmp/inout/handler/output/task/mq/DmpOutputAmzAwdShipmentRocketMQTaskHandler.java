package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformAwdShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpAwdShipmentEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpAwdShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpAwdShipmentEntity;
import com.erp.server.dmp.convert.DmpAwdShipmentConverter;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputAmzAwdShipmentRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpAwdShipmentEntity> dmpMainEntityMap = new HashMap<>();
        Map<String, List<DmpAwdShipmentDetailEntity>> dmpDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_awd_shipment".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpAwdShipmentEntity dmpMainEntity = (DmpAwdShipmentEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
                    }
                } else if ("dmp_awd_shipment_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpAwdShipmentDetailEntity dmpDetailEntity = (DmpAwdShipmentDetailEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpAwdShipmentDetailEntity> list = dmpDetailEntityMap.get(mainId);
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
                if ("dmp_awd_shipment".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_awd_shipment_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpAwdShipmentDetailEntity dmpDetailEntity = (DmpAwdShipmentDetailEntity) v;
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
            DmpAwdShipmentEntity dmpMainEntity = dmpMainEntityMap.get(changId);
            List<DmpAwdShipmentDetailEntity> dmpDetailEntityList = dmpDetailEntityMap.get(changId);
            PlatformAwdShipmentDTO dto = this.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
            if (null != dto){
                map.put(dto.getUniqueId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * FBA数据
     **/
    public PlatformAwdShipmentDTO convert(DmpAwdShipmentEntity dmpMainEntity, List<DmpAwdShipmentDetailEntity> dmpDetailEntityList, String cfgOutputId) {
        if (this.validateDataBlack(dmpMainEntity, cfgOutputId)) {
            ServiceException.runError("校验参数出错");
        }
        if (CollectionUtils.isEmpty(dmpDetailEntityList)){
//            ServiceException.runError("明细不能为空");
            log.warn("FBA货件,无明细数据:entity={}", JSONUtil.toJsonStr(dmpMainEntity));
            return null;
        }
        PlatformAwdShipmentDTO dto = DmpAwdShipmentConverter.INSTANCE.convertToPlatformAwdShipmentDTO(dmpMainEntity, dmpDetailEntityList);
        return dto;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("fbaShipmentId");
    }
}
