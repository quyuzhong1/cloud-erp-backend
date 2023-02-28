package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.PlanTaskNameDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.vo.ScheduleChangeTaskVO;
import com.erp.model.plm.vo.ScheduleTaskDetailsVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname ProjectPlanTaskMapper
 * @Description TODO
 * @Date 2023-02-03 15:06
 * @Created by yl
 */
@Mapper
public interface ProjectPlanTaskMapper extends BaseMapper<ProjectPlanTaskEntity> {
    List<ScheduleTaskVO> getScheduleTaskList(@Param("productId") String productId,@Param("status") String status);

    List<ScheduleTaskVO> getByTaskIds(@Param("productId") String productId, @Param("taskIdList")List<String> taskIdList);

    List<ScheduleTaskDetailsVO> getTaskByPlanType(@Param("productId") String productId, @Param("projectPlanType") String projectPlanType);

    List<ScheduleChangeTaskVO> getChangeTaskList(@Param("productId") String productId ,@Param("taskName") String taskName,@Param("scheduleStatus") String scheduleStatus);

    List<PlanTaskNameDTO> listByPlanIds(@Param("planIdList") List<String> planIdList);
}
