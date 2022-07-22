package com.cloud.erp.admin.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.erp.admin.modules.sys.dto.SysUserPagingSearchDTO;
import com.cloud.erp.admin.modules.sys.entity.SysUserInfoEntity;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.modules.sys.dto.SysLoginIpDTO;
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


    List<SysUserVO> findList(@Param("searchKeyword") String q,@Param("roleId") String roleId);

    IPage<SysUserInfoEntity> paging(Page query, @Param("params") SysUserPagingSearchDTO params);

    List<SysUserVO> findRoleIfExistList(@Param("searchKeyword") String q,@Param("roleId") String roleId);

    List<SysUserVO> findPostIfExistList(@Param("searchKeyword") String q,@Param("postId") String postId);

    List<SysUserVO> findDepartmentIfExistList(@Param("searchKeyword") String searchKeyWord,@Param("departmentId") String flagId);

    void setLoginIp(@Param("params") SysLoginIpDTO dto);
}
