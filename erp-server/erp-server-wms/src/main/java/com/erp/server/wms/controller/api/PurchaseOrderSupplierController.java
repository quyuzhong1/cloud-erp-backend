package com.erp.server.wms.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.PurchaseOrderSupplierService;
import com.common.core.controller.vo.ApiResult;


/**
 * 采购订单供应商表
 *
 * @author Luo_WG
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/purchaseOrderSupplier")
public class PurchaseOrderSupplierController extends BaseController {

    @Autowired
    private PurchaseOrderSupplierService purchaseOrderSupplierService;



}
