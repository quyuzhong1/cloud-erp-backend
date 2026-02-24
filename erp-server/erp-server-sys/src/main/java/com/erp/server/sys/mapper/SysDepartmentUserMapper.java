package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.common.business.dto.FindUserDTO;
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

 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Mapper
public interface SysDepartmentUserMapper  extends BaseMapper<SysDepartmentUserEntity> {

    IPage<SysDepartmentUserDTO> findDepartmentUser(Page query, @Param("params") DepartmentSearchDTO params, @Param("departmentIds") List<String> departmentIds);

    List<SysDepartmentUserNumberDTO> findUserNumber();

    SysDepartmentUserNumberDTO getDeptByUserId(@Param("userId") String userId);

    /**
     * 根据用户id查询部门信息（过滤已禁用部门，取最近绑定的部门）
     * @param userId 用户id
     * @return 部门信息
     */
    SysDepartmentUserNumberDTO getDeptByUserIdWithDisabledFilter(@Param("userId") String userId);

    /**
     * 根据用户id获取上级用户
     * @param userId
     * @return
     */
    List<UserSuperiorDTO> listSuperiorByUserId(@Param("userId") String userId);

    
    /**
     * 根据部门id获取部门员工信息
     * @author yl
     * @date 2023-06-05 12:08
     * @param deptId
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    List<FindUserDTO> listDeptUserByDeptId(@Param("deptId") String deptId);

    List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(@Param("userIdList")List<String> userIdList);
    /**
     * @description: 根据部门id集合查询
     * @author Will
     * @date: 2024/1/31 17:57
     * @param deptIdList
     * @return List<SysDepartmentUserNumberDTO>
     */
    List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(@Param("deptIdList") List<String> deptIdList);
}
