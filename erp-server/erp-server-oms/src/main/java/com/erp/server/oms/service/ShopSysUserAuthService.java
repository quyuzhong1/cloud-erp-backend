package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;

/**
 * <p>
 * 店铺权限设置表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-09-01
 */
public interface ShopSysUserAuthService extends SuperService<ShopSysUserAuthEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-09-01
    * @param dto
    * @return
    */
    String add(ShopSysUserAuthDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-09-01
    * @param dto
    * @return
    */
    Boolean update(ShopSysUserAuthDTO.UpdateDTO dto);

    /**
     * @description: 批量授权
     * @author Will
     * @date: 2023/9/1 10:59
     * @param dto
     * @return Boolean
     */
    Boolean batchAuth(ShopSysUserAuthDTO.BatchAuthDTO dto);
}
