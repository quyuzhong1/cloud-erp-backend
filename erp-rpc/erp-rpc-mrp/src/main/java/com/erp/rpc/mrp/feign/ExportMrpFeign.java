package com.erp.rpc.mrp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-mrp", contextId = "exportMrpFeign", configuration = ExportFeignConfig.class)
public interface ExportMrpFeign {
    /**
     * 导出历史销量（补货建议）
     */
    @PostMapping("/feign/export/listHistorySalesQty")
    PagingVO<DynamicExcelDTO> listHistorySalesQty(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出采购建议（补货建议）
     */
    @PostMapping("/feign/export/listPurchaseSuggestion")
    PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出补货规则（补货建议）
     */
    @PostMapping("/feign/export/listReplenishmentRule")
    PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出发货建议（补货建议）
     */
    @PostMapping("/feign/export/listDeliverySuggestion")
    PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出发货建议
     */
    @PostMapping("/feign/export/pagingDeliverySuggestion")
    PagingVO<DeliverySuggestDTO.ListDTO> pagingDeliverySuggestion(PagingDTO<DeliverySuggestDTO.PagingParamDTO> dto);
    /**
     * 导出采购建议
     */
    @PostMapping("/feign/export/pagingPurchaseSuggestion")
    PagingVO<PurchaseSuggestDTO.ListDTO> pagingPurchaseSuggestion(PagingDTO<PurchaseSuggestDTO.PagingParamDTO> dto);
    /**
     * 导出采购建议（合并）
     */
    @PostMapping("/feign/export/pagingPurchaseSuggestionMerge")
    PagingVO<PurchaseSuggestMergeDTO.ListDTO> pagingPurchaseSuggestionMerge(PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> dto);

    /**
     * fba每日库存
     */
    @PostMapping("/feign/export/fbaInventory")
    PagingVO<FbaHistoryInventoryDTO.ListDTO> exportFbaInventory(@RequestBody PagingDTO<FbaHistoryInventoryDTO.ExportDTO> dto);
    /**
     * 海外仓每日库存
     */
    @PostMapping("/feign/export/overseasInventory")
    PagingVO<OverseasHistoryInventoryDTO.ListDTO> exportOverseasInventory(@RequestBody PagingDTO<OverseasHistoryInventoryDTO.ExportDTO> dto);

    /**
     * 本地仓每日库存
     */
    @PostMapping("/feign/export/localInventory")
    PagingVO<LocalHistoryInventoryDTO.PagingViewDTO> exportLocalInventory(@RequestBody PagingDTO<LocalHistoryInventoryDTO.ExportDTO> dto);

    /**
     * 导出历史销量
     * @param dto 参数
     */
    @PostMapping("/feign/export/exportCalcHistorySale")
    PagingVO<CfgRuleCalcDTO.HistorySaleDTO> exportCalcHistorySale(@RequestBody PagingDTO<CfgRuleCalcDTO.DownloadDTO> dto);
}
