package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryAgeEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class AntuInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdInventoryEntity> dmpThirdInventoryEntityMap = new HashMap<>();
        Map<String, List<DmpThirdInventoryAgeEntity>> dmpDetailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_inventory".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdInventoryEntity dmpThirdInventoryEntity = (DmpThirdInventoryEntity) v;
                        dmpThirdInventoryEntityMap.put(dmpThirdInventoryEntity.getId(), dmpThirdInventoryEntity);
                    }
                } else if ("dmp_third_inventory_age".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdInventoryAgeEntity dmpDetailEntity = (DmpThirdInventoryAgeEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpThirdInventoryAgeEntity> list = dmpDetailEntityMap.get(mainId);
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
                if ("dmp_third_inventory".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_third_inventory_age".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdInventoryAgeEntity dmpDetailEntity = (DmpThirdInventoryAgeEntity) v;
                        changeIds.add(dmpDetailEntity.getMainId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpThirdInventoryEntity dmpThirdInventoryEntity = dmpThirdInventoryEntityMap.get(changId);
            List<DmpThirdInventoryAgeEntity> dmpDetailList = dmpDetailEntityMap.getOrDefault(changId, Collections.emptyList());
            PlatformInventoryDTO platformInventoryDTO = this.convert(dmpThirdInventoryEntity, cfgOutputId, dmpDetailList);
            if (platformInventoryDTO != null) {
                map.put(dmpThirdInventoryEntity.getId(), JSON.toJSONString(platformInventoryDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformInventoryDTO convert(DmpThirdInventoryEntity dmpThirdInventoryEntity, String cfgOutputId, List<DmpThirdInventoryAgeEntity> dmpDetailList) {
        if (this.validateDataBlack(dmpThirdInventoryEntity, cfgOutputId)) {
            return null;
        }
        PlatformInventoryDTO platformInventoryDTO = BeanUtil.copyProperties(dmpThirdInventoryEntity, PlatformInventoryDTO.class);
        String sourcePlatform = dmpThirdInventoryEntity.getSourcePlatform();
        platformInventoryDTO.setPlatform(sourcePlatform);
        platformInventoryDTO.setProvider(sourcePlatform);
        platformInventoryDTO.setProviderErpId(dmpThirdInventoryEntity.getAuthId());
        platformInventoryDTO.setDownloadTime(LocalDateTime.now());

        // 库龄信息
        List<PlatformInventoryDTO.PlatformInventoryAgeDTO> ageList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(dmpDetailList)){
            ageList = dmpDetailList.stream().map(this::convertAgeInfo).collect(Collectors.toList());
        }
        platformInventoryDTO.setAgeInfoList(ageList);
        return platformInventoryDTO;
    }

    /**
     * 明细转换
     */
    private PlatformInventoryDTO.PlatformInventoryAgeDTO  convertAgeInfo(DmpThirdInventoryAgeEntity ageEntity) {
        PlatformInventoryDTO.PlatformInventoryAgeDTO platformInventoryAgeDTO = new PlatformInventoryDTO.PlatformInventoryAgeDTO();
        platformInventoryAgeDTO.setInventoryQty(ageEntity.getInventoryQty());
        platformInventoryAgeDTO.setPullDate(ageEntity.getUpdateTime().toLocalDate());
        platformInventoryAgeDTO.setPutAwayDate(ageEntity.getPutAwayDate());
        return platformInventoryAgeDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformWarehouseCode", "productSku");
    }

}
