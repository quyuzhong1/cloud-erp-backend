package com.erp.server.workflow.mapper;

import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
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
public interface ProcessTaskManagementMapper extends BaseMapper<ProcessTaskManagementEntity> {


    List<ProcessTaskManagementEntity> listByProcessInstanceId(@Param("ids") List<String> businessIds);

    /**
     * 查询任务前置节点任务列表
     * @param taskManagementId
     * @param processInstanceId
     * @return
     */
    List<ProcessTaskManagementEntity> listPreActivityTask(@Param("taskManagementId") String taskManagementId, @Param("processInstanceId") String processInstanceId);

    /**
     * 获取审核记录
     */
    List<ProcessTaskManagementDTO.ApproveHistoryDTO> listApproveHistory(@Param("businessId") String businessId);
}
