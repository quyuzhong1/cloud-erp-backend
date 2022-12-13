package com.erp.server.dmp.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.PlatformApiTask
 */
@Mapper
public interface PlatformApiTaskMapper extends BaseMapper<PlatformApiTaskEntity> {

    List<JobTaskDTO> selectApiTask(@Param("pageNumber") Integer pageNumber,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("localTime") Long localTime);

    void updateTaskTypeState(@Param("jobTaskDTOList") List<JobTaskDTO> jobTaskDTOList);

    void batchInsert(@Param("taskEntityList") List<PlatformApiTaskEntity> taskEntityList);

}




