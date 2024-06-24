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
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;

/**
 * 推送任务
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("推送任务")
@RequestMapping("/dmpOutputTask")
public class DmpOutputTaskController extends BaseController {

    @Resource
    private DmpOutputTaskService dmpOutputTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpOutputTaskDTO.AddDTO dto) {
        return success(dmpOutputTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpOutputTask:update",
        serviceClass = DmpOutputTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpOutputTaskDTO.UpdateDTO dto) {
        dmpOutputTaskService.update(dto);
        return success();
    }



}
