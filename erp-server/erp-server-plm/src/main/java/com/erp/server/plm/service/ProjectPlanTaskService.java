package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    void exportExcel(ProjectPlanTaskConditionDTO dto, HttpServletResponse response);

    Boolean importTaskSchedule(MultipartFile excelFile,String productId, HttpServletResponse response);



    void savePlanTask(String projectPlanId,String productId,List<ProjectTaskEntity> taskList);

    List<ScheduleTaskVO> getByTaskIds(String productId, List<String> taskIdList);



    List<ProjectPlanTaskEntity> getByTaskIdList(String productId, List<String> taskIdList);

    Boolean cancelSchedule(HandleTaskScheduleDTO dto);

    List<ProjectPlanTaskEntity> getByProjectPlanIdList(List<String> projectPlanIds);

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
    Boolean changeSchedule(ChangeScheduleDTO dto);

    
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

    
    /**
     * 获取到类型的任务
     * @author yl
     * @date 2023-02-09 17:49
     * @param productId
     * @param projectPlanChange
     * @return java.util.List<com.erp.model.plm.entity.ProjectPlanEntity>
     */
    List<ScheduleTaskDetailsVO> getTaskByPlanType(String productId, String projectPlanChange);

    
    /**
     * 查询变更 排期的任务
     * @author yl
     * @date 2023-02-14 8:27
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.ScheduleChangeTaskVO>
     */

    List<ScheduleChangeTaskVO> getChangeTaskList(BaseIdDTO dto);

    
    /**
     * 导出排期变更
     * @author yl
     * @date 2023-02-14 10:39
     * @param dto
     * @param response
     * @return void
     */
    void exportChangeSchedule(HandleTaskScheduleDTO dto, HttpServletResponse response);

    
    /**
     * 导入数据
     * @author yl
     * @date 2023-02-14 11:25
     * @param excelFile
     * @param response
     * @return void
     */
    ChangeScheduleExportResultVO importChangeSchedule(MultipartFile excelFile, HttpServletResponse response,String  productId);

    /**
     * 导出任务排期模板
     * @author yl
     * @date 2023-02-17 11:41
     * @param request
     * @param response
     * @return void
     */
    void exportScheduleTemplate(HttpServletRequest request, HttpServletResponse response);


    /**
     * 查询任务是否更改了开始时间
     * @param projectPlanIds
     */
    void updateTaskInfo(List<String> projectPlanIds);

    /**
     * 通过ID查询任务相关信息
     * @param planIdList
     * @return
     */
    List<PlanTaskNameDTO> listByPlanId(List<String> planIdList);
}
