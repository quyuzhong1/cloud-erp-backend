package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.erp.model.sys.dto.DepartmentSearchDTO;
import com.erp.model.sys.dto.SysDepartmentUserDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname SysDepartmentUserMapper
 * @Description TODO
 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Mapper
public interface SysDepartmentUserMapper  extends BaseMapper<SysDepartmentUserEntity> {

    IPage<SysDepartmentUserDTO> findDepartmentUser(Page query, @Param("params") DepartmentSearchDTO params, @Param("departmentIds") List<String> departmentIds);

    List<SysDepartmentUserNumberDTO> findUserNumber();

    SysDepartmentUserNumberDTO getDeptByUserId(@Param("userId") String userId);

    /**
     * 根据用户id获取上级用户
     * @param userId
     * @return
     */
    List<UserSuperiorDTO> listSuperiorByUserId(@Param("userId") String userId);
}
