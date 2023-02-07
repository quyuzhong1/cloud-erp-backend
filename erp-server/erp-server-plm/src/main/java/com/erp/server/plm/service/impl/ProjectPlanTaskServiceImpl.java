package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.server.plm.mapper.ProjectPlanTaskMapper;
import com.erp.server.plm.service.ProjectPlanTaskService;
import org.springframework.stereotype.Service;

/**
 * 项目计划任务表(ProjectPlanTask)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:05:42
 */
@Service
public class ProjectPlanTaskServiceImpl extends ServiceImpl<ProjectPlanTaskMapper, ProjectPlanTaskEntity> implements ProjectPlanTaskService {


    /**
     * 根据条件获取到项目计划任务
     * @author yl
     * @date 2023-02-03 15:54
     * @param dto
     * @return com.erp.model.plm.vo.ProductItemScheduleVO
     */
    @Override
    public ProductItemScheduleVO getTaskList(ProjectPlanTaskConditionDTO dto) {
        return null;
    }


    /**
     * 导出
     */
    @Override
    public void export() {

    }

    
    /**
     * 导入
     * @author yl
     * @date 2023-02-03 18:37
     * @param
     * @return java.lang.Boolean
     */
    @Override
    public Boolean importTaskschedule() {
        return null;
    }
}
