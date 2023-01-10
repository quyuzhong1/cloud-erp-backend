package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.SysRoleDTO;
import com.erp.model.sys.entity.SysRoleEntity;

import java.util.List;

/**
 * 角色表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysRoleService extends IService<SysRoleEntity> {



    /**
     * 根据角色id 删除 角色
     * @param id
     */
    void removeRoleById(List<String> id);


    void copyRole(String roleId);

    boolean saveRoleEntity(SysRoleEntity sysRole);
    /**
     * @description: 根据ids查询名称
     * @author Will
     * @date: 2023/1/9 11:37
     * @param roleIds
     * @return List<String>
     */
    List<String> listRoleByIds(List<String> roleIds);
    /**
     * @description: 根据用户ids查询角色
     * @author Will
     * @date: 2023/1/10 10:24
     * @param userIds
     * @return List<String>
     */
    List<SysRoleDTO> listRoleByUserIds(List<String> userIds);
}

