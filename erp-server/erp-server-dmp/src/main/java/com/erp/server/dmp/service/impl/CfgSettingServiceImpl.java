package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.mapper.CfgSettingMapper;
import com.erp.server.dmp.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务配置表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {


    @Override
    public Map<SettingEnum, String> getMap(List<SettingEnum> keys) {
        List<CfgSettingEntity> list = lambdaQuery()
                .in(CfgSettingEntity::getKey, keys)
                .eq(CfgSettingEntity::getStatus, Boolean.TRUE)
                .list();
        return list.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, CfgSettingEntity::getValue));
    }

    @Override
    public Map<SettingEnum, String> getMap(String type) {
        List<CfgSettingEntity> list = lambdaQuery()
                .eq(CfgSettingEntity::getType, type)
                .eq(CfgSettingEntity::getStatus, Boolean.TRUE)
                .list();
        return list.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, CfgSettingEntity::getValue));
    }

    @Override
    public String getValue(SettingEnum key) {
        CfgSettingEntity entity = lambdaQuery()
                .eq(CfgSettingEntity::getKey, key)
                .eq(CfgSettingEntity::getStatus, Boolean.TRUE)
                .last("LIMIT 1")
                .one();
        if (null == entity) {
            return null;
        }
        return entity.getValue();
    }

    @Override
    public Map<String, Integer> getApiTaskDelaySecond(SettingEnum settingEnum) {
        String value = this.getValue(settingEnum);
        if (StringUtils.isBlank(value)){
            return Collections.emptyMap();
        }
        return JSONUtil.toBean(value, Map.class);
    }

    @Override
    public List<CfgSettingDTO.WarehouseLocationSettingDTO> isPushKingdeeWarehouseLocation(List<String> warehouseIdList) {
        List<CfgSettingDTO.WarehouseLocationSettingDTO> resultList = new ArrayList<>();
        String value = getValue(SettingEnum.PUSH_KINGDEE_WAREHOUSE_LOCATION_LIST);
        //未配置则不推送金蝶
        if (StrUtil.isBlank(value)) {
            log.warn("未配置推送金蝶仓位的仓库信息");
            return new ArrayList<>();
        }
        List<String> pushWarehouseIdList = Arrays.stream(value.split(",")).distinct().collect(Collectors.toList());

        for (String warehouseId : warehouseIdList) {
            CfgSettingDTO.WarehouseLocationSettingDTO settingDTO = new CfgSettingDTO.WarehouseLocationSettingDTO();
            settingDTO.setWarehouseId(warehouseId);
            Boolean isPush = pushWarehouseIdList.contains(warehouseId) ? Boolean.TRUE : Boolean.FALSE;
            settingDTO.setIsPush(isPush);
            resultList.add(settingDTO);
        }
        return resultList;
    }
}
