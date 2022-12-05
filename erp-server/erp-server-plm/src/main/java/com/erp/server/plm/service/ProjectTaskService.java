package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import javafx.util.Pair;

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


    List<ProjectTaskEntity> addSysTask(String id, List<TaskDocsNameEntity> taskDocsNameList, LoginUser loginUser );

    void removeTaskByProductId(String productId);

    List<ProjectTaskEntity> getByProductIds(List<String> productIds);

    List<ProjectTaskEntity> getByProductId(String productId);

    void copyTaskByProject(String saveProductId, String saveProjectId,String  flagProjectId);


    Pair<Boolean, List<ProjectTaskEntity>> copyTaskBySys(String productId, String projectId);


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

    PagingVO<List<TaskPagingShowDTO>> assignToMePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    PagingVO<List<TaskPagingShowDTO>> myCreatePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    void taskFinishSku(TaskFinishSkuDTO dto);

    /**
     * 分配给我
     * @author yl
     * @date 2022-11-29 16:23
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     */
    List<TaskGroupResultDTO> getGroupAssignToMeCondition(TaskGroupParamDTO dto);

    /**
     * 我创造的分组条件
     * @author yl
     * @date 2022-11-29 16:37
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     */
    List<TaskGroupResultDTO> groupMyCreateConditionList(TaskGroupParamDTO dto);

    
    /**
     * 交付文档 填写 是否更改了sku
     * @author yl
     * @date 2022-11-30 12:31
     * @param dto
     * @return void
     */
    void skuChangeResult(StateDTO dto);
}
