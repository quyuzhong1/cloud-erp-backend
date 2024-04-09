package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.mapper.CfgSettingMapper;
import com.erp.server.dmp.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
}
