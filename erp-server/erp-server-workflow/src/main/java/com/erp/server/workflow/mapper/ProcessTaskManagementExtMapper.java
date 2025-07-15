package com.erp.server.workflow.mapper;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * process_task_management拓展表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Mapper
public interface ProcessTaskManagementExtMapper extends BaseMapper<ProcessTaskManagementExtEntity> {

    List<ProcessTaskManagementExtDTO.MessageDTO> listByProcessTaskManagementIds(@Param("processTaskManagementIds") List<String> processTaskManagementIds);

    List<ProcessTaskManagementEntity> listProcessTaskByTaskIds(@Param("processTaskManagementIds") List<String> processTaskManagementIds,@Param("processInstanceId") String processInstanceId);
}
