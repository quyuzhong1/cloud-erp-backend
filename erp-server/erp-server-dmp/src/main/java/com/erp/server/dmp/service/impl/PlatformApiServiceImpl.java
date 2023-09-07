package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.server.dmp.mapper.PlatformApiMapper;
import com.erp.server.dmp.service.PlatformApiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformApiServiceImpl extends SuperServiceImpl<PlatformApiMapper, PlatformApiEntity>
        implements PlatformApiService {

    @Override
    public List<PlatformApiEntity> listForDisabled(String dictPlatform) {
        return lambdaQuery().eq(PlatformApiEntity::getDictPlatform, dictPlatform)
                .eq(PlatformApiEntity::getDisabled, Boolean.FALSE)
                .list();
    }

    @Override
    public List<PlatformApiEntity> listByPlatform(String dictPlatform) {
        return lambdaQuery()
                .eq(PlatformApiEntity::getDictPlatform, dictPlatform)
                .list();
    }
}
