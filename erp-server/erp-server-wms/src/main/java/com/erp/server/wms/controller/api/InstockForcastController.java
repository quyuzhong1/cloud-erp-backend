package com.erp.server.wms.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.server.wms.service.InstockForcastService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.validation.Valid;

/**
 * <p>
 * 入库预报表 前端控制器
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@AllArgsConstructor
@RestController
@RequestMapping("/instockForcast")
public class InstockForcastController extends BaseController {

    private final InstockForcastService instockForcastService;

    /**
     * 根据采购订单生成入库预报单
     * @param dto
     */
    @PostMapping(value = "/generateByPurchaseOrder")
    public ApiResult<Void> generateByPurchaseOrder(@RequestBody @Valid InstockForcastDTO.AddDTO dto) {
        instockForcastService.generateByPurchaseOrder(dto);
        return success();
    }

    /**
     * 采购订单反审核
     * @param purchaseOrderId
     */
    @PostMapping(value = "/purchaseOrderUnApprove")
    public ApiResult<Void> purchaseOrderUnApprove(@RequestParam(value = "purchaseOrderId")String purchaseOrderId) {
        instockForcastService.purchaseOrderUnApprove(purchaseOrderId);
        return success();
    }

}
