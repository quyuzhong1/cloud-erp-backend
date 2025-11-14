package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpEtlTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;

/**
 * etl任务
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@RestController
@LogSystemModule("etl任务")
@RequestMapping("/dmpEtlTask")
public class DmpEtlTaskController extends BaseController {

    @Resource
    private DmpEtlTaskService dmpEtlTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "etl任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpEtlTaskDTO.AddDTO dto) {
        return success(dmpEtlTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "etl任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpEtlTask:update",
        serviceClass = DmpEtlTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpEtlTaskDTO.UpdateDTO dto) {
        dmpEtlTaskService.update(dto);
        return success();
    }



}
