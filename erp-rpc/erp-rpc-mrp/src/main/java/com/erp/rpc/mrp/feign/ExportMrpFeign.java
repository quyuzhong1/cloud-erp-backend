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

    @PostMapping("/feign/export/exportHistorySalesQty")
    PagingVO<ReplenishmentSuggestionDTO.HistorySalesQtyDTO> exportHistorySalesQty(@RequestBody PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);

}
