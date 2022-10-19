package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.workflow.dto.TaskOperateDTO;
import com.erp.server.plm.service.*;
import com.erp.server.plm.service.impl.*;
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

    @Autowired
    private CommonService commonService;


    /**
     * 项目任务-分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
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
    public ApiResult update(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = taskService.updateTask(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/saveSonTask")
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
    public ApiResult publishTask(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        TaskOperate taskOperate = new TaskOperate(new ReleaseTask());
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-取消发布
     *
     * @return
     */
    @PostMapping("/cancelPublishTask")
    public ApiResult cancelPublishTask(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        CancelRelease  cancelRelease=new CancelRelease();
        TaskOperate taskOperate = new TaskOperate(cancelRelease);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-开始任务
     *
     * @return
     */
    @PostMapping("/startTask")
    public ApiResult startTask(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        StartTask startTask=new StartTask();
        TaskOperate taskOperate = new TaskOperate(startTask);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-关闭任务
     *
     * @return
     */
    @PostMapping("/closeTask")
    public ApiResult closeTask(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        CloseTask  closeTask =new CloseTask();
        TaskOperate taskOperate = new TaskOperate(closeTask);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }


    /**
     * 项目任务-任务分页列表 -状态操作-完成任务
     *
     * @return
     */
    @PostMapping("/finishTask")
    public ApiResult finishTask(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        FinishTask  finishTask =new FinishTask();
        TaskOperate taskOperate = new TaskOperate(finishTask);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过
     *
     * @return
     */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        TaskApprovalPass approvalPass =new TaskApprovalPass();
        TaskOperate taskOperate = new TaskOperate(approvalPass);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过
     *
     * @return
     */
    @PostMapping("/approvalReject")
    public ApiResult approvalNoPass(@RequestBody TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        TaskApprovalReject approvalReject =new TaskApprovalReject();
        TaskOperate taskOperate = new TaskOperate(approvalReject);
        Boolean result = taskOperate.updateTaskState(dto.getTaskIdList(), dto.getProductId(), loginUser.getUid());
        return result == true ? success() : failure();
    }


}

