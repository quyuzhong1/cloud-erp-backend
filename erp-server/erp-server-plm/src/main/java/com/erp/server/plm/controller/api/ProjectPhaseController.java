package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.SelectShowDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.server.plm.service.ProjectPhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@LogSystemModule("产品开发管理")
@RequestMapping("task/phase")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "批量保存或者修改阶段:产品ID={productId}")
    @PostMapping("/batchSaveOrUpdate")
    //@RequestPermissions("plm:task:phase:batchSaveOrUpdate")
    public ApiResult<Object> batchSaveOrUpdate(@RequestBody @Validated BatchTaskPhaseDTO dto) {
        projectPhaseService.batchSaveOrUpdate(dto);
        return success();
    }

    /**
     * 项目任务-删除阶段
     *
     * @param id
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "项目任务-删除阶段")
    @PostMapping("/remove")
    //  @RequestPermissions("plm:task:phase:remove")
    //@DataPermission(operationType = "delete", tableField = "create_user_id", menuCode = "plm:task:phase:remove", serviceClass = ProductInfoServiceImpl.class)
    public ApiResult<Object> remove(String id) {
        Boolean flag = projectPhaseService.removeTaskPhaseById(id);
        return flag == true ? success() : failure();
    }

    /**
     * 项目任务-所有阶段
     *
     * @return
     */
    @GetMapping("/listPhaseName")
    public ApiResult<List<SelectShowDTO>> listAll() {
        List<SelectShowDTO> list = projectPhaseService.listPhaseName();
        return success(list);
    }
}

