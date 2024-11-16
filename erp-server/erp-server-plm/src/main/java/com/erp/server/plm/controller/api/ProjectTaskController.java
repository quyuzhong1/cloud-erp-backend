package com.erp.server.plm.controller.api;


import com.alibaba.excel.EasyExcel;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProjectTaskExcelDTO;
import com.erp.model.plm.entity.ProjectTaskVO;
import com.erp.model.plm.enums.TaskPriorityEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.ProjectTaskExcelListener;
import com.erp.server.plm.query.ProjectTaskQueryHandler;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@LogSystemModule("任务列表")
@RequestMapping("task")
public class ProjectTaskController extends BaseController {

    @Autowired
    private ProjectTaskService projectTaskService;


    @Autowired
    private TaskService taskService;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProjectPhaseService projectPhaseService;

    @Autowired
    private TaskDocsNameService taskDocsNameService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;

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
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出任务【plm1.3】
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-06-25 11:55
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出任务")
    @PostMapping("/exportTask")
    public ApiResult exportTask(@RequestBody @Validated TaskPagingDTO.ExportDTO dto) {
        Boolean result =taskService.exportTask(dto);
        return result ? success() : failure();

    }

    /**
     * 项目任务-新建任务【PLM1.3】
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "项目任务-新建任务")
    @PostMapping("/save")
    public ApiResult save(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = projectTaskService.save(dto);
        return flag ? success() : failure();
    }

    /**
     * 项目任务-编辑任务
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "项目任务-编辑任务")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:update",
            serviceClass = ProjectTaskService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = projectTaskService.updateTask(dto);
        return flag == true ? success() : failure();
    }


    @LogAction(value = LogActionEnum.INSERT, desc = "项目任务-保存子任务")
    @PostMapping("/saveSonTask")
    public ApiResult saveSonTask(@RequestBody @Validated ProjectTaskDTO dto) {
        Boolean flag = projectTaskService.save(dto);
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
        List<Map<String, Object>> list = projectTaskService.getTaskListByProductId(productId);
        return success(list);
    }

    /**
     * 项目任务-编辑任务-获取任务详情
     *
     * @param taskId
     * @return
     */
    @LogViewService
    @GetMapping("/taskDetails")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:taskDetails",
            serviceClass = ProjectTaskService.class
    )
    public ApiResult<ProjectTaskVO> taskDetails(String taskId) {
        ProjectTaskVO taskVO = projectTaskService.taskDetails(taskId);
        return success(taskVO);
    }

    /**
     * 项目任务-任务详情-删除任务
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "项目任务-任务详情-删除任务")
    @PostMapping("/removeTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:removeTask",
            serviceClass = ProjectTaskService.class,
            keyIdName = "id"
    )
    public ApiResult remove(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = projectTaskService.removeTask(dto.getId());
        return flag == true ? success() : failure();
    }


    /**
     * 更新前置任务列表
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新前置任务列表:id={id}")
    @PostMapping("/update/pre/task")
    public ApiResult setPreTask(@RequestBody @Validated @NotEmpty(message = "参数列表不能为空") List<PreTaskUpdateDTO> dto) {
        Boolean flag = preTaskService.updatePreTask(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询前置任务列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list/pre/task")
    public ApiResult<List<PreTaskListVO>> listPreTask(@RequestBody @Validated PreTaskDTO.ListPreTaskDTO dto) {
        List<PreTaskListVO> reusltList = preTaskService.ListPreTaskByTaskId(dto.getTaskId());
        return success(reusltList);
    }

    /**
     * 项目任务-任务详情-移除前置任务
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "项目任务-任务详情-移除前置任务")
    @PostMapping("/removePreTask")
    public ApiResult removePreTask(@RequestBody @Validated SetPreTaskDTO dto) {
        Boolean flag = preTaskService.removePreTask(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情
     *
     * @param taskId
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProjectTaskDetailsDTO>
     * @author yl
     * @date 2022-10-11 11:23
     */
    @GetMapping("/details")
    public ApiResult<ProjectTaskDetailsDTO> details(String taskId) {
        ProjectTaskDetailsDTO detailsDTO = projectTaskService.getTaskDetails(taskId);
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
        ProductTaskCountDTO dto = projectTaskService.getProductTaskCount(showDTO, new Date());
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
        List<ProductTaskCategoryCountDTO> list = projectTaskService.listProductTaskCategoryCount(dto);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改任务", keyIdName = "taskId")
    @PostMapping("/updateTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:update",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskId"
    )
    public ApiResult updateTask(@RequestBody @Validated UpdateTaskDTO dto, HttpServletRequest request) {
        Boolean result = projectTaskService.updateBaseTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-发布任务
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目任务发布任务:产品id={productId},任务ids={taskIdList}")
    @PostMapping("/publishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult publishTask(@RequestBody @Validated OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.publishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-取消发布
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目任务取消发布:产品id={productId},任务ids={taskIdList}")
    @PostMapping("/cancelPublishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:project:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult cancelPublishTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.cancelPublishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-开始任务
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "开始项目任务:产品id={productId},任务id={taskIdList}")
    @PostMapping("/startTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult startTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.startTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-关闭任务
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "关闭项目任务:产品id={productId},任务id={taskIdList}")
    @PostMapping("/closeTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult closeTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.closeTask(dto);
        return result == true ? success() : failure();
    }


    /**
     * 项目任务-任务分页列表 -状态操作-完成任务
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "完成项目任务:产品id={productId},任务id={taskIdList}")
    @PostMapping("/finishTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult finishTask(@RequestBody OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.finishTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核通过项目任务", keyIdName = "productId")
    @PostMapping("/approvalPass")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult approvalPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = projectTaskService.approvalPass(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核不通过项目任务", keyIdName = "productId")
    @PostMapping("/approvalReject")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult approvalNoPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = projectTaskService.approvalReject(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-撤销【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销项目任务")
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = projectTaskService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }




    /**
     * 项目任务-任务分页列表 -状态操作-重新开始
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重新开始项目任务:产品id={productId},任务id={taskIdList}")
    @PostMapping("/restartTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:tasks:status",
            serviceClass = ProjectTaskService.class,
            keyIdName = "taskIdList"
    )
    public ApiResult restartTask(@RequestBody @Validated OperateBaseTaskDTO dto) {
        Boolean result = projectTaskService.restartTask(dto);
        return result == true ? success() : failure();
    }

    /**
     * 项目任务-任务详情 -查看任务流程
     *
     * @return
     */
    @GetMapping("/findTaskProcess")
    public ApiResult<List<TaskProcessNodeDTO>> findTaskProcess(String taskId) {
        List<TaskProcessNodeDTO> taskProcess = projectTaskService.findTaskProcess(taskId);
        return success(taskProcess);
    }

    /**
     * 项目任务-查看任务审核情况【PLM1.3】
     *
     * @return
     */
    @GetMapping("/listTaskAudit")
    public ApiResult<List<ApproveNodeRecordVO>> listTaskAudit(String taskId) {
        List<ApproveNodeRecordVO> taskProcess = projectTaskService.listTaskAudit(taskId);
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
        projectTaskService.approvalTaskPass(processId);
        return success();
    }


    /**
     * 任务列表-全部-分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:expert:paging:all",
            tableAlias = "t"
    )
    public ApiResult<List<TaskGroupResultDTO>> groupConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = projectTaskService.getGroupCondition(dto);
        return success(resultList);
    }

    /**
     * 任务列表-分配给我-分组条件列表
     *
     * @return
     */
    @PostMapping("/group/condition/assignToMe/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "t"
    )
    public ApiResult<List<TaskGroupResultDTO>> groupAssignToMeConditionList(@Validated @RequestBody TaskGroupParamDTO dto) {
        List<TaskGroupResultDTO> resultList = projectTaskService.getGroupAssignToMeCondition(dto);
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
        List<TaskGroupResultDTO> resultList = projectTaskService.groupMyCreateConditionList(dto);
        return success(resultList);
    }

    /**
     * 全部 任务列表
     *
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:expert:paging:all",
            tableAlias = "pt"
    )
    @PostMapping("/all/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> expertPaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.expertPaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 分配给我任务列表
     *
     * @return
     */

    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:expert:paging:assignToMe",
            tableAlias = "pt"
    )
    @PostMapping("/assignToMe/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> assignToMePaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.assignToMePaging(searchParamDTO);
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
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.assignToMePaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 分配给 我 待审核
     *
     * @return
     */
    @PostMapping("/assignToMe/waitAudit/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> assignToMeWaitAuditPaging(@Validated @RequestBody PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.assignToMeWaitAuditPaging(searchParamDTO);
        return success(pagingVO);
    }

    /**
     * 我创造的任务列表
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
        PagingVO<List<TaskPagingShowDTO>> pagingVO = projectTaskService.myCreatePaging(searchParamDTO);
        return success(pagingVO);
    }


    /**
     * 任务列表-任务操作更多列表
     *
     * @return
     */
    @PostMapping("/operate/moreList")
    public ApiResult<List<Map<String, Object>>> operateMoreList(@Validated @RequestBody BaseIdDTO dto) {
        List<Map<String, Object>> list = projectTaskService.operateMoreList(dto.getId());
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
        Arrays.stream(TaskStateEnum.values()).filter(obj -> !TaskStateEnum.APPROVAL_PASS.getCode().equals(obj.getCode())).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getCode());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }

    /**
     * 任务列表-优先级下拉框
     *
     * @return ApiResult<List < SelectShowDTO>>
     * @author Will
     * @date: 2023/1/31 17:16
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
        projectTaskService.taskFinishSku(dto);
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "配置表单输出物是否对sku更改:id={id},模板状态={state}(1=启用,0=未启用)")
    @PostMapping(value = "/skuChangeResult")
    public ApiResult skuChangeResult(@RequestBody @Validated StateDTO dto) {
        projectTaskService.skuChangeResult(dto);
        return success();
    }

    /**
     * 任务列表-飞书提醒
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/2/1 14:13
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "飞书提醒:用户ids={userIds}")
    @PostMapping(value = "/flyingBookReminder")
    public ApiResult flyingBookReminder(@RequestBody @Validated FlyingBookReminderDTO dto) {
        projectTaskService.flyingBookReminder(dto);
        return success();
    }

    /**
     * 任务列表-查询模板任务
     *
     * @param
     * @return ApiResult
     * @Author Luo_WG
     * @Date 2023/3/20 10:17
     **/
    @PostMapping(value = "/templateTaskList")
    public ApiResult<PagingVO<TemplateTaskShowDTO>> templateTaskList(@RequestBody @Validated PagingDTO<TemplateTaskSearchDTO> dto) {
        return success(templateTaskService.templateTaskList(dto));
    }

    /**
     * 任务列表-模板引入任务
     *
     * @param
     * @return ApiResult
     * @Author Luo_WG
     * @Date 2023/3/20 10:17
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "模板引入任务")
    @PostMapping(value = "/templateCiteTask")
    public ApiResult templateCiteTask(@RequestBody @Validated TemplateCiteTaskDTO dto) {
        return success(templateTaskService.templateCiteTask(dto));
    }

    /**
     * 项目任务-批量删除任务
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/3/29 18:16
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除任务")
    @PostMapping("/removeBatch")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:task:removeTask",
            serviceClass = ProjectTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult removeBatch(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectTaskService.removeBatch(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-excel导入产品任务【PLM1.3】
     *
     * @param excelFile 文件流
     * @param response  响应
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导入产品任务")
    @PostMapping("/importProjectTaskFile")
    public ApiResult importProjectTaskFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "productId") String productId, HttpServletResponse response) {
        ProjectTaskExcelListener excelListenerUtil = new ProjectTaskExcelListener(productId, projectTaskService, productInfoService, sysUserFeign, projectPhaseService, taskDocsNameService, taskChargeDistributionService);
        try {
            EasyExcel.read(excelFile.getInputStream(), ProjectTaskExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<ProjectTaskExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProjectTaskExcelDTO> list = excelListenerUtil.getDateList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/productTaskTemplateError.xlsx";
            String name = "productTaskTemplate";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }

            return failure();
        }
        return success();
    }

    /**
     * 下载导入模板
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导入模板")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/productTaskTemplate.xlsx";
        String excelName = "template.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 概览获取任务明细【PLM1.3】
     *
     * @param taskIdList
     * @return
     */
    @PostMapping("/listTaskInfo")
    public ApiResult<List<ProductTask.TaskInfoDTO>> listTaskInfo(@RequestBody List<String> taskIdList) {
        List<ProductTask.TaskInfoDTO> list = projectTaskService.listTaskInfo(taskIdList);
        return success(list);

    }

    @PostMapping("/pagingByAdvanceQuery")
    @WebAdvanceQuery(handler = ProjectTaskQueryHandler.class)
    public ApiResult<PagingVO<ProjectTaskDTO.SimpleViewDTO>> pagingByAdvanceQuery(@RequestBody @Validated PagingDTO<ProjectTaskDTO.PagingParamDTO> dto) {
        PagingVO<ProjectTaskDTO.SimpleViewDTO> list = projectTaskService.pagingByAdvanceQuery(dto);
        return success(list);

    }
}