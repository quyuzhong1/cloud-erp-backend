package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.SysUserPagingSearchDTO;
import com.erp.model.sys.dto.UserDTO;
import com.erp.model.sys.dto.UserManageDTO;
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
}
