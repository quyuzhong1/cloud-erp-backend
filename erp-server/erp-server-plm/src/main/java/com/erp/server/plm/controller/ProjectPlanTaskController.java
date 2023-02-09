package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.vo.CustomizeFieldVO;
import com.erp.model.sys.vo.UserFieldVO;
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
     * 取消排期
     */
    @PostMapping("/cancel")
    public  ApiResult cancelSchedule(HandleTaskScheduleDTO dto){
        Boolean result = projectPlanTaskService.cancelSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @PostMapping("/restart")
    public  ApiResult restartSchedule(HandleTaskScheduleDTO dto){
        Boolean result = projectPlanTaskService.restartSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 变更排期
     */
    @PostMapping("/change")
    public  ApiResult changeSchedule(List<ChangeTaskScheduleDTO> list){
        Boolean result = projectPlanTaskService.changeSchedule(list);
        return result == true ? success() : failure();
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
     *
     * @return
     */
    @GetMapping("/getUserField")
    public ApiResult<UserFieldVO> getUserField() {
        UserFieldVO result = projectPlanTaskService.getUserField();
        return success(result);
    }

}

