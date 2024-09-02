package com.erp.server.plm.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.PilotApplicationRefTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.PilotApplicationRefTaskDTO;

/**
 * 试产/量产 关联任务
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("试产/量产 关联任务")
@RequestMapping("/pilotApplicationRefTask")
public class PilotApplicationRefTaskController extends BaseController {

    @Resource
    private PilotApplicationRefTaskService pilotApplicationRefTaskService;

    /**
    * 新增
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "试产/量产 关联任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PilotApplicationRefTaskDTO.AddDTO dto) {
        return success(pilotApplicationRefTaskService.add(dto));
    }
}
