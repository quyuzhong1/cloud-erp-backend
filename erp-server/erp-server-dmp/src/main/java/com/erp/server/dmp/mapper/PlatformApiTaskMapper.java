package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.common.business.dto.JobTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.PlatformApiTask
 */
@Mapper
public interface PlatformApiTaskMapper extends BaseMapper<PlatformApiTaskEntity> {

    List<JobTaskDTO> selectApiTask(@Param("localTime") LocalDateTime localTime,@Param("operateType") String operateType);

    void updateTaskTypeState(@Param("jobTaskDTOList") List<JobTaskDTO> jobTaskDTOList, @Param("status") Integer state);

    void batchInsert(@Param("taskEntityList") List<PlatformApiTaskEntity> taskEntityList);

}




