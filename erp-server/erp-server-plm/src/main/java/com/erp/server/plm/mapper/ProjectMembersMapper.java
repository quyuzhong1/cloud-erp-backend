package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.MemberPagingDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
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

    IPage paging(Page query, @Param("productId")String productId,@Param("roleIds") List<String> roleIds  );

    IPage allPaging(Page query, @Param("productId") String productId);
}
