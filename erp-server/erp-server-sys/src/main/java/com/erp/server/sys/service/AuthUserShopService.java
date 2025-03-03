package com.erp.server.sys.service;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.AuthUserShopDTO;

import java.util.List;

/**
 * <p>
 * 用户-店铺权限 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
public interface AuthUserShopService extends SuperService<AuthUserShopEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-02-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AuthUserShopDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-02-27
    * @param dto
    * @return
    */
    Boolean update(AuthUserShopDTO.UpdateDTO dto);


    /**
     * 根据用户获取店铺权限
     * @param userId
     * @return
     */
    List<SysUserDTO.ShopDTO> getShopUserList(String userId);

    /**
     * 获取用户店铺权限sql
     * @return
     */
    String getShopPermissionSql(String shopTableField);

    /**
     * 同步权限数据
     */
    void initShopDataOmsToSys();
}
