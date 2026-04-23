package com.erp.server.sys.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.RedisDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;

import java.util.List;

/**
 * <p>
 * 用户-店铺权限 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
public interface ErpRedisService {
    /**
     * 将redis key转换到另一个key
     * @param sourcePrefix
     * @param targetPrefix
     * @return
     */
    Boolean copyKey(String sourcePrefix, String targetPrefix);
}
