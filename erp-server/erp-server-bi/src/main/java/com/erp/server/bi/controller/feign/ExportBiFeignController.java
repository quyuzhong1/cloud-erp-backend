package com.erp.server.bi.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.model.dmp.dto.*;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderInfoService;
import com.erp.server.bi.service.SalesOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportBiFeignController {

    @Resource
    private SalesOrderService salesOrderService;
    @Resource
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;

    @PostMapping("/exportSkuSales")
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(@RequestBody PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
       return salesOrderService.exportSkuSales(dto);
    }

    @PostMapping("/exportBiOrderInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpOrderInfo:paging", tableAlias = "doi")
    PagingVO<DmpOrderInfoExcelDTO> exportBiOrderInfo(@RequestBody PagingDTO<DmpOrderInfoSearchDTO> dto){
        return biOrderInfoService.exportBiOrderInfo(dto);
    }

    @PostMapping("/exportBiReturnOrderInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpReturnOrderInfo:paging", tableAlias = "droi")
    PagingVO<DmpReturnOrderInfoExcelDTO> exportBiReturnOrderInfo(@RequestBody PagingDTO<DmpReturnOrderInfoSearchDTO> dto){
        return biReturnOrderInfoService.exportBiReturnOrderInfo(dto);
    }
}
