package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;

import javax.servlet.http.HttpServletResponse;
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
    /**
     * @description: 根据产品id获取里程碑任务
     * @author Will
     * @date: 2022/11/18 15:08
     * @param productId
     * @return List<ProductMilepostDTO>
     */
    List<ProductMilepostDTO> getMilepostTaskListByProductId(String productId);
    /**
     * @description: 查询里程碑结束时间
     * @author Will
     * @date: 2022/11/18 16:47
     * @param dto
     * @return ProductMilepostDateDTO
     */
    ProductMilepostDateDTO getMilepostDate(ProductMilepostParamDTO dto);
    /**
     * @description: 查询产品各个阶段任务完成进度
     * @author Will
     * @date: 2022/11/21 9:27
     * @param productId
     * @return List<ProductPhaseProgressDTO>
     */
    List<ProductPhaseProgressDTO> getFinishProgressList(String productId);
    /**
     * @description: 项目视图按人员查询
     * @author Will
     * @date: 2022/11/23 11:50
     * @param dto
     * @return List<ProductTaskPersonnelViewDTO>
     */
    List<ProductTaskPersonnelViewDTO> getPersonnelView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto 
     * @return List<ProductTaskProductViewDTO> 
     */
    List<ProductTaskProductViewDTO> getProductView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto 
     * @return List<ProductTaskPhaseViewDTO> 
     */
    List<ProductTaskPhaseViewDTO> getPhaseView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto 
     * @return List<ProductTaskInWarehouseTimeViewDTO>
     */
    List<ProductTaskInWarehouseTimeViewDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图导出
     * @author Will
     * @date: 2022/11/23 16:16
     * @param dto
     * @param response

     */
    void exportExcel(ProductTaskViewSearchDTO dto, HttpServletResponse response);
}
