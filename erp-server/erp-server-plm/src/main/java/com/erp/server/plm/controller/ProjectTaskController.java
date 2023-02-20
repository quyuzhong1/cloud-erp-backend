package com.erp.server.plm.controller;


import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.enums.TaskPriorityEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:paging",
            tableAlias = "pt"
          )
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:taskDetails",
            serviceClass = ProjectTaskService.class
    )
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
    @PostMapping("/getProductTaskCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:paging",
            tableAlias = "project_task"
    )
    public ApiResult<ProductTaskCountDTO> getProductTaskCount(@RequestBody ProductTaskCountShowDTO showDTO) {
        ProductTaskCountDTO dto = taskService.getProductTaskCount(showDTO, new Date());
        return success(dto);
    }

    /**
     * 项目任务-2全部任务/0待我完成的任务/1待我审核的任务全部数量
     *
     * @return
     */
    @PostMapping("/listProductTaskCategoryCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:paging",
            tableAlias = "project_task"
    )
    public ApiResult<List<ProductTaskCategoryCountDTO>> listProductTaskCategoryCount(@RequestBody TaskPagingDTO dto) {
        List<ProductTaskCategoryCountDTO> list = taskService.listProductTaskCategoryCount(dto);
        return success(list);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
                tableField = "charge_id",
                menuCode = "plm:task:update",
                serviceClass = ProjectTaskService.class,
                keyIdName = "taskId"
    )
    public ApiResult updateTask(@RequestBody @Validated UpdateTaskDTO dto, HttpServletRequest request) {
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
            menuCode = "plm:task:project:status",
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
            menuCode = "plm:task:project:status",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
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
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult approvalNoPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = taskService.approvalReject(dto);
        return result == true ? success() : failure();
    }


    /**
     * 项目任务-任务分页列表 -状态操作-重新开始
     *
     * @return
     */
    @PostMapping("/restartTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult restartTask(@RequestBody @Validated OperateBaseTaskDTO dto) {
        Boolean result = taskService.restartTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情 -查看任务流程
     *
     * @return
     */
    @GetMapping("/findTaskProcess")
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
     * 任务列表-全部-分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:task:expert:paging:all",
            tableAlias = "t,tcd"
    )
    public ApiResult<List<TaskGroupResultDTO>> groupConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = taskService.getGroupCondition(dto);
        return success(resultList);
    }

    /**
     * 任务列表-分配给我-分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/assignToMe/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "t,tcd"
    )
    public ApiResult<List<TaskGroupResultDTO>> groupAssignToMeConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = taskService.getGroupAssignToMeCondition(dto);
        return success(resultList);
    }

    /**
     * 任务列表-我创造的-分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/myCreate/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:task:expert:paging:myCreate",
            tableAlias = "t"
    )
    public ApiResult<List<TaskGroupResultDTO>> groupMyCreateConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = taskService.groupMyCreateConditionList(dto);
        return success(resultList);
    }

    /**
     * 全部 任务列表
     *
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:task:expert:paging:all",
            tableAlias = "pt,tcd"
    )
    @PostMapping("/all/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> expertPaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.expertPaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 分配给我任务列表
     *
     * @return
     */

    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "pt,tcd"
    )
    @PostMapping("/assignToMe/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> assignToMePaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.assignToMePaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 分配给 待完成
     *
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "pt"
    )
    @PostMapping("/assignToMe/waitFinish/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> assignToMeWaitFinishPaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.assignToMePaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 分配给 我 待审核
     *
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "pt,tcd"
    )
    @PostMapping("/assignToMe/waitAudit/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> assignToMeWaitAuditPaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.assignToMeWaitAuditPaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 我创造的任务列表
     *
     *
     * @return
     */

    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:task:expert:paging:myCreate",
            tableAlias = "pt"
    )
    @PostMapping("/myCreate/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> myCreatePaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.myCreatePaging(searchParamDTO);
        return success(pagingVO);
    }


    /**
     * 任务列表-任务操作更多列表
     *
     * @return
     */
    @PostMapping("/operate/moreList")
    public ApiResult<List<Map<String, Object>>> operateMoreList(@Validated @RequestBody BaseIdDTO dto) {
        List<Map<String, Object>> list = taskService.operateMoreList(dto.getId());
        return success(list);
    }

    /**
     * 任务列表-任务状态下拉框
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/11/24 10:28
     */
    @GetMapping("/getTaskStatusSelect")
    public ApiResult<List<SelectShowDTO>> getTaskStatusSelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(TaskStateEnum.values()).filter(obj ->!TaskStateEnum.APPROVAL_PASS.getCode().equals(obj.getCode())).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getCode());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }

    /**
     * 任务列表-优先级下拉框
     * @author Will
     * @date: 2023/1/31 17:16
     * @return ApiResult<List<SelectShowDTO>>
     */
    @GetMapping("/getTaskPrioritySelect")
    public ApiResult<List<SelectShowDTO>> getTaskPrioritySelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(TaskPriorityEnum.values()).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getCode());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }


    /**
     * 配置表单-输出物-完成sku
     *
     * @param dto
     * @return
     * @author yl
     * @date 2022-11-29 14:42
     */
    @PostMapping(value = "/finishSku")
    public ApiResult finishSku(@RequestBody @Validated TaskFinishSkuDTO dto) {
        taskService.taskFinishSku(dto);
        return success();
    }

    /**
     * 配置表单-输出物-是否对sku 更改
     *
     * @param dto
     * @return
     * @author yl
     * @date 2022-11-29 14:42
     */
    @PostMapping(value = "/skuChangeResult")
    public ApiResult skuChangeResult(@RequestBody @Validated StateDTO dto) {
        taskService.skuChangeResult(dto);
        return success();
    }

    /**
     * 任务列表-飞书提醒
     * @author Will
     * @date: 2023/2/1 14:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/flyingBookReminder")
    public ApiResult flyingBookReminder(@RequestBody @Validated FlyingBookReminderDTO dto) {
        taskService.flyingBookReminder(dto);
        return success();
    }

}

