package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.CfgTimezoneEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 国家对应的时区配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-13
 */
public interface CfgTimezoneService extends SuperService<CfgTimezoneEntity> {

    /**
     * 查询所有并用country作为key转成map
     *
     * @author Jim
     * {@code @date:} 2024-03-13
     */
    Map<String, CfgTimezoneEntity> mapByCountry();


    /**
     * 查询所有
     *
     * @author Jim
     * {@code @date:} 2024-03-13
     */

    List<CfgTimezoneEntity> listAndCache();

    /**
     * 根据国家代号获取时区
     *
     * @author Jim
     * {@code @date:} 2024-03-13
     */

    CfgTimezoneEntity getAndCacheByCountry(String country);
}
