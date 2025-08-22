package com.erp.rpc.bi.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.model.dmp.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-bi", contextId = "exportBiFeign",configuration = {FeignErrorDecoder.class})
public interface ExportBiFeign {

    @PostMapping("/feign/export/exportSkuSales")
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(@RequestBody PagingDTO<SkuSalesDTO.SearchSkuDTO> dto);
    @PostMapping("/feign/export/exportBiOrderInfo")
    PagingVO<DmpOrderInfoExcelDTO> exportBiOrderInfo(@RequestBody PagingDTO<DmpOrderInfoSearchDTO> dto);
    @PostMapping("/feign/export/exportBiReturnOrderInfo")
    PagingVO<DmpReturnOrderInfoExcelDTO> exportBiReturnOrderInfo(@RequestBody PagingDTO<DmpReturnOrderInfoSearchDTO> dto);
}
