package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.entity.SysRoleEntity;

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
}

