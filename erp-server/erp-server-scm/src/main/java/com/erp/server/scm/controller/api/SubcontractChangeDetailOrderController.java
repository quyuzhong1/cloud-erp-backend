package com.erp.server.scm.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.scm.service.SubcontractChangeDetailOrderService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * 委外变单明细
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@RestController
@RequestMapping("/subcontractChangeDetailOrder")
public class SubcontractChangeDetailOrderController extends BaseController {

    @Autowired
    private SubcontractChangeDetailOrderService subcontractChangeDetailOrderService;



}
