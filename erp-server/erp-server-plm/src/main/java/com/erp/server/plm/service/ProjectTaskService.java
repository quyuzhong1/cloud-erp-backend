package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品任务表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTaskService extends IService<ProjectTaskEntity> {


    List<ProjectTaskEntity> addSysTask(String id,List<TaskDocsNameEntity> taskDocsNameList);

    void removeTaskByProductId(String productId);

    List<ProjectTaskEntity> getByProductIds(List<String> productIds);

    List<ProjectTaskEntity> getByProductId(String productId);

    void copyTaskByProject(String saveProductId, String saveProjectId,String  flagProjectId);


    List<ProjectTaskEntity> copyTaskBySys(String productId, String projectId);


    PagingVO<List<TaskPagingShowDTO>> paging(PagingDTO<TaskPagingDTO> dto);

    Boolean save(ProjectTaskDTO dto);

    List<Map<String, Object>> getTaskListByProductId(String productId );

    Boolean removeTask(String id);



    List<TaskConductDTO> getTaskConductList(String productId);

    TaskConductDTO getTaskConduct(String productId);

    List<TaskExcelDTO> getExportTask(List<String> productIds);


    ProjectTaskDetailsDTO getTaskDetails(String taskId);

    Boolean updateTask(ProjectTaskDTO dto);

    ProductTaskCountDTO getProductTaskCount(String productId, Date date);

    Boolean updateBaseTask(UpdateTaskDTO dto);


    ProjectTaskDTO taskDetails(String taskId);

    List<ProjectTaskEntity> getByTaskIds(List<String> taskIds);

    boolean updateTaskState(List<String> taskIds, Integer state,Date realityStart,Date realityEnd);


    int countUndoneByTaskIds(Integer code, Integer approvalPass, List<String> preTaskIds);

    void checkSonTaskFinish(List<String> noProcessTaskIds,String productId);

    Boolean startTask(OperateBaseTaskDTO dto);

    Boolean publishTask(OperateBaseTaskDTO dto);

    Boolean cancelPublishTask(OperateBaseTaskDTO dto);

    Boolean closeTask(OperateBaseTaskDTO dto);

    Boolean finishTask(OperateBaseTaskDTO dto);

    Boolean approvalPass(TaskOperateDTO dto);

    Boolean approvalReject(TaskOperateDTO dto);


    List<TaskProcessNodeDTO>  findTaskProcess(String taskId);

    void approvalTaskPass(String processId);

    Integer getTaskProperty(ProjectTaskEntity taskEntity);



    void checkTaskFinish(List<ProjectTaskEntity> list);

    List<TaskGroupResultDTO> getGroupCondition(TaskGroupParamDTO dto);

    PagingVO<List<TaskPagingShowDTO>> expertPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    List<Map<String, Object>> operateMoreList(String taskId);

    List<ProjectTaskEntity> getExpireTaskList(Date nowDay, int i);

    Boolean restartTask(OperateBaseTaskDTO dto);

}
