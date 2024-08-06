package com.erp.rpc.bi.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.SkuSalesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("erp-bi")
public interface ExportBiFeign {

    @PostMapping("/feign/bi/exportSkuSales")
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(@RequestBody PagingDTO<SkuSalesDTO.SearchSkuDTO> dto);
}
