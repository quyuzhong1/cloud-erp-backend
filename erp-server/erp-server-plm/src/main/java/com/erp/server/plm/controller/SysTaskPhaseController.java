package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.SysTaskPhaseDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.service.SysTaskPhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Classname 系统阶段名
 * @Description TODO
 * @Date 2022-09-13 16:28
 * @Created by yl
 */
@RestController
@RequestMapping("plm/sys/taskPhase")
public class SysTaskPhaseController extends BaseController {

    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;

    @PostMapping("/batchSaveOrUpdate")
    public ApiResult add(@RequestBody @Validated List<UpdateBasicNameDTO> list) {
        sysTaskPhaseService.batchSaveOrUpdate(list);
        return success();
    }


    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        sysTaskPhaseService.updateTaskPhase(dto);
        return success();
    }


    @GetMapping("/list")
    public ApiResult list() {
        return success( sysTaskPhaseService.list());
    }

}
