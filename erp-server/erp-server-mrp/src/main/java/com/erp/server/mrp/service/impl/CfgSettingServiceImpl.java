package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.mrp.entity.CfgSettingEntity;
import com.erp.server.mrp.mapper.CfgSettingMapper;
import com.erp.server.mrp.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-03
 */
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {

    @Override
    public CfgSettingEntity getCfgSetting(String code) {
        return getOne(Wrappers.<CfgSettingEntity>lambdaQuery().eq(CfgSettingEntity::getKey, code));
    }
}
