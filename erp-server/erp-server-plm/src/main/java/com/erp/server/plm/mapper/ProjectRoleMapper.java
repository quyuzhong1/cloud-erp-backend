package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductRoleDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Classname RoleMapper
 * @Description TODO
 * @Date 2022-10-09 19:49
 * @Created by yl
 */
@Mapper
public interface ProjectRoleMapper extends BaseMapper<ProjectRoleEntity> {

}
