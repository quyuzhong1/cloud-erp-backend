package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 销量试算表
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("销量试算表")
@RequestMapping("/calcSalesInfoDim")
public class CalcSalesInfoDimController extends BaseController {

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;


    /**
     * 销量试算列表
     * @param params 参数
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.PagingParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.PagingView> paging = calcSalesInfoDimService.paging(params);
        return success(paging);
    }

//    /**
//     * 补货建议明细
//     * @param detailId 明细id
//     */
//    @GetMapping("/view")
//    public ApiResult<ReplenishmentSuggestionVO.View> view(@RequestParam String detailId) {
//        ReplenishmentSuggestionVO.View view = calcSalesInfoDimService.view(detailId);
//        return success(view);
//    }

}
