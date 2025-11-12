package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 任务节点记录表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
@Mapper
public interface WorkflowTaskRecordMapper extends BaseMapper<WorkflowTaskRecordEntity> {

    List<WorkflowTaskRecordEntity> listErrorTask(@Param("id") String id );

    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport();
}
