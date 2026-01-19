package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.server.tms.service.ImportHistoryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流授权表
 *
 * @author will
 * @since 2026-01-19
 */
@Slf4j
@RestController
@LogSystemModule("物流授权表")
@RequestMapping("/importHistoryRecord")
public class ImportHistoryRecordController extends BaseController {

    @Resource
    private ImportHistoryRecordService importHistoryRecordService;


    /**
    * 列表查询
    * @author will
    * @date: 2026-01-19
    * @param dto
    * @return ApiResult<PagingVO<ImportHistoryRecordDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:importHistoryRecord:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<ImportHistoryRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> dto) {
        return success(importHistoryRecordService.paging(dto));
    }


    /**
    * 详情
    * @author will
    * @date:  2026-01-19
    * @param id
    * @return ApiResult<ImportHistoryRecordDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:importHistoryRecord:view",
            serviceClass = ImportHistoryRecordService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ImportHistoryRecordDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(importHistoryRecordService.view(id));
    }


}
