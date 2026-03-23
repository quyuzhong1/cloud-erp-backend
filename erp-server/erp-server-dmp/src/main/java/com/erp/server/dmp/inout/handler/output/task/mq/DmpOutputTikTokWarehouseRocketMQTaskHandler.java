package com.erp.server.dmp.inout.handler.output.task.mq;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.model.dmp.enums.WdtWarehouseTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wangdian.dto.ErpWarehouseDto;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Scope("prototype")
public class DmpOutputTikTokWarehouseRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdWarehouseInfoEntity> dmpThirdWarehouseInfoEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_warehouse_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = (DmpThirdWarehouseInfoEntity) v;
                        dmpThirdWarehouseInfoEntityMap.put(dmpThirdWarehouseInfoEntity.getId(), dmpThirdWarehouseInfoEntity);
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
                if ("dmp_third_warehouse_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = dmpThirdWarehouseInfoEntityMap.get(changeId);
            ErpWarehouseDto warehouse = this.convert(dmpThirdWarehouseInfoEntity, cfgOutputId);
            if (warehouse != null) {
                map.put(dmpThirdWarehouseInfoEntity.getId(), JSON.toJSONString(warehouse));
            }
        }
        return map;
    }

    /**
     * TikTok 销售仓走普通仓口径，单独输出到 TikTok 专用 MQ。
     */
    public ErpWarehouseDto convert(DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpThirdWarehouseInfoEntity, cfgOutputId)) {
            return null;
        }
        String warehouseCode = dmpThirdWarehouseInfoEntity.getWarehouseCode();
        String authId = dmpThirdWarehouseInfoEntity.getAuthId();
        String warehouseId = StrUtil.format("{}:{}", authId, warehouseCode);
        ErpWarehouseDto warehouse = new ErpWarehouseDto();
        warehouse.setUniqueId(warehouseId);
        warehouse.setPlatform(PlatformDictEnum.TIK_TOK.getCode());
        warehouse.setDisabled(resolveDisabled(dmpThirdWarehouseInfoEntity));
        warehouse.setSysType(PlatformDictEnum.TIK_TOK.getCode());
        warehouse.setWarehouseId(warehouseId);
        warehouse.setType(WdtWarehouseTypeEnum.PLATFORM.getCode());
        warehouse.setSubType(StrUtil.EMPTY);
        warehouse.setCode(warehouseCode);
        warehouse.setName(dmpThirdWarehouseInfoEntity.getWarehouseName());
        warehouse.setAddress(dmpThirdWarehouseInfoEntity.getAddress());
        warehouse.setContacts(dmpThirdWarehouseInfoEntity.getContacts());
        warehouse.setTelNumber(dmpThirdWarehouseInfoEntity.getTelNumber());
        warehouse.setTelno(dmpThirdWarehouseInfoEntity.getTelno());
        warehouse.setZip(dmpThirdWarehouseInfoEntity.getZip());
        warehouse.setProvince(dmpThirdWarehouseInfoEntity.getProvince());
        warehouse.setCity(dmpThirdWarehouseInfoEntity.getCity());
        warehouse.setDistrict(dmpThirdWarehouseInfoEntity.getDistrict());
        warehouse.setCreated(LocalDateUtil.formatTime(dmpThirdWarehouseInfoEntity.getPlatformCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        warehouse.setModified(LocalDateUtil.formatTime(dmpThirdWarehouseInfoEntity.getPlatformUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
        warehouse.setRemark(dmpThirdWarehouseInfoEntity.getRemark());
        warehouse.setWarehouseList(dmpThirdWarehouseInfoEntity.getWarehouseList());
        return warehouse;
    }

    private Boolean resolveDisabled(DmpThirdWarehouseInfoEntity entity) {
        if (entity.getDisabled() != null) {
            return entity.getDisabled();
        }
        return !"1".equals(entity.getPlatformWarehouseStatus());
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("code");
    }
}
