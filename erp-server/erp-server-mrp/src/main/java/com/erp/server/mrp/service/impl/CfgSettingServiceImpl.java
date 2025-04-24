package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.entity.CfgSettingEntity;
import com.erp.server.mrp.mapper.CfgSettingMapper;
import com.erp.server.mrp.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<CfgSettingDTO> listAllSetting() {
        List<CfgSettingEntity> list = list();
        return BeanMapperUtils.copyList(CfgSettingDTO.class, list);
    }
}
