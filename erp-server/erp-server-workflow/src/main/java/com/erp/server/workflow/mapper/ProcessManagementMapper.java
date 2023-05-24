package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
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
public interface ProcessManagementMapper extends BaseMapper<ProcessManagementEntity> {

    /**
     * 根据业务id和业务类型获取流程管理信息
     * @param businessId
     * @param businessKey
     * @param userId
     * @return
     */
    ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(@Param("businessId") String businessId, @Param("businessKey") String businessKey,@Param("userId")  String userId);

    /**
     * 根据业务id和业务类型获取流程管理信息
     * @param businessId
     * @param businessKey
     * @return
     */
    List<ProcessManagementDTO.ManagementTaskDTO> listTaskByBusiness(@Param("businessId")String businessId, @Param("businessKey") String businessKey);

    /**
     * 分页查询
     * @param page
     * @param params
     * @return IPage<ProcessManagementDTO.PagingResultDTO>
     */
    IPage<ProcessManagementDTO.PagingResultDTO> paging(Page page, @Param("param") ProcessManagementDTO.SearchDTO param);

    /**
     * 导出
     * @param dto
     * @return
     */
    List<ProcessManagementDTO.PagingResultDTO> export(@Param("param") ProcessManagementDTO.ExportDTO dto);

    /**
     * 根据业务id和业务类型获取流程管理信息
     */
    List<ProcessManagementDTO.ManagementTaskDTO> listProcessTask(@Param("taskId") String taskId,@Param("timeoutStatus")String timeoutStatus);

    /**
     * 根据业务id和业务类型获取流程管理信息
     * @param ids
     * @return List<ProcessManagementDTO.ManagementTaskDTO>
     */
    List<ProcessManagementDTO.ManagementTaskDTO> listProcessTaskByIds(@Param("ids") List<String> ids);
}
