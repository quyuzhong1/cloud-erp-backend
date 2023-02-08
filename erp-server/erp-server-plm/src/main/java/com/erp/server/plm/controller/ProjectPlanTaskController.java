package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.server.plm.service.ProjectPlanTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    @PostMapping("/export")
    public ApiResult export() {
        projectPlanTaskService.export();
        return success();
    }

    /**
     * 导入数据
     */
    @PostMapping("/import")
    public ApiResult importTaskschedule() {
        Boolean result = projectPlanTaskService.importTaskschedule();
        return result == true ? success() : failure();
    }


    /**
     * 字段设置
     */
    @PostMapping("/fieldSet")
    public ApiResult fieldSet(@RequestBody List<CustomizeFieldHiddenDTO> dto) {
        Boolean result = projectPlanTaskService.fieldSet(dto);
        return result == true ? success() : failure();
    }

    /**
     * 所有字段显示
     */
    @GetMapping("/allField")
    public ApiResult<List<CustomizeFieldHiddenDTO>> fieldShow() {
        List<CustomizeFieldHiddenDTO> result = projectPlanTaskService.allField();
        return success(result);
    }

    /**
     *
     * @return
     */
    @GetMapping("/getUserField")
    public ApiResult<List<CustomizeFieldHiddenDTO>> getUserHiddenField() {
        List<CustomizeFieldHiddenDTO> result = projectPlanTaskService.getUserHiddenField();
        return success(result);
    }

}

