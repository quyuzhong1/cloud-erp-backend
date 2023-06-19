package com.erp.server.wms.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.PurchaseOrderDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * 采购订单明细表
 *
 * @author Luo_WG
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/purchaseOrderDetail")
public class PurchaseOrderDetailController extends BaseController {

    @Autowired
    private PurchaseOrderDetailService purchaseOrderDetailService;



}
