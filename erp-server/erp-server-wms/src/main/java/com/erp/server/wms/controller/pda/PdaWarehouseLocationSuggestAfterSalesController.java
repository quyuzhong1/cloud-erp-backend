package com.erp.server.wms.controller.pda;

import com.common.core.anno.LogSystemModule;
import com.erp.server.wms.service.WarehouseLocationSuggestAfterSalesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * PDA售后推荐仓位管理
 * @author liuchao
 * @date 2026/05/09
 */
@RestController
@RequestMapping("/pdaWarehouseLocationSuggestAfterSales")
@LogSystemModule("PDA售后推荐仓位管理")
public class PdaWarehouseLocationSuggestAfterSalesController {

    @Resource
    private WarehouseLocationSuggestAfterSalesService warehouseLocationSuggestAfterSalesService;


//    @GetMapping("")
//    public ApiResult<List<WarehouseLocationSuggestAfterSalesDto.PdaListDto>> getSuggestWarehouseLocationListBySkuNoAndWarehouseInfo(WarehouseLocationSuggestAfterSalesDto.SearchParamDTO dto) {
////        ApiResult.success(warehouseLocationSuggestAfterSalesService.getSuggestWarehouseLocationList(dto));
//    }
}
