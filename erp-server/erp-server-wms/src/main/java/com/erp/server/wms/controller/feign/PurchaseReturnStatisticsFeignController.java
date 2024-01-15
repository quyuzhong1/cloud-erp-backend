package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import com.erp.server.wms.service.PoReturnService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 退货统计FeignController
 */
@RestController
@RequestMapping("feign/purchaseReturnStatistics")
public class PurchaseReturnStatisticsFeignController extends BaseController {

    @Resource
    private PoReturnService poReturnService;


    @PostMapping("/statisticsBySupplier")
    public PurchaseReturnStatisticsDTO.ResponseDTO statisticsBySupplier(@RequestBody PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO){
        return poReturnService.statisticsBySupplier(returnRequestDTO);
    }
}
