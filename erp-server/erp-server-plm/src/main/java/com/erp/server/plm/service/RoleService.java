package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.RoleDTO;
import com.erp.model.plm.entity.RoleEntity;

/**
 * @Classname ProjectRoleService
 * @Description TODO
 * @Date 2022-10-09 19:37
 * @Created by yl
 */
public interface RoleService extends IService<RoleEntity> {

    Boolean saveRole(RoleDTO dto);


}
