package com.erp.server.bi.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.server.bi.service.SalesOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/bi")
public class ExportBiFeignController {

    @Resource
    private SalesOrderService salesOrderService;

    @PostMapping("/exportSkuSales")
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(@RequestBody PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
       return salesOrderService.exportSkuSales(dto);
    }
}
