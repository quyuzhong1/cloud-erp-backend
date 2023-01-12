package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.PlatformEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 14:41
 */
public interface PlatformService extends IService<PlatformEntity> {
    /**
     * @description: 根据平台名称查询
     * @author Will
     * @date: 2023/1/12 9:18
     * @param name
     * @return PlatformEntity
     */
    PlatformEntity getByName(String name);
}
