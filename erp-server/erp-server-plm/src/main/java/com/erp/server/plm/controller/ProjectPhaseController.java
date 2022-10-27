package com.erp.server.plm.controller;


import com.erp.common.annotation.DataPermission;
import com.erp.common.annotation.RequestPermissions;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.server.plm.service.ProjectPhaseService;
import com.erp.server.plm.service.impl.ProductInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/task/phase")
public class ProjectPhaseController extends BaseController {

    @Autowired
    private ProjectPhaseService projectPhaseService;

    /**
     * 项目任务-阶段列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
  //  @RequestPermissions("plm:task:phase:list")
    public ApiResult<List<TaskPhaseDTO>> list(@RequestBody @Validated BasicProductIdDTO dto) {
        List<TaskPhaseDTO> resultList = projectPhaseService.findList(dto);
        return success(resultList);
    }

    /**
     * 项目任务-批量保存或者修改阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchSaveOrUpdate")
    //@RequestPermissions("plm:task:phase:batchSaveOrUpdate")
    public ApiResult batchSaveOrUpdate(@RequestBody @Validated BatchTaskPhaseDTO dto) {
        projectPhaseService.batchSaveOrUpdate(dto);
        return success();
    }

    /**
     * 项目任务-删除阶段
     *
     * @param id
     * @return
     */
    @PostMapping("/remove")
    //  @RequestPermissions("plm:task:phase:remove")
    //@DataPermission(operationType = "delete", tableField = "create_user_id", menuCode = "plm:task:phase:remove", serviceClass = ProductInfoServiceImpl.class)
    public ApiResult remove(String id) {
        Boolean flag = projectPhaseService.removeTaskPhaseById(id);
        return flag == true ? success() : failure();
    }


}

