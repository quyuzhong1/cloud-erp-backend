package com.erp.server.scm.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * 委外订单明细
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@RestController
@RequestMapping("/subcontractOrderDetail")
public class SubcontractOrderDetailController extends BaseController {

    @Autowired
    private SubcontractOrderDetailService subcontractOrderDetailService;



}
