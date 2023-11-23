package com.erp.server.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.server.sys.mapper.ImlDictCityMapper;
import com.erp.server.sys.service.ImlDictCityService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.ap.internal.model.assignment.UpdateWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 艾姆勒城市字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-11-23
 */
@Slf4j
@Service
public class ImlDictCityServiceImpl extends SuperServiceImpl<ImlDictCityMapper, ImlDictCityEntity> implements ImlDictCityService {

    @Override
    public boolean saveOrUpdateByRegionId(ImlDictCityEntity entity) {
        LambdaUpdateWrapper<ImlDictCityEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ImlDictCityEntity::getRegionId, entity.getRegionId());
        return this.saveOrUpdate(entity,updateWrapper);
    }
}
