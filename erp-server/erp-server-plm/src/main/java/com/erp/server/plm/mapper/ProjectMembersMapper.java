package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.MemberPagingDTO;
import com.erp.model.plm.dto.MemberPagingShowDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.TemplateMembersEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 项目成员表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectMembersMapper extends BaseMapper<ProjectMembersEntity> {

    IPage<MemberPagingShowDTO> paging(Page query, @Param("productId")String productId, @Param("roleIds") List<String> roleIds  );

    IPage<MemberPagingShowDTO> allPaging(Page query, @Param("productId") String productId);

    /**
     * 根据角色名称和模板id查询人员
     * @Author Luo_WG
     * @Date 2023/3/27 12:00
     * @param roles roles
     * @param productId productId
     * @return java.util.List<com.erp.model.plm.dto.MemberPagingShowDTO>
     **/
    List<MemberPagingShowDTO> listByRoleNames(@Param("roles") List<String> roles, @Param("productId") String productId, @Param("roleName") String roleName);
}
