package com.erp.server.oms.service;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;

import java.util.List;

/**
 * <p>
 * 任务节点记录表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
public interface WorkflowTaskRecordService extends SuperService<WorkflowTaskRecordEntity> {


    List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto);

    List<WorkflowTaskRecordEntity> listErrorTask();

    void WorkflowTaskRecordRetryJob();

    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport();

    /**
     * 根据sourceId和sourceType删除任务记录
     * @param sourceId
     * @param sourceType
     */
    void removeBySourceIdAndSourceType(String sourceId, String sourceType);
}
