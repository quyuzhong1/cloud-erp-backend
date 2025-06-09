package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import com.erp.server.mrp.service.HistoryImportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
     * 分页查询
     * @author will
     * @date 2024/8/29 9:54
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<HistoryImportRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<HistoryImportRecordDTO.PagingParamDTO> dto) {
        return success(historyImportRecordService.paging(dto));
    }
}
