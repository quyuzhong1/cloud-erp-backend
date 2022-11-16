package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.TemplateRoleShowDTO;
import com.erp.model.plm.entity.TemplateRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * @Entity entity..TemplateRole
 */
@Mapper
public interface TemplateRoleMapper extends BaseMapper<TemplateRoleEntity> {
    /**
     * @description: 角色成员列表查询
     * @author Will
     * @date: 2022/11/15 12:14
     * @param query
     * @param params
     * @return IPage<TemplateRoleShowDTO>
     */
    IPage<TemplateRoleShowDTO> paging(Page query, @Param("params") BaseSearchDTO params);
}




