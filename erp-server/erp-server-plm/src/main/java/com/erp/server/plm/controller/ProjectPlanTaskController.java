package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.server.plm.service.ProjectPlanTaskService;
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
 * @since 2023-02-03 15:03:44
 */
@RestController
@RequestMapping("plm/project/plan")
public class ProjectPlanTaskController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProjectPlanTaskService projectPlanTaskService;


    @PostMapping("/list")
    public ApiResult<ProductItemScheduleVO> list(@Validated @RequestBody ProjectPlanTaskConditionDTO dto) {
        return success(this.projectPlanTaskService.getTaskList(dto));
    }



}

