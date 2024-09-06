package com.erp.rpc.mrp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.DynamicExcelDTO;
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
}
