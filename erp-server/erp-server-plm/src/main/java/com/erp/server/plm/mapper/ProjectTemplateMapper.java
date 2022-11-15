package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProjectTemplateDTO;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 项目模板信息 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectTemplateMapper extends BaseMapper<ProjectTemplateEntity> {

    List<StartItemSourceDTO> getStartItemSource(@Param("sourceType") Integer sourceType);
    /**
     * @description: 模板管理列表查询
     * @author Will
     * @date: 2022/11/11 12:19
     * @param query
     * @param params
     * @return IPage<ProjectTemplateEntity>
     */
    IPage<ProjectTemplateEntity> paging(Page query, @Param("params") ProjectTemplateDTO params);
}
