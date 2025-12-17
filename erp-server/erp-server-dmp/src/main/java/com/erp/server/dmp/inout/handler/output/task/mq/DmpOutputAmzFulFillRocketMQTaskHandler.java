package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformFulfillOrderDTO;
import com.common.business.dto.PlatformFulfillOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpPlatformSoDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpPlatformSoDeliveryEntity;
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
public class DmpOutputAmzFulFillRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpPlatformSoDeliveryEntity> dmpMainEntityMap = new HashMap<>();
        Map<String, List<DmpPlatformSoDeliveryDetailEntity>> dmpDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_platform_so_delivery".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpPlatformSoDeliveryEntity dmpMainEntity = (DmpPlatformSoDeliveryEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
                    }
                } else if ("dmp_platform_so_delivery_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpPlatformSoDeliveryDetailEntity dmpDetailEntity = (DmpPlatformSoDeliveryDetailEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpPlatformSoDeliveryDetailEntity> list = dmpDetailEntityMap.get(mainId);
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
                if ("dmp_platform_so_delivery".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_platform_so_delivery_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpPlatformSoDeliveryDetailEntity dmpDetailEntity = (DmpPlatformSoDeliveryDetailEntity) v;
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
            DmpPlatformSoDeliveryEntity dmpMainEntity = dmpMainEntityMap.get(changId);
            List<DmpPlatformSoDeliveryDetailEntity> dmpDetailEntityList = dmpDetailEntityMap.get(changId);
            PlatformFulfillOrderDTO dto = this.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
            if (null != dto){
                map.put(dto.getUniqueId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * FBA数据
     **/
    public PlatformFulfillOrderDTO convert(DmpPlatformSoDeliveryEntity dmpMainEntity, List<DmpPlatformSoDeliveryDetailEntity> dmpDetailEntityList, String cfgOutputId) {
        if (this.validateDataBlack(dmpMainEntity, cfgOutputId)) {
            ServiceException.runError("校验参数出错");
        }
        if (CollectionUtils.isEmpty(dmpDetailEntityList)){
//            ServiceException.runError("明细不能为空");
            log.warn("FBA货件,无明细数据:entity={}", JSONUtil.toJsonStr(dmpMainEntity));
            return null;
        }

        // 主表
        PlatformFulfillOrderDTO PlatformFulfillOrderDTO = new PlatformFulfillOrderDTO();
        BeanMapperUtils.copy(dmpMainEntity, PlatformFulfillOrderDTO);
        PlatformFulfillOrderDTO.setDmpSyncTaskId(dmpMainEntity.getInputTaskId());
        PlatformFulfillOrderDTO.setUniqueId(dmpMainEntity.getId());
        PlatformFulfillOrderDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        PlatformFulfillOrderDTO.setShopId(dmpMainEntity.getNextLevelId());
        // 明细
        List<PlatformFulfillOrderDetailDTO> detailDTOS = BeanMapperUtils.copyList(PlatformFulfillOrderDetailDTO.class,dmpDetailEntityList);
        PlatformFulfillOrderDTO.setDetailList(detailDTOS);
        return PlatformFulfillOrderDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("code");
    }
}
