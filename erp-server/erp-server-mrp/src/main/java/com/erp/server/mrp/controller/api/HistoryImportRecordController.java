package com.erp.server.mrp.controller.api;


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
import com.erp.server.mrp.service.HistoryImportRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;

/**
 * 历史导入记录
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("历史导入记录")
@RequestMapping("/historyImportRecord")
public class HistoryImportRecordController extends BaseController {

    @Resource
    private HistoryImportRecordService historyImportRecordService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "历史导入记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated HistoryImportRecordDTO.AddDTO dto) {
        return success(historyImportRecordService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "历史导入记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:historyImportRecord:update",
        serviceClass = HistoryImportRecordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated HistoryImportRecordDTO.UpdateDTO dto) {
        historyImportRecordService.update(dto);
        return success();
    }



}
