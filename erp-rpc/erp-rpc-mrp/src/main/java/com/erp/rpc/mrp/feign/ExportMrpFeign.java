package com.erp.rpc.mrp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-mrp", contextId = "exportMrpFeign", configuration = ExportFeignConfig.class)
public interface ExportMrpFeign {
    /**
     * 导出历史销量（补货建议）
     */
    @PostMapping("/feign/export/exportHistorySalesQty")
    PagingVO<ReplenishmentSuggestionDTO.HistorySalesQtyDTO> exportHistorySalesQty(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出采购建议（补货建议）
     */
    @PostMapping("/feign/export/exportPurchaseSuggestion")
    PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> exportPurchaseSuggestion(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 导出补货规则（补货建议）
     */
    @PostMapping("/feign/export/exportReplenishmentRule")
    PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleDTO> exportReplenishmentRule(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
}
