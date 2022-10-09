package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.SysTaskPhaseDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.service.SysTaskPhaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品系统通用设置
 * @Classname
 * @Description TODO
 * @Date 2022-09-13 16:28
 * @Created by yl
 */

@RestController
@RequestMapping("plm/sys/taskPhase")
public class SysTaskPhaseController extends BaseController {

    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;


    /**
     * 新建任务-批量保存或者修改阶段名
     * @author yl
     * @date 2022-10-09 10:27
     * @param list
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/batchSaveOrUpdate")
    public ApiResult add(@RequestBody @Validated List<UpdateBasicNameDTO> list) {
        sysTaskPhaseService.batchSaveOrUpdate(list);
        return success();
    }


    /**
     * 新建任务-修改阶段名
     * @author yl
     * @date 2022-10-09 10:27
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        sysTaskPhaseService.updateTaskPhase(dto);
        return success();
    }


    /**
     * 新建任务-获取阶段名称列表
     * @author yl
     * @date 2022-10-09 10:27
     * @return com.erp.common.dto.base.ApiResult
     */
    @GetMapping("/list")
    public ApiResult<List<SysTaskPhaseEntity>> list() {
        return success( sysTaskPhaseService.list());
    }

}
