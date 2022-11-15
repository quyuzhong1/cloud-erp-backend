package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TemplateMembersEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity entity..TemplateMembers
 */
@Mapper
public interface TemplateMembersMapper extends BaseMapper<TemplateMembersEntity> {
    /**
     * @description: 根据角色id和模板id查询
     * @author Will
     * @date: 2022/11/15 14:35
     * @param roleId
     * @param templateId
     * @return List<TemplateMembersEntity>
     */
    List<TemplateMembersEntity> getMembersByRoleIdAndTemplateId(@Param("roleId") String roleId,@Param("templateId") String templateId);
}




