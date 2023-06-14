package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.mapper.CfgSettingMapper;
import com.erp.server.dmp.service.CfgSettingService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

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
        List<CfgSettingEntity> list = lambdaQuery().in(CfgSettingEntity::getKey, keys).list();
        return list.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, CfgSettingEntity::getValue));
    }

    @Override
    public Map<SettingEnum, String> getMap(String type) {
        List<CfgSettingEntity> list = lambdaQuery().eq(CfgSettingEntity::getType, type).list();
        return list.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, CfgSettingEntity::getValue));
    }

    @Override
    public String getValue(SettingEnum key) {
        CfgSettingEntity entity = lambdaQuery().eq(CfgSettingEntity::getKey, key).last("LIMIT 1").one();
        if (null == entity) {
            return null;
        }
        return entity.getValue();
    }
}
