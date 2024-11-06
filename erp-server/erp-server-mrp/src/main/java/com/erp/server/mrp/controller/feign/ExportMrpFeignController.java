package com.erp.server.mrp.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.server.mrp.handler.ReplenishmentSuggestionQueryHandler;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import com.erp.server.mrp.service.PurchaseSuggestService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportMrpFeignController {

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private PurchaseSuggestService purchaseSuggestService;


    @Resource
    private DeliverySuggestService deliverySuggestService;

    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;
    /**
     * 历史销量导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    @PostMapping("/listHistorySalesQty")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<DynamicExcelDTO> listHistorySalesQty(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return replenishmentSuggestionService.listHistorySalesQty(dto);
    }
    /**
     * (补货建议)采购建议导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<PurchaseSuggestionDTO>
     */
    @PostMapping("/listPurchaseSuggestion")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return purchaseSuggestService.listPurchaseSuggestion(dto);
    }

    /**
     * 备货规则导出数据查询
     * @author will
     * @date 2024/9/6 14:53
     * @param dto
     * @return PagingVO<ReplenishmentRuleDTO>
     */
    @PostMapping("/listReplenishmentRule")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return replenishmentSuggestionService.listReplenishmentRule(dto);
    }

    /**
     * (补货建议)发货建议导出数据查询
     * @author will
     * @date 2024/10/12 14:54
     * @param dto
     * @return PagingVO<DeliverySuggestionDTO>
     */
    @PostMapping("/listDeliverySuggestion")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return deliverySuggestService.listDeliverySuggestion(dto);
    }

    /**
     * 采购建议导出数据查询
     * @author will
     * @date 2024/10/17 10:41
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/pagingPurchaseSuggestion")
    @WebAdvanceQuery
    public PagingVO<PurchaseSuggestDTO.ListDTO> pagingPurchaseSuggestion(@RequestBody PagingDTO<PurchaseSuggestDTO.PagingParamDTO> dto) {
        return purchaseSuggestService.paging(dto);
    }

    /**
     * 发货建议导出数据查询
     * @author will
     * @date 2024/10/17 10:44
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/pagingDeliverySuggestion")
    @WebAdvanceQuery
    public PagingVO<DeliverySuggestDTO.ListDTO> pagingDeliverySuggestion(@RequestBody PagingDTO<DeliverySuggestDTO.PagingParamDTO> dto) {
        return deliverySuggestService.paging(dto);
    }


    @PostMapping("/fbaInventory")
    @WebAdvanceQuery
    public PagingVO<FbaHistoryInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaHistoryInventoryDTO.ExportDTO> dto) {
        return fbaHistoryInventoryService.exportFbaInventory(dto);
    }
}
