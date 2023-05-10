package com.erp.server.workflow.mapper;

import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Mapper
public interface ProcessManagementMapper extends BaseMapper<ProcessManagementEntity> {

    /**
     * 根据业务id和业务类型获取流程管理信息
     * @param businessId
     * @param businessKey
     * @param userId
     * @return
     */
    ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(@Param("businessId") String businessId, @Param("businessKey") String businessKey,@Param("userId")  String userId);
}
