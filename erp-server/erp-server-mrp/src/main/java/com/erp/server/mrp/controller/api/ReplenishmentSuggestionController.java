package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.vo.*;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 补货建议主表 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@RestController
@RequestMapping("/replenishment")
public class ReplenishmentSuggestionController extends BaseController {

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    /**
     * 补货建议列表
     * @param params 参数
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ReplenishmentSuggestionVO.PagingView>> paging(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        PagingVO<ReplenishmentSuggestionVO.PagingView> paging = replenishmentSuggestionService.paging(params);
        return success(paging);
    }

    /**
     * 补货建议明细
     * @param detailId 明细id
     */
    @GetMapping("/view")
    public ApiResult<ReplenishmentSuggestionVO.View> view(@RequestParam String detailId) {
        ReplenishmentSuggestionVO.View view = replenishmentSuggestionService.view(detailId);
        return success(view);
    }

    /**
     * fba在途明细明细
     * @param params 明细id
     */
    @PostMapping("/fbaInTransitDetail")
    public ApiResult<PagingVO<FbaInTransitDetailVO>> fbaInTransitDetail(@RequestBody @Validated PagingDTO<String> params) {
        PagingVO<FbaInTransitDetailVO> paging = replenishmentSuggestionService.fbaInTransitDetail(params);
        return success(paging);
    }


    /**
     * 海外仓在途明细
     * @param params 明细id
     */
    @PostMapping("/overseasInTransitDetail")
    public ApiResult<PagingVO<OverseasInTransitDetailVO>> overseasInTransitDetail(@RequestBody @Validated PagingDTO<String> params) {
        PagingVO<OverseasInTransitDetailVO> paging = replenishmentSuggestionService.overseasInTransitDetail(params);
        return success(paging);
    }

    /**
     * 本地仓在途明细
     * @param params 明细id
     */
    @PostMapping("/localInTransitDetail")
    public ApiResult<PagingVO<LocalInTransitDetailVO>> localInTransitDetail(@RequestBody @Validated PagingDTO<LocalInTransitDetailDTO> params) {
        PagingVO<LocalInTransitDetailVO> paging = replenishmentSuggestionService.localInTransitDetail(params);
        return success(paging);
    }

    /**
     * 预计发货明细
     * @param params 明细id
     */
    @PostMapping("/estimatedDelivery")
    public ApiResult<PagingVO<EstimatedDeliveryVO>> estimatedDelivery(@RequestBody @Validated PagingDTO<EstimatedDeliveryDTO> params) {
        PagingVO<EstimatedDeliveryVO> paging = replenishmentSuggestionService.estimatedDelivery(params);
        return success(paging);
    }

    /**
     * 预计发货明细
     * @param params 明细id
     */
    @PostMapping("/estimatedPurchase")
    public ApiResult<PagingVO<EstimatedPurchaseVO>> estimatedPurchase(@RequestBody @Validated PagingDTO<EstimatedPurchaseDTO> params) {
        PagingVO<EstimatedPurchaseVO> paging = replenishmentSuggestionService.estimatedPurchase(params);
        return success(paging);
    }

    /**
     * 预计发货明细
     * @param params 明细id
     */
    @PostMapping("/inventoryTotal")
    public ApiResult<Integer> inventoryTotal(@RequestBody @Validated InventoryTotalDTO params) {
        Integer inventoryTotal = replenishmentSuggestionService.inventoryTotal(params);
        return success(inventoryTotal);
    }

    /**
     * 店铺库存明细
     * @param params 明细id
     */
    @PostMapping("/inventoryDetail")
    public ApiResult<InventoryDetailVO> inventoryDetail(@RequestBody @Validated InventoryTotalDTO params) {
        InventoryDetailVO inventoryDetail = replenishmentSuggestionService.inventoryDetail(params);
        return success(inventoryDetail);
    }

}
