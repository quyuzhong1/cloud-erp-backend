
package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingPushRecordDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.server.oms.service.ListingPushRecordService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * listing 推送记录
 *
 */
@Slf4j
@RestController
@LogSystemModule("listing推送记录")
@RequestMapping("/listingPushRecord")
public class ListingPushRecordController extends BaseController {

    @Resource
    private ListingPushRecordService listingPushRecordService;

    @Resource
    private SkuMappingService skuMappingService;


    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ListingPushRecordDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto) {
        PagingVO<ListingPushRecordDTO.PagingViewDTO> pagingVO = listingPushRecordService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出库存sku 对照表
     *
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult export(@RequestBody @Valid ListingPushRecordDTO.PagingParamDTO dto) {
        Boolean result = listingPushRecordService.export(dto);
        return result ? success() : failure();
    }
}
