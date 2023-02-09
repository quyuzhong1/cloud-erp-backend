package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.model.sys.vo.UserFieldVO;

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

    Boolean fieldSet(List<CustomizeFieldHiddenDTO> dto);


    List<CustomizeFieldHiddenDTO> allField();

    UserFieldVO getUserField();


    List<ScheduleTaskVO> getScheduleTaskList(String productId,String status);

    void savePlanTask(String projectPlanId,String productId,List<ProjectTaskEntity> taskList);

    List<ScheduleTaskVO> getByTaskIds(String productId, List<String> taskIdList);

    List<ScheduleTaskVO> getPlanTaskByTaskIds(String productId, List<String> taskIdList);


    List<ProjectPlanTaskEntity> getByTaskIdList(String productId, List<String> taskIdList);

    Boolean cancelSchedule(HandleTaskScheduleDTO dto);

    List<ProjectPlanTaskEntity> getByProjectPlanId(String id);

    /**
     * 重启排期
     * @author yl
     * @date 2023-02-09 15:14
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean restartSchedule(HandleTaskScheduleDTO dto);

    /**
     * 变更排期
     * @author yl
     * @date 2023-02-09 15:32
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean changeSchedule(List<ChangeTaskScheduleDTO> dto);

    
    /**
     * 保存变更的任务
     * @author yl
     * @date 2023-02-09 16:00
     * @param id
     * @param productId
     * @param taskList
     * @param list
     * @return void
     */
    void saveChangePlanTask(String id, String productId, List<ProjectTaskEntity> taskList, List<ChangeTaskScheduleDTO> list);
}
