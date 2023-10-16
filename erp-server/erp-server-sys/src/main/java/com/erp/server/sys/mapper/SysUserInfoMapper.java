package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
@Mapper
public interface SysUserInfoMapper extends BaseMapper<SysUserInfoEntity> {


    List<UserDTO> findList(@Param("searchKeyword") String q, @Param("roleId") String roleId);

    IPage<UserManageDTO> paging(Page query, @Param("params") SysUserPagingSearchDTO params, @Param("roleIds") List<String> roleIds);

    List<UserDTO> findRoleIfExistList(@Param("searchKeyword") String q, @Param("roleId") String roleId);

    List<UserDTO> findPostIfExistList(@Param("searchKeyword") String q, @Param("postId") String postId);

    List<UserDTO> findDepartmentIfExistList(@Param("searchKeyword") String searchKeyWord, @Param("departmentId") String flagId);

    void setLoginIp(@Param("params") SysLoginIpDTO dto);

    List<UserRequestPermissionsDTO> getRequestPermissionsList(@Param("userId") String userId);

    List<String> getDepUserList(@Param("deptList") List<String> deptList);

    List<String> getUserDepList(@Param("userId") String userId);

    List<SysUserDeptDTO> getUserDeptList();

    /**
     * 根据角色id获取用户列表
     * @param roleIds
     * @return
     */
    List<FindUserDTO> getListByRoleIds(List<String> roleIds);

    /**
     * 根据部门id查询用户
     * @Author Luo_WG
     * @Date 2023/7/20 15:54
     * @param deptIds
     * @return java.util.List<com.erp.model.sys.entity.SysUserInfoEntity>
     **/
    List<SysUserInfoEntity> listUserByDept(@Param("deptIds") List<String> deptIds);

    /**
     * 根据部门名称获取所有的子部门
     * @Author Luo_WG
     * @Date 2023/7/20 15:54
     * @param deptName
     * @return java.util.List<com.erp.model.sys.entity.SysUserInfoEntity>
     **/
    List<SysDepartmentTreeDTO> listSonDeptAll(@Param("deptName") String deptName);
    /**
     * @description:
     * @author Will
     * @date: 2023/9/4 12:23
     * @param query 
     * @param params 
     * @return IPage 
     */
    IPage<SysUserInfoDTO.ShopAuthPagingDTO> shopAuthPaging(Page query,@Param("params") SysUserInfoDTO.ShopAuthPagingSearchDTO params,@Param("userIdList") List<String> userIdList);


}
