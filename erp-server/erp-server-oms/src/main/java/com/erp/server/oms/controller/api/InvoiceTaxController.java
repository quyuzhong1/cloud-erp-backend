package com.erp.server.oms.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.server.oms.service.InvoiceTaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 发票税务信息
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票税务信息")
@RequestMapping("/invoiceTax")
public class InvoiceTaxController extends BaseController {

    @Resource
    private InvoiceTaxService invoiceTaxService;


    /**
    * 修改
    * @author will
    * @date:  2025-04-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Validated InvoiceTaxDTO.UpdateDTO dto) {
        invoiceTaxService.update(dto);
        return success();
    }


    /**
     * 根据业务id（销售订单id）查询详情
     * @author will
     * @date 2025/4/7 16:41
     * @param businessId
     * @return ApiResult<VirtualWarehouseAllocationDTO.ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<InvoiceTaxDTO.ViewDTO> view(@RequestParam(value = "businessId") String businessId) {
        return success(invoiceTaxService.view(businessId));
    }
}
