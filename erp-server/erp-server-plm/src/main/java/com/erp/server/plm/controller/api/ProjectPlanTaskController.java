package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BatchScheduleTaskDTO;
import com.erp.model.plm.dto.ChangeScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.vo.ChangeScheduleExportResultVO;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.plm.vo.ScheduleChangeTaskVO;
import com.erp.server.plm.service.ProjectPlanTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 产品排期
 *
 * @author yl
 * @since 2023-02-03 15:03:44
 */
@RestController
@LogSystemModule("产品排期审核")
@RequestMapping("schedule/task")
public class ProjectPlanTaskController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProjectPlanTaskService projectPlanTaskService;

    @Resource
    private ProjectTaskService projectTaskService;


    /**
     * 任务列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:schedule:task:list",
            tableAlias = "pt"
    )
    public ApiResult<ProductItemScheduleVO> list(@Validated @RequestBody ProjectPlanTaskConditionDTO dto) {
        ProductItemScheduleVO scheduleVO = projectPlanTaskService.getTaskList(dto);
        return success(scheduleVO);
    }


    /**
     * 导出数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出数据")
    @PostMapping("/exportExcel")
    public ApiResult<Object> export(@RequestBody @Validated ProjectPlanTaskConditionDTO dto) {
       Boolean result= projectPlanTaskService.exportExcel(dto);
        return result?success():failure();
    }

    /**
     * 导出模板
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Object> exportScheduleTemplate(HttpServletRequest request, HttpServletResponse response) {
        projectPlanTaskService.exportScheduleTemplate(request, response);
        return success();
    }


    /**
     * 取消排期
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "取消排期", keyIdName = "productId")
    @PostMapping("/cancel")
    public ApiResult<Object> cancelSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanTaskService.cancelSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重启排期:产品id={productId},任务id={taskIdList}")
    @PostMapping("/restart")
    public ApiResult<Object> restartSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanTaskService.restartSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 变更排期
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "变更排期:产品id={productId}")
    @PostMapping("/change")
    public ApiResult<Object> changeSchedule(@RequestBody @Validated ChangeScheduleDTO dto) {
        Boolean result = projectPlanTaskService.changeSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 导入数据
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入数据")
    @PostMapping("/import")
    public ApiResult<Object> importTaskSchedule(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "productId") String  productId, HttpServletResponse response) {
        Boolean result = projectPlanTaskService.importTaskSchedule(excelFile,productId, response);
        return result == true ? success() : failure();
    }


    /**
     * 批量更新
     * 字段
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量更新:产品ID={productId},任务ids={taskIdList}")
    @PostMapping("/batchUpdate")
    public ApiResult<Object> batchUpdate(@RequestBody @Validated BatchScheduleTaskDTO dto) {
        Boolean result = projectTaskService.batchUpdate(dto);
        return result == true ? success() : failure();
    }


    /**
     * 排期变更添加任务 =查询任务
     */
    @PostMapping("/changeTaskList")
    public ApiResult<List<ScheduleChangeTaskVO>> addChangeTask(@RequestBody @Validated BaseIdDTO dto) {
        List<ScheduleChangeTaskVO> list = projectPlanTaskService.getChangeTaskList(dto);
        return success(list);
    }


    /**
     * 导出变更排期数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出变更排期数据")
    @PostMapping("/exportChangeSchedule")
    public ApiResult<Object> exportChangeSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto, HttpServletResponse response) {
        projectPlanTaskService.exportChangeSchedule(dto, response);
        return success();
    }


    /**
     * 导入变更排期数据
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入变更排期数据")
    @PostMapping("/importChangeSchedule")
    public ApiResult<ChangeScheduleExportResultVO> importChangeSchedule(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "productId") String  productId, HttpServletResponse response) {
        ChangeScheduleExportResultVO vo=  projectPlanTaskService.importChangeSchedule(excelFile, response,productId);
        return success(vo);
    }




}

