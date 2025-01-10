package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestIndependentDTO;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.server.mrp.handler.PurchaseSuggestionMergeQueryHandler;
import com.erp.server.mrp.service.PurchaseSuggestIndependentService;
import com.erp.server.mrp.service.PurchaseSuggestMergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 建议采购(独立采购)
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@RestController
@LogSystemModule("建议采购(独立采购)")
@RequestMapping("/purchaseSuggestIndependent")
public class PurchaseSuggestIndependentController extends BaseController {

    @Resource
    private PurchaseSuggestIndependentService purchaseSuggestIndependentService;

    @Resource
    private PurchaseSuggestMergeService purchaseSuggestMergeService;

    /**
     * 分页查询
     * @author will
     * @date 2024/10/16 10:21
     * @param dto 
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = PurchaseSuggestionMergeQueryHandler.class)
    public ApiResult<PagingVO<PurchaseSuggestIndependentDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseSuggestIndependentDTO.PagingParamDTO> dto) {
        PagingVO<PurchaseSuggestIndependentDTO.ListDTO> pagingVO = purchaseSuggestIndependentService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:59
     * @param params
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<PurchaseSuggestIndependentDTO.ListDTO>> list(@RequestBody @Validated PurchaseSuggestIndependentDTO.ListParamDTO params) {
        List<PurchaseSuggestIndependentDTO.ListDTO> paging = purchaseSuggestIndependentService.list(params);
        return success(paging);
    }


    /**
     * 导出采购建议
     * @author will
     * @date 2024/10/16 12:26
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出发货建议")
    @PostMapping(value = "/export")
    @WebAdvanceQuery
    public ApiResult export(@RequestBody DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = purchaseSuggestIndependentService.export(pagingParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 独立采购弹框
     * @author will
     * @date 2025/1/8 15:22
     * @param dto
     * @return ApiResult<ViewPushDTO>
     */
    @PostMapping(value = "/viewIndependentFrame")
    public ApiResult<List<PurchaseSuggestIndependentDTO.IndependentFrameDTO>> viewIndependentFrame(@RequestBody BaseIdDTO dto) {
        List<PurchaseSuggestIndependentDTO.IndependentFrameDTO> list = purchaseSuggestIndependentService.viewIndependentFrame(dto.getId());
        return success(list);
    }
}
