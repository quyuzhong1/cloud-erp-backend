package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
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


    IPage paging(Page query,@Param("productId") String productId ,@Param("phaseId") String phaseId, @Param("searchList") List<TaskSearchDTO> searchList,
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

    IPage myApprovalPaging(Page query,@Param("productId") String productId ,@Param("phaseId") String phaseId, @Param("searchList") List<TaskSearchDTO> searchList,
                          @Param("userId") String userId,@Param("searchKeyword") String searchKeyword,
                          @Param("statusList") List<Integer> statusList,@Param("processIdList") List<String> processIdList);

    int findUndone(@Param("finishState") Integer finishState,@Param("approvalPassState") Integer approvalPassState, @Param("taskIds") List<String> preTaskIds);

    List<TaskGroupResultDTO> toMeTaskGroup(@Param("userId") String userId,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> toMeTaskPlanEndTimeGroup(String userId, List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskGroup(@Param("userId") String userId,@Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> myCreateTaskPlanEndTimeGroup(@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> allTaskGroup( @Param("notStateList") List<Integer> notStateList);

    List<TaskGroupResultDTO> taskPlanEndTimeGroup(List<Integer> notStateList);

    IPage toMeProductTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

    IPage toMePlanEndTimeTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    IPage myCreateProductTaskList(Page query,@Param("userId") String userId, @Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

    IPage myCreatePlanEndTimeTaskList(Page query,@Param("userId") String userId,@Param("notStateList") List<Integer> notStateList,@Param("params") TaskSearchParamDTO params,  @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    IPage allProductTaskList(Page query, @Param("notStateList") List<Integer> notStateList, @Param("params")TaskSearchParamDTO params);

    IPage allPlanTimeTaskList(Page query,@Param("notStateList") List<Integer> notStateList,@Param("params") TaskSearchParamDTO params, @Param("startTime")Date startTime,@Param("endTime") Date endTime);
}

