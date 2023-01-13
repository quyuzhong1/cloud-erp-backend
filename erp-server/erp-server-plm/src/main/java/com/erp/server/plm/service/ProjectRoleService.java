package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductRoleDTO;
import com.erp.model.plm.dto.ProjectRoleDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;

import java.util.List;

/**
 * @Classname ProjectRoleService
 * @Description TODO
 * @Date 2022-10-09 19:37
 * @Created by yl
 */
public interface ProjectRoleService extends IService<ProjectRoleEntity> {

    Boolean saveRole(ProjectRoleDTO dto);

    List<ProjectRoleEntity> listByProductId(String projectId);

    List<ProductRoleDTO> roleSortList();

    List<String> getRoleIdsByProductId(String productId);

    List<ProjectRoleEntity> listRoleByMemberIds(List<String> memberIds);

}
