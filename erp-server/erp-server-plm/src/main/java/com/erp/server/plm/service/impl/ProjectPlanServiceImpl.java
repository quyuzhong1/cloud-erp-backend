package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.server.plm.mapper.ProjectPlanMapper;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.stereotype.Service;

/**
 * 项目计划表(ProjectPlan)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@Service
public class ProjectPlanServiceImpl extends ServiceImpl<ProjectPlanMapper, ProjectPlanEntity> implements ProjectPlanService {


    
    /**
     * 提交项目计划
     * @author yl
     * @date 2023-02-03 17:08
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    public Boolean submitSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }

    /**
     * 取消排期
     * @author yl
     * @date 2023-02-03 17:35
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    public Boolean cancelSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }

    /**
     * 重启提交
     * @author yl
     * @date 2023-02-03 17:35
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    public Boolean restartSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }


    /**
     * 变更排期
     * @param dto
     * @return
     */
    @Override
    public Boolean changeSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }


}
