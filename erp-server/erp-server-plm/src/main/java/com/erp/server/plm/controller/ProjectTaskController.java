package com.erp.server.plm.controller;


import com.erp.common.annotation.DataPermission;
import com.erp.common.annotation.RequestPermissions;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.TaskOperateDTO;
import com.erp.server.plm.service.*;
import com.erp.server.plm.service.impl.*;
import com.erp.server.plm.utils.ApplicationContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/task")
public class ProjectTaskController extends BaseController {

    @Autowired
    private ProjectTaskService taskService;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private ProductInfoService productInfoService;

    /**
     * 项目任务-分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    //  @RequestPermissions("plm:task:paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:paging", tableAlias = "project_task")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> paging(@RequestBody @Validated PagingDTO<TaskPagingDTO> dto) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 项目任务-新建任务
     *
     * @param dto
     * @return
     */
    @PostMapping("/save")
    //  @RequestPermissions("plm:task:save")
    public ApiResult save(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.save(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-编辑任务
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    //   @RequestPermissions("plm:task:update")
/*    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:update",
            serviceClass = ProjectTaskService.class
    )*/
    public ApiResult update(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.updateTask(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/saveSonTask")
    //   @RequestPermissions("plm:task:saveSonTask")
    public ApiResult saveSonTask(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.save(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-新建任务-获取前置任务列表
     *
     * @param productId
     * @return
     */
    @GetMapping("/list")
    //  @RequestPermissions("plm:task:list")
    public ApiResult list(String productId) {
        List<Map<String, Object>> list = taskService.getTaskListByProductId(productId);
        return success(list);
    }

    /**
     * 项目任务-编辑任务-获取任务详情
     *
     * @param taskId
     * @return
     */
    @GetMapping("/taskDetails")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:taskDetails",
            serviceClass = ProjectTaskService.class
    )
    //   @RequestPermissions("plm:task:taskDetails")
    public ApiResult<ProjectTaskDTO> taskDetails(String taskId) {
        ProjectTaskDTO taskDTO = taskService.taskDetails(taskId);
        return success(taskDTO);
    }

    /**
     * 项目任务-任务详情-删除任务
     *
     * @param dto
     * @return
     */
    @PostMapping("/removeTask")
    //   @RequestPermissions("plm:task:removeTask")
    public ApiResult remove(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = taskService.removeTask(dto.getId());
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情-关联前置任务
     *
     * @param dto
     * @return
     */
    @PostMapping("/setPreTask")
    //   @RequestPermissions("plm:task:setPreTask")
    public ApiResult setPreTask(@RequestBody @Validated SetPreTaskDTO dto) {
        Boolean flag = preTaskService.addPreTask(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情-移除前置任务
     *
     * @param dto
     * @return
     */
    @PostMapping("/removePreTask")
//    @RequestPermissions("plm:task:removePreTask")
    public ApiResult removePreTask(@RequestBody @Validated SetPreTaskDTO dto) {
        Boolean flag = preTaskService.removePreTask(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情
     *
     * @param taskId
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProjectTaskDetailsDTO>
     * @author yl
     * @date 2022-10-11 11:23
     */
    @GetMapping("/details")
//    @RequestPermissions("plm:task:details")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:details",
            serviceClass = ProjectTaskService.class
    )
    public ApiResult<ProjectTaskDetailsDTO> details(String taskId) {
        ProjectTaskDetailsDTO detailsDTO = taskService.getTaskDetails(taskId);
        return success(detailsDTO);
    }

    /**
     * 项目任务-任务各类总数信息
     *
     * @return
     */
    @GetMapping("/getProductTaskCount")
    //@RequestPermissions("plm:task:getProductTaskCount")
    public ApiResult<ProductTaskCountDTO> getProductTaskCount(String productId) {
        ProductTaskCountDTO dto = taskService.getProductTaskCount(productId, new Date());
        return success(dto);
    }

    /**
     * 项目任务-获取新建产品 -所属产品列表
     *
     * @return
     */
    @GetMapping("/getProductList")
    //   @RequestPermissions("plm:task:getProductList")
    public ApiResult<List<ProductProjectDTO>> getProductList() {
        List<ProductProjectDTO> resultList = productInfoService.getProductAndProjectList();
        return success(resultList);
    }


    /**
     * 项目任务-任务分页列表 -修编辑任务名，计划开始结束时间，任务负责人
     *
     * @return
     */
    @PostMapping("/updateTask")
//    @RequestPermissions("plm:task:updateTask")
    public ApiResult updateTask(@RequestBody @Validated UpdateTaskDTO dto) {
        Boolean result = taskService.updateBaseTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-发布任务
     *
     * @return
     */
    @PostMapping("/publishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult publishTask(@RequestBody @Validated OperateBaseTaskDTO dto) {
        Boolean result = taskService.publishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-取消发布
     *
     * @return
     */
    @PostMapping("/cancelPublishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult cancelPublishTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = taskService.cancelPublishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-开始任务
     *
     * @return
     */
    @PostMapping("/startTask")
    //  @RequestPermissions("plm:task:startTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult startTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = taskService.startTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-关闭任务
     *
     * @return
     */
    @PostMapping("/closeTask")
    //  @RequestPermissions("plm:task:closeTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult closeTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = taskService.closeTask(dto);
        return result == true ? success() : failure();
    }


    /**
     * 项目任务-任务分页列表 -状态操作-完成任务
     *
     * @return
     */
    @PostMapping("/finishTask")
    //  @RequestPermissions("plm:task:finishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult finishTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = taskService.finishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过
     *
     * @return
     */
    @PostMapping("/approvalPass")
    //  @RequestPermissions("plm:task:approvalPass")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult approvalPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = taskService.approvalPass(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过
     *
     * @return
     */
    @PostMapping("/approvalReject")
    //  @RequestPermissions("plm:task:approvalReject")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult approvalNoPass(@RequestBody TaskOperateDTO dto) {
        Boolean result = taskService.approvalReject(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情 -查看任务流程
     *
     * @return
     */
    @GetMapping("/findTaskProcess")
    //  @RequestPermissions("plm:task:findTaskProcess")
    public ApiResult<List<TaskProcessNodeDTO>> findTaskProcess(String taskId) {
        List<TaskProcessNodeDTO> taskProcess = taskService.findTaskProcess(taskId);
        return success(taskProcess);
    }


    /**
     * 工作流
     * 审核通过 改变任务状态
     * 以及
     *
     * @return
     */
    @PostMapping("/workflow/pass")
    public ApiResult processPass(String processId) {
        taskService.approvalTaskPass(processId);
        return success();
    }


    /**
     * 分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/list")
    public ApiResult<List<TaskGroupResultDTO>> groupConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = taskService.getGroupCondition(dto);
        return success(resultList);
    }


}

