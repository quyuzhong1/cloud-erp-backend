package com.erp.server.sys.service;
import com.common.business.service.SuperService;
import com.erp.model.sys.entity.SysRefererConfigEntity;

import java.util.List;

/**
 * <p>
 * 第三方系统配置 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-05-06
 */
public interface SysRefererConfigService extends SuperService<SysRefererConfigEntity> {

    /**
     * 根据App-Id和应用类型查询配置
     *
     * @param appId   应用ID
     * @param appType 应用类型
     * @return 配置列表
     */
    List<SysRefererConfigEntity> getByAppIdAndType(String appId, String appType);

    /**
     * 根据App-Id查询配置
     *
     * @param appId 应用ID
     * @return 配置列表
     */
    List<SysRefererConfigEntity> getByAppId(String appId);
}
