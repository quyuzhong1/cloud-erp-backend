package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.sys.dto.CustomizeFieldDisplayDTO;

import java.util.List;

/**
 * 项目计划任务表(ProjectPlanTask)表服务接口
 *
 * @author yl
 * @since 2023-02-03 15:05:42
 */
public interface ProjectPlanTaskService  extends IService<ProjectPlanTaskEntity> {


    /**
     * 获取任务列表
     * @param dto
     * @return
     */
    ProductItemScheduleVO getTaskList(ProjectPlanTaskConditionDTO dto);

    void export();

    Boolean importTaskschedule();

    Boolean fieldSet(List<CustomizeFieldDisplayDTO> dto);

    List<CustomizeFieldDisplayDTO> fieldShow();
}
