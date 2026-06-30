package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.oms.dto.WorkflowTaskInstanceDTO;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowTaskInstanceMapper extends BaseMapper<WorkflowTaskInstanceEntity> {

    IPage<WorkflowTaskInstanceDTO.ListDTO> paging(Page<?> page, @Param("params") WorkflowTaskInstanceDTO.PagingParamDTO params);

    WorkflowTaskInstanceDTO.ViewDTO viewHeader(@Param("id") String id);

    List<WorkflowTaskInstanceDTO.StepDTO> listSteps(@Param("instanceId") String instanceId);

    List<String> listBySource(@Param("params") WorkflowTaskInstanceDTO.ListBySourceParamDTO params);

    List<WorkflowTaskInstanceDTO.ViewDTO> listViewHeaders(@Param("instanceIds") List<String> instanceIds);

    List<WorkflowTaskInstanceDTO.StepDTO> listStepsByInstanceIds(@Param("instanceIds") List<String> instanceIds);

    List<WorkflowTaskInstanceDTO.ErrorReportDTO> errorReport(@Param("params") WorkflowTaskInstanceDTO.ErrorReportParamDTO params);

    List<WorkflowTaskInstanceDTO.TabListDTO> tabList(@Param("params") PermissionsDTO dto);
}
