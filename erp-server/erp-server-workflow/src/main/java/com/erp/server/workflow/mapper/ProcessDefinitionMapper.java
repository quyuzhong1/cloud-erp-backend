package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Mapper
public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinitionEntity> {

    /**
     * 分页查询流程定义
     * @param page
     * @param params
     * @return
     */
    IPage<ProcessDefinitionDTO.ListDTO> paging(Page<?> page, @Param("params") ProcessDefinitionDTO.QueryDTO params);

    /**
     * 查询流程定义
     * @param dto
     * @return
     */
    List<ProcessDefinitionDTO.ExportDTO> query(@Param("params") ProcessDefinitionDTO.QueryExportDTO dto);
    Page<ProcessDefinitionDTO.ExportDTO> query(@Param("page") Page<ProcessDefinitionDTO.ExportDTO> page, @Param("params") ProcessDefinitionDTO.QueryExportDTO dto);
}
