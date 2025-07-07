package com.erp.server.sys.service;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;

import java.util.List;

/**
 * <p>
 * 用户-仓库权限 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
public interface AuthUserWarehouseService extends SuperService<AuthUserWarehouseEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-02-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AuthUserWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-02-27
    * @param dto
    * @return
    */
    Boolean update(AuthUserWarehouseDTO.UpdateDTO dto);



    /**
     * 根据用户获取仓库权限
     * @param userId
     * @return
     */
    List<SysUserDTO.WarehouseDTO> getWarehouseUserList(String userId);

    /**
     * 获取仓库授权sql
     * @return
     */
    String getWarehousePermissionSql(String warehouseTableField , String dynamicDataSource);

    /**
     * 更新用户仓库权限
     * @param uid
     * @param warehouseIdList
     * @param warehouseAuthType
     */
    void batchSaveOrUpdate(String uid, List<String> warehouseIdList, String warehouseAuthType);

    /**
     * 根据用户获取店铺列表
     * @param userIds
     * @return
     */
    List<SysUserDTO.WarehouseDTO> listWarehouseIdByUserIds(List<String> userIds);

    void addUserWarehouseAuth(AuthUserWarehouseDTO.AddUserWarehouseAuthDTO addUserWarehouseAuthDTO);
}
