package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 产品排期
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@RestController
@RequestMapping("plm/product/schedule")
public class ProjectPlanController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProjectPlanService projectPlanService;

    /**
     * 提交排期
     *
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submitSchedule(HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanService.submitSchedule(dto);
        return result == true ? success() : failure();
    }


    /**
     * 取消排期
     */
    @PostMapping("/cancel")
    public  ApiResult cancelSchedule(HandleTaskScheduleDTO dto){
        Boolean result = projectPlanService.cancelSchedule(dto);
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @PostMapping("/restart")
    public  ApiResult restartSchedule(HandleTaskScheduleDTO dto){
        Boolean result = projectPlanService.restartSchedule(dto);
        return result == true ? success() : failure();
    }


    /**
     * 变更排期
     */
    @PostMapping("/change")
    public  ApiResult changeSchedule(HandleTaskScheduleDTO dto){
        Boolean result = projectPlanService.changeSchedule(dto);
        return result == true ? success() : failure();
    }


    /**
     * 排期 审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated AuditParamDTO dto) {
        return success();
    }



    /**
     * 排期  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalNoPass")
    public ApiResult approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        return success();
    }

}

