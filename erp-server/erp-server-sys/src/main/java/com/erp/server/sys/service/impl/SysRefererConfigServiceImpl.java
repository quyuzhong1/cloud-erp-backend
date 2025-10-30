package com.erp.server.sys.service.impl;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.server.sys.mapper.SysRefererConfigMapper;
import com.erp.server.sys.service.SysRefererConfigService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 * 第三方系统配置 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-05-06
 */
@Slf4j
@Service
public class SysRefererConfigServiceImpl extends SuperServiceImpl<SysRefererConfigMapper, SysRefererConfigEntity> implements SysRefererConfigService {

    /**
     * 根据App-Id和应用类型查询配置
     *
     * @param appId   应用ID
     * @param appType 应用类型
     * @return 配置列表
     */
    @Override
    @Cacheable(value = "sysRefererConfig", key = "#appType + ':' + #appId ")
    public List<SysRefererConfigEntity> getByAppIdAndType(String appId, String appType) {
        log.info("查询应用配置，appId：{}，appType：{}", appId, appType);
        return this.lambdaQuery()
                .eq(SysRefererConfigEntity::getAppId, appId)
                .eq(SysRefererConfigEntity::getAppType, appType)
                .list();
    }

    /**
     * 根据App-Id查询配置
     *
     * @param appId 应用ID
     * @return 配置列表
     */
    @Override
    @Cacheable(value = "sysRefererConfig", key = "#appId")
    public List<SysRefererConfigEntity> getByAppId(String appId) {
        log.info("查询应用配置，appId：{}", appId);
        return this.lambdaQuery()
                .eq(SysRefererConfigEntity::getAppId, appId)
                .list();
    }
}
