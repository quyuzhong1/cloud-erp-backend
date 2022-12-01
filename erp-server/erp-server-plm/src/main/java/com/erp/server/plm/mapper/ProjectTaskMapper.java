package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

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


    IPage<TaskPagingShowDTO> paging(Page query,@Param("productId") String productId ,@Param("phaseId") String phaseId, @Param("searchList") List<TaskSearchDTO> searchList,
                 @Param("userId") String userId,@Param("searchKeyword") String searchKeyword,
                 @Param("statusList") List<Integer> statusList, @Param("param") String param
                 );

    List<TaskExcelDTO> getExportTask(@Param("productIds") List<String> productIds);

    /**
     * 获取任务详情
     * @param taskId
     * @return
     */
    ProjectTaskDetailsDTO getTaskDetails(@Param("taskId") String taskId);


    List<RefTaskInfoDTO> getRefTask(@Param("taskIds") List<String> taskIds);

    List<TaskPagingShowDTO> allChildrenList(@Param("productId") String productId);

    IPage<TaskPagingShowDTO> myApprovalPaging(Page query,@Param("productId") String productId ,@Param("phaseId") String phaseId, @Param("searchList") List<TaskSearchDTO> searchList,
                          @Param("userId") String userId,@Param("searchKeyword") String searchKeyword,
                          @Param("statusList") List<Integer> statusList,@Param("processIdList") List<String> processIdList);

    int findUndone(@Param("finishState") Integer finishState,@Param("approvalPassState") Integer approvalPassState, @Param("taskIds") List<String> preTaskIds);

    List<TaskGroupResultDTO> toMeTaskGroup(@Param("param") String param,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> toMeTaskPlanEndTimeGroup(@Param("param") String param, List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskGroup(@Param("param") String param,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskPlanEndTimeGroup(@Param("param") String param, @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> allTaskGroup( @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> taskPlanEndTimeGroup(@Param("notStateList") List<Integer> notStateList);

    IPage<TaskPagingShowDTO> toMeProductTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

    IPage<TaskPagingShowDTO> toMePlanEndTimeTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    IPage<TaskPagingShowDTO> myCreateProductTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

    IPage<TaskPagingShowDTO> myCreatePlanEndTimeTaskList(Page query,@Param("userId") String userId,@Param("notStateList") List<Integer> notStateList,@Param("params") TaskSearchParamDTO params,  @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    IPage<TaskPagingShowDTO> allProductTaskList(Page query, @Param("notStateList") List<Integer> notStateList, @Param("params")TaskSearchParamDTO params);

    IPage<TaskPagingShowDTO> allPlanTimeTaskList(Page query,@Param("notStateList") List<Integer> notStateList,@Param("params") TaskSearchParamDTO params, @Param("startTime")Date startTime,@Param("endTime") Date endTime);

    List<ProjectTaskEntity> getExpireWarnTaskList(@Param("startTime") Date startNowDate,@Param("endTime") Date flagDateEnd ,@Param("state") Integer state);
    /**
     * @description: 任务视图查询根据负责人所有任务
     * @author Will
     * @date: 2022/11/23 15:54
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskPersonnelView(@Param("dto") ProductTaskViewSearchDTO dto);

    /**
     * @description: 任务视图根据产品查询所有任务
     * @author Will
     * @date: 2022/11/23 15:55
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskProductView(@Param("dto") ProductTaskViewSearchDTO dto);

    /**
     * @description: 任务视图根据阶段查询所有任务
     * @author Will
     * @date: 2022/11/23 15:55
     * @param dto
     * @return List<ProductTaskViewDTO>
     */
    List<ProductTaskViewDTO> getAllTaskPhaseView(@Param("dto") ProductTaskViewSearchDTO dto);

    /**
    /**
     * @description: 任务视图查询所有量产入库数据
     * @author Will
     * @date: 2022/11/23 15:56
     * @param dto
     * @return List<ProductTaskInWarehouseTimeChildDTO>
     */
    List<ProductTaskInWarehouseTimeChildDTO> getAllTaskInWarehouseTimeView(@Param("dto") ProductTaskViewSearchDTO dto);
}

