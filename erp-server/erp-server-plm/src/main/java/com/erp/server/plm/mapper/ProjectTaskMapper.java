package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ChangeScheduleExportVO;
import com.erp.model.plm.vo.ProductTaskVO;
import com.erp.model.plm.vo.ScheduleTaskExportExcelVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品任务表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectTaskMapper extends BaseMapper<ProjectTaskEntity> {


    IPage<TaskPagingShowDTO> paging(Page query,@Param("params") TaskPagingDTO params,@Param("userId") String userId);

    List<TaskDTO.TaskExportDTO> waitMyFinishExport(@Param("params") TaskPagingDTO.ExportDTO params, @Param("userId")String userId);
    Page<TaskDTO.TaskExportDTO> waitMyFinishExport(@Param("page") Page<TaskDTO.TaskExportDTO> page, @Param("params") TaskPagingDTO.ExportDTO params, @Param("userId")String userId);

    Integer pagingCount(@Param("productId") String productId,@Param("userId") String userId,
                                    @Param("statusList") List<Integer> statusList, @Param("param") String param
    );


    /**
     *产品的全部任务列表
     * @return
     */
    IPage<TaskPagingShowDTO> allPaging(Page query, @Param("params") TaskPagingDTO params);

    /**
     * 所有导出
     * @param params
     * @return
     */
    List<TaskDTO.TaskExportDTO> allExport(@Param("params") TaskPagingDTO.ExportDTO params);
    Page<TaskDTO.TaskExportDTO> allExport(@Param("page") Page<TaskDTO.TaskExportDTO> page, @Param("params") TaskPagingDTO.ExportDTO params);

    /**
     * 变更导出
     * @param params
     * @return
     */
    List<TaskDTO.TaskExportDTO> changeExport(@Param("params") TaskPagingDTO.ExportDTO params);
    Page<TaskDTO.TaskExportDTO> changeExport(@Param("page") Page<TaskDTO.TaskExportDTO> page, @Param("params") TaskPagingDTO.ExportDTO params);

    /**
     * 这个是变更的任务
     * @author yl
     * @date 2023-06-25 11:39
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     */
    IPage<TaskPagingShowDTO> changePaging(Page query,@Param("params") TaskPagingDTO params);


    Integer allPagingCount( @Param("productId") String productId,@Param("param") String param);

    /**
     * 统计变更任务数
     * @param productId
     * @param param
     * @param scheduleType
     * @return
     */
    Integer changeCount(@Param("productId")String productId,@Param("param") String param,@Param("scheduleType") String scheduleType);

    List<TaskExcelDTO> getExportTask(@Param("productIds") List<String> productIds);

    /**
     * 获取任务详情
     * @param taskId
     * @return
     */
    ProjectTaskDetailsDTO getTaskDetails(@Param("taskId") String taskId);


    List<RefTaskInfoDTO> getRefTask(@Param("taskIds") List<String> taskIds);

    List<TaskPagingShowDTO> allChildrenList(@Param("productId") String productId);

    IPage<TaskPagingShowDTO> myApprovalPaging(Page query,@Param("params") TaskPagingDTO params,@Param("processIdList") List<String> processIdList);

    /**
     * 待我审核
     * @param params
     * @param processIds
     * @return
     */
    List<TaskDTO.TaskExportDTO> myApprovaExport(@Param("params")TaskPagingDTO.ExportDTO params, @Param("processIdList")List<String> processIds);
    Page<TaskDTO.TaskExportDTO> myApprovaExport(@Param("page") Page<TaskDTO.TaskExportDTO> page, @Param("params")TaskPagingDTO.ExportDTO params, @Param("processIdList")List<String> processIds);

    Integer myApprovalPagingCount(@Param("productId") String productId,@Param("userId") String userId,
                                              @Param("statusList") List<Integer> statusList,@Param("processIdList") List<String> processIdList);

    int findUndone(@Param("excludeStatusList") List<Integer> excludeStatusList, @Param("taskIds") List<String> preTaskIds);

    List<TaskGroupResultDTO> toMeTaskGroup(@Param("params") TaskGroupParamDTO params,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> toMeTaskPlanEndTimeGroup(@Param("params") TaskGroupParamDTO params, List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskGroup(@Param("params") TaskGroupParamDTO params,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskPlanEndTimeGroup(@Param("params") TaskGroupParamDTO params, @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> allTaskGroup(@Param("params") TaskGroupParamDTO params,  @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> taskPlanEndTimeGroup(@Param("params") TaskGroupParamDTO params, @Param("notStateList") List<Integer> notStateList);

    IPage<TaskPagingShowDTO> listProductTaskBySearchCategory(Page query, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

    List<ProjectTaskEntity> getExpireWarnTaskList(@Param("startTime") Date startNowDate,@Param("endTime") Date flagDateEnd ,@Param("state") Integer state);
    /**
     * @description: 任务视图查询根据负责人所有任务
     * @author Will
     * @date: 2022/11/23 15:54
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskPersonnelView(@Param("dto") ProductTaskViewSearchDTO dto);
    Page<ProductTaskViewDTO> getAllTaskPersonnelView(@Param("page") Page<ProductTaskViewDTO> page, @Param("dto") ProductTaskViewSearchDTO dto);

    /**
     * @description: 任务视图根据产品查询所有任务
     * @author Will
     * @date: 2022/11/23 15:55
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskProductView(@Param("dto") ProductTaskViewSearchDTO dto);
    Page<ProductTaskViewDTO> getAllTaskProductView(@Param("page") Page<ProductTaskViewDTO> page, @Param("dto") ProductTaskViewSearchDTO dto);

    /**
     * @description: 任务视图根据阶段查询所有任务
     * @author Will
     * @date: 2022/11/23 15:55
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskPhaseView(@Param("dto") ProductTaskViewSearchDTO dto);
    Page<ProductTaskViewDTO> getAllTaskPhaseView(@Param("page") Page<ProductTaskViewDTO> page, @Param("dto") ProductTaskViewSearchDTO dto);
    /**
    /**
     * @description: 任务视图查询所有量产入库数据
     * @author Will
     * @date: 2022/11/23 15:56
     * @param dto
     * @return List<ProductTaskInWarehouseTimeChildDTO>
     */
    List<ProductTaskInWarehouseTimeChildDTO> getAllTaskInWarehouseTimeView(@Param("dto") ProductTaskViewSearchDTO dto);
    Page<ProductTaskViewDTO> getAllTaskInWarehouseTimeViewByPage(@Param("page") Page<ProductTaskViewDTO> page, @Param("dto") ProductTaskViewSearchDTO dto);



    /**
     * 根据产品id查询项目任务
     * @Author Luo_WG
     * @Date 2022/12/6 9:35
     * @param showDTO
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskEntity>
     **/
    List<ProjectTaskEntity> getProjectTaskByProductId(@Param("showDTO") ProductTaskCountShowDTO showDTO);

    /**
     * 根据产品id 获取到项目计划的任务
     * @author yl
     * @date 2023-02-08 9:26
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.ProductTaskVO>
     */
    List<ProductTaskVO> getScheduleTask(@Param("dto") ProjectPlanTaskConditionDTO dto);
    /**
     * 导出排期的任务列表
     * @author yl
     * @date 2023-06-25 15:51
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskDTO.TaskExportDTO>
     */
    List<TaskDTO.TaskExportDTO> listScheduleTaskExport(@Param("dto") ProjectPlanTaskConditionDTO dto);
    Page<TaskDTO.TaskExportDTO> listScheduleTaskExport(@Param("page") Page<TaskDTO.TaskExportDTO> page, @Param("dto") ProjectPlanTaskConditionDTO dto);

    /**
     * 更改任务排期状态
     * @param productId
     * @param taskIdList
     * @param status
     */
    void updateScheduleStatus(@Param("productId") String productId, @Param("taskIdList") List<String> taskIdList, @Param("scheduleStatus") String status);

    /**
     * 更改任务排期状态及类型
     * @param productId
     * @param taskIdList
     * @param status
     */
    void updateScheduleTask(@Param("productId") String productId, @Param("taskIdList") List<String> taskIdList, @Param("scheduleStatus") String status,@Param("scheduleType") String scheduleType);

    Map<String, Object> getProductMapByProductId(@Param("productId") String productId);

    List<ScheduleTaskVO> getScheduleTaskByTaskIds(@Param("productId") String productId, @Param("taskIdList")List<String> taskIds);

    List<ScheduleTaskExportExcelVO> getExportScheduleTask(@Param("dto") HandleTaskScheduleDTO dto);

    ScheduleTaskExportExcelVO getExport(@Param("productId") String productId, @Param("taskId")  String taskId);

    List<ChangeScheduleExportVO> getExportChangeScheduleTask(@Param("dto") HandleTaskScheduleDTO dto);

    Boolean removeBatch(@Param("ids") List<String> ids);

    /**
     * 根据负责人id 获取到产品id
     * @param userId
     * @return
     */
    List<String> listProductIdByTaskChargeId(@Param("chargeId") String userId);

    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/23 17:22
     * @param chargeId
     * @return List<ProjectTaskEntity>
     */
    List<ProjectTaskEntity> listByChargeId(@Param("chargeId")String chargeId);

    /**
     * 任务高级查询
     */
    IPage<ProjectTaskDTO.SimpleViewDTO> pagingByAdvanceQuery(Page query, @Param("params") ProjectTaskDTO.PagingParamDTO params, @Param("spuNos") List<String> spuNos);

    /**
     * 根据任务Id查询
     */
    List<ProjectTaskDTO.SimpleViewDTO> listByTaskIds(@Param("taskIds") List<String> taskIds);
}

