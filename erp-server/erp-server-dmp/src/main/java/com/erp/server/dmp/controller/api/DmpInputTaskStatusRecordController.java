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
import com.erp.server.dmp.service.DmpInputTaskStatusRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputTaskStatusRecordDTO;

/**
 * 拉取任务状态记录
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取任务状态记录")
@RequestMapping("/dmpInputTaskStatusrecord")
public class DmpInputTaskStatusRecordController extends BaseController {

    @Resource
    private DmpInputTaskStatusRecordService dmpInputTaskStatusrecordService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取任务状态记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputTaskStatusRecordDTO.AddDTO dto) {
        return success(dmpInputTaskStatusrecordService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取任务状态记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputTaskStatusrecord:update",
        serviceClass = DmpInputTaskStatusRecordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputTaskStatusRecordDTO.UpdateDTO dto) {
        dmpInputTaskStatusrecordService.update(dto);
        return success();
    }



}
