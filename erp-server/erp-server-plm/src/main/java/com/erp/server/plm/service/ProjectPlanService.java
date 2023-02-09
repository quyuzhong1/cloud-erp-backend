package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;

import java.util.List;

/**
 * 项目计划表(ProjectPlan)表服务接口
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
public interface ProjectPlanService  extends IService<ProjectPlanEntity> {



    Boolean submitSchedule(HandleTaskScheduleDTO dto);

    Boolean cancelSchedule(String id);

    Boolean restartSchedule(String id);

    Boolean changeSchedule(List<ChangeTaskScheduleDTO> list);

    List<ProjectPlanEntity> getByIds(List<String> projectPlanIds);
}
