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
import com.erp.server.dmp.service.DmpInputTaskSubStatusService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputTaskSubStatusDTO;

/**
 * 拉取任务子状态
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取任务子状态")
@RequestMapping("/dmpInputTaskSubstatus")
public class DmpInputTaskSubStatusController extends BaseController {

    @Resource
    private DmpInputTaskSubStatusService dmpInputTaskSubstatusService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取任务子状态新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputTaskSubStatusDTO.AddDTO dto) {
        return success(dmpInputTaskSubstatusService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取任务子状态修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputTaskSubstatus:update",
        serviceClass = DmpInputTaskSubStatusService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputTaskSubStatusDTO.UpdateDTO dto) {
        dmpInputTaskSubstatusService.update(dto);
        return success();
    }



}
