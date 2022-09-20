package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.server.plm.service.ProjectPhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 任务阶段表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/task/phase")
public class ProjectPhaseController extends BaseController {

    @Autowired
    private ProjectPhaseService projectPhaseService;

    @GetMapping("/list")
    public ApiResult list(@RequestBody BasicProductIdDTO dto) {
        List<TaskPhaseDTO> resultList = projectPhaseService.findList(dto);
        return success(resultList);
    }

    @PostMapping("/batchSaveOrUpdate")
    public ApiResult batchSaveOrUpdate(@RequestBody @Validated BatchTaskPhaseDTO dto) {
        projectPhaseService.batchSaveOrUpdate(dto);
        return success();
    }


}

