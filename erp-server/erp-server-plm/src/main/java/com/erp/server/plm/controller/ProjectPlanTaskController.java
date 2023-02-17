package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.BatchScheduleTaskDTO;
import com.erp.model.plm.dto.ChangeScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.vo.ChangeScheduleExportResultVO;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.plm.vo.ScheduleChangeTaskVO;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.vo.CustomizeFieldVO;
import com.erp.model.sys.vo.UserFieldVO;
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
@RequestMapping("plm/schedule/task")
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
    public ApiResult<ProductItemScheduleVO> list(@Validated @RequestBody ProjectPlanTaskConditionDTO dto) {
        ProductItemScheduleVO scheduleVO = projectPlanTaskService.getTaskList(dto);
        return success(scheduleVO);
    }


    /**
     * 导出数据
     */
    @PostMapping("/exportExcel")
    public ApiResult export(@RequestBody @Validated ProjectPlanTaskConditionDTO dto, HttpServletResponse response) {
        projectPlanTaskService.exportExcel(dto, response);
        return success();
    }

    /**
     * 导出模板
     */
    @PostMapping("/exportTemplate")
    public ApiResult exportScheduleTemplate(HttpServletRequest request, HttpServletResponse response) {
        projectPlanTaskService.exportScheduleTemplate(request, response);
        return success();
    }


    /**
     * 取消排期
     */
    @PostMapping("/cancel")
    public ApiResult cancelSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanTaskService.cancelSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @PostMapping("/restart")
    public ApiResult restartSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanTaskService.restartSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 变更排期
     */
    @PostMapping("/change")
    public ApiResult changeSchedule(@RequestBody @Validated ChangeScheduleDTO dto) {
        Boolean result = projectPlanTaskService.changeSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 导入数据
     */
    @PostMapping("/import")
    public ApiResult importTaskSchedule(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "productId") String  productId, HttpServletResponse response) {
        Boolean result = projectPlanTaskService.importTaskSchedule(excelFile,productId, response);
        return result == true ? success() : failure();
    }


    /**
     * 字段设置
     */
    @PostMapping("/fieldSet")
    public ApiResult fieldSet(@RequestBody CustomizeFieldLayoutDTO dto) {
        Boolean result = projectPlanTaskService.fieldSet(dto);
        return result == true ? success() : failure();
    }

    /**
     * 所有字段显示
     */
    @GetMapping("/allField")
    public ApiResult<List<CustomizeFieldVO>> fieldShow() {
        List<CustomizeFieldVO> result = projectPlanTaskService.allField();
        return success(result);
    }

    /**
     * 获取用户设置字段
     *
     * @return
     */
    @GetMapping("/getUserField")
    public ApiResult<UserFieldVO> getUserField() {
        UserFieldVO result = projectPlanTaskService.getUserField();
        return success(result);
    }

    /**
     * 批量更新
     * 字段
     */
    @PostMapping("/batchUpdate")
    public ApiResult batchUpdate(@RequestBody @Validated BatchScheduleTaskDTO dto) {
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
    @PostMapping("/exportChangeSchedule")
    public ApiResult exportChangeSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto, HttpServletResponse response) {
        projectPlanTaskService.exportChangeSchedule(dto, response);
        return success();
    }


    /**
     * 导入变更排期数据
     */
    @PostMapping("/importChangeSchedule")
    public ApiResult<ChangeScheduleExportResultVO> importChangeSchedule(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "productId") String  productId, HttpServletResponse response) {
        ChangeScheduleExportResultVO vo=  projectPlanTaskService.importChangeSchedule(excelFile, response,productId);
        return success(vo);
    }




}

