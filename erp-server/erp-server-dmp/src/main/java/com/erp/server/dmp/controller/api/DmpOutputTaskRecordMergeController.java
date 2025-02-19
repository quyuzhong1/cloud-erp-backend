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
import com.erp.server.dmp.service.DmpOutputTaskRecordMergeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpOutputTaskRecordMergeDTO;

/**
 * 推送任务记录合并表
 *
 * @author Luo_WG
 * @since 2025-02-18
 */
@Slf4j
@RestController
@LogSystemModule("推送任务记录合并表")
@RequestMapping("/dmpOutputTaskRecordMerge")
public class DmpOutputTaskRecordMergeController extends BaseController {

    @Resource
    private DmpOutputTaskRecordMergeService dmpOutputTaskRecordMergeService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2025-02-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送任务记录合并表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpOutputTaskRecordMergeDTO.AddDTO dto) {
        return success(dmpOutputTaskRecordMergeService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2025-02-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送任务记录合并表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpOutputTaskRecordMerge:update",
        serviceClass = DmpOutputTaskRecordMergeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpOutputTaskRecordMergeDTO.UpdateDTO dto) {
        dmpOutputTaskRecordMergeService.update(dto);
        return success();
    }



}
